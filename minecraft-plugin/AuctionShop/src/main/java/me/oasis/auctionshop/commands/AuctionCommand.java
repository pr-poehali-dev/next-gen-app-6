package me.oasis.auctionshop.commands;

import me.oasis.auctionshop.auction.AuctionListing;
import me.oasis.auctionshop.auction.AuctionManager;
import me.oasis.auctionshop.gui.AuctionGui;
import me.oasis.auctionshop.gui.ItemLore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Команда аукциона. Регистрируется под алиасами /ah, /auc, /auction (см. plugin.yml).
 * Использование:
 *   /ah                — открыть окно аукциона
 *   /ah sell <цена>     — выставить предмет из руки на продажу
 *   /ah mail            — открыть посылки (непереданные предметы/деньги)
 */
public class AuctionCommand implements CommandExecutor {

    private final AuctionManager auctionManager;

    public AuctionCommand(AuctionManager auctionManager) {
        this.auctionManager = auctionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Эту команду можно использовать только в игре.");
            return true;
        }

        if (args.length == 0) {
            player.openInventory(AuctionGui.build(player, auctionManager));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "sell" -> handleSell(player, args);
            case "mail", "mailbox" -> player.openInventory(AuctionGui.buildMailbox(player, auctionManager));
            case "my", "listings" -> player.openInventory(AuctionGui.buildMyListings(player, auctionManager));
            default -> player.sendMessage(ChatColor.RED + "Неизвестная команда. Используйте: /ah, /ah sell <цена>, /ah mail");
        }

        return true;
    }

    private void handleSell(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /ah sell <цена>");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Цена должна быть числом.");
            return;
        }

        if (price <= 0) {
            player.sendMessage(ChatColor.RED + "Цена должна быть больше нуля.");
            return;
        }

        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand == null || inHand.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "Возьмите предмет в руку, чтобы выставить его на аукцион.");
            return;
        }

        AuctionListing listing = auctionManager.createListing(player, inHand, price);
        player.getInventory().setItemInMainHand(null);

        player.sendMessage(ChatColor.GREEN + "Лот выставлен на аукцион за "
                + ItemLore.money(listing.getPrice()) + " монет!");
    }
}
