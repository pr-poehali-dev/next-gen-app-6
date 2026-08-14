package me.oasis.auctionshop.auction;

import me.oasis.auctionshop.economy.EconomyManager;
import me.oasis.auctionshop.util.ItemCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Управляет активными лотами аукциона и «почтовым ящиком» —
 * куда попадают деньги продавца после продажи и невостребованные предметы,
 * которые не забрали сразу (например, если инвентарь был полон).
 */
public class AuctionManager {

    private final JavaPlugin plugin;
    private final EconomyManager economy;
    private final File file;
    private FileConfiguration config;

    private final Map<UUID, AuctionListing> listings = new HashMap<>();
    // Предметы, ожидающие выдачи игроку (купленный товар, если инвентарь был полон, либо непроданный лот)
    private final Map<UUID, List<ItemStack>> mailbox = new HashMap<>();

    public AuctionManager(JavaPlugin plugin, EconomyManager economy) {
        this.plugin = plugin;
        this.economy = economy;
        this.file = new File(plugin.getDataFolder(), "auction.yml");
        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Не удалось создать auction.yml", e);
            }
        }
        config = YamlConfiguration.loadConfiguration(file);

        List<?> rawListings = config.getList("listings");
        if (rawListings != null) {
            for (Object obj : rawListings) {
                if (obj instanceof AuctionListing listing) {
                    listings.put(listing.getId(), listing);
                } else if (obj instanceof Map<?, ?> map) {
                    AuctionListing listing = AuctionListing.deserialize((Map<String, Object>) map);
                    listings.put(listing.getId(), listing);
                }
            }
        }

        if (config.isConfigurationSection("mailbox")) {
            for (String uuidStr : config.getConfigurationSection("mailbox").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                List<?> items = config.getList("mailbox." + uuidStr);
                List<ItemStack> stacks = new ArrayList<>();
                if (items != null) {
                    for (Object o : items) {
                        if (o instanceof ItemStack stack) {
                            stacks.add(stack);
                        }
                    }
                }
                mailbox.put(uuid, stacks);
            }
        }
    }

    public void save() {
        config.set("listings", new ArrayList<>(listings.values()));
        config.set("mailbox", null);
        for (Map.Entry<UUID, List<ItemStack>> entry : mailbox.entrySet()) {
            config.set("mailbox." + entry.getKey(), entry.getValue());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось сохранить auction.yml", e);
        }
    }

    public AuctionListing createListing(Player seller, ItemStack item, double price) {
        AuctionListing listing = new AuctionListing(
                UUID.randomUUID(),
                seller.getUniqueId(),
                seller.getName(),
                item.clone(),
                price,
                System.currentTimeMillis()
        );
        listings.put(listing.getId(), listing);
        save();
        return listing;
    }

    public List<AuctionListing> getActiveListings() {
        return new ArrayList<>(listings.values());
    }

    public List<AuctionListing> getListingsBySeller(UUID sellerId) {
        return listings.values().stream()
                .filter(l -> l.getSellerId().equals(sellerId))
                .collect(Collectors.toList());
    }

    public List<AuctionListing> getSortedFiltered(ItemCategory category, me.oasis.auctionshop.util.SortMode sortMode) {
        List<AuctionListing> result = listings.values().stream()
                .filter(l -> category == ItemCategory.ALL || ItemCategory.categoryOf(l.getItem().getType()) == category)
                .collect(Collectors.toList());

        switch (sortMode) {
            case PRICE_ASC -> result.sort(Comparator.comparingDouble(AuctionListing::getPrice));
            case PRICE_DESC -> result.sort(Comparator.comparingDouble(AuctionListing::getPrice).reversed());
            case NEWEST -> result.sort(Comparator.comparingLong(AuctionListing::getCreatedAt).reversed());
        }
        return result;
    }

    public AuctionListing getListing(UUID id) {
        return listings.get(id);
    }

    public void removeListing(UUID id) {
        listings.remove(id);
        save();
    }

    /**
     * Покупка лота. Деньги зачисляются продавцу в его "почтовый ящик" (баланс),
     * предмет передаётся покупателю напрямую или в mailbox, если инвентарь полон.
     */
    public boolean buy(Player buyer, UUID listingId) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null) {
            return false;
        }
        if (listing.getSellerId().equals(buyer.getUniqueId())) {
            return false;
        }
        if (!economy.withdraw(buyer.getUniqueId(), listing.getPrice())) {
            return false;
        }

        economy.deposit(listing.getSellerId(), listing.getPrice());
        removeListing(listingId);

        giveOrMail(buyer.getUniqueId(), listing.getItem());
        return true;
    }

    /**
     * Продавец снимает свой лот с продажи и забирает предмет обратно.
     */
    public boolean cancelListing(Player player, UUID listingId) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null || !listing.getSellerId().equals(player.getUniqueId())) {
            return false;
        }
        removeListing(listingId);
        giveOrMail(player.getUniqueId(), listing.getItem());
        return true;
    }

    public void giveOrMail(UUID playerId, ItemStack item) {
        Player online = plugin.getServer().getPlayer(playerId);
        if (online != null && online.isOnline()) {
            Map<Integer, ItemStack> leftover = online.getInventory().addItem(item.clone());
            if (leftover.isEmpty()) {
                return;
            }
            for (ItemStack left : leftover.values()) {
                addToMailbox(playerId, left);
            }
        } else {
            addToMailbox(playerId, item);
        }
    }

    private void addToMailbox(UUID playerId, ItemStack item) {
        mailbox.computeIfAbsent(playerId, k -> new ArrayList<>()).add(item.clone());
        save();
    }

    public List<ItemStack> getMailbox(UUID playerId) {
        return mailbox.getOrDefault(playerId, new ArrayList<>());
    }

    public void clearMailboxItem(UUID playerId, ItemStack item) {
        List<ItemStack> box = mailbox.get(playerId);
        if (box != null) {
            box.remove(item);
            if (box.isEmpty()) {
                mailbox.remove(playerId);
            }
            save();
        }
    }
}
