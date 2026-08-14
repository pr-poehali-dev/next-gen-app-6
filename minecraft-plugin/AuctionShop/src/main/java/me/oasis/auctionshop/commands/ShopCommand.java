package me.oasis.auctionshop.commands;

import me.oasis.auctionshop.gui.ShopGui;
import me.oasis.auctionshop.shop.ShopItem;
import me.oasis.auctionshop.shop.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Команда серверного магазина. Регистрируется как /shop.
 * Использование:
 *   /shop                              — открыть магазин
 *   /shop additem <цена_покупки> <цена_продажи> — добавить предмет из руки в магазин (только для админов)
 *   /shop removeitem <id>              — убрать товар из магазина (только для админов)
 */
public class ShopCommand implements CommandExecutor {

    private final ShopManager shopManager;

    public ShopCommand(ShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Эту команду можно использовать только в игре.");
            return true;
        }

        if (args.length == 0) {
            player.openInventory(ShopGui.build(player, shopManager));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "additem" -> handleAddItem(player, args);
            case "removeitem" -> handleRemoveItem(player, args);
            default -> player.sendMessage(ChatColor.RED + "Использование: /shop, /shop additem <покупка> <продажа>");
        }

        return true;
    }

    private void handleAddItem(Player player, String[] args) {
        if (!player.hasPermission("auctionshop.admin")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав для управления магазином.");
            return;
        }
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Использование: /shop additem <цена_покупки> <цена_продажи>");
            return;
        }

        double buyPrice;
        double sellPrice;
        try {
            buyPrice = Double.parseDouble(args[1]);
            sellPrice = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Цены должны быть числами.");
            return;
        }

        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand == null || inHand.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "Возьмите предмет в руку, чтобы добавить его в магазин.");
            return;
        }

        ItemStack singleItem = inHand.clone();
        singleItem.setAmount(1);

        ShopItem shopItem = shopManager.addItem(singleItem, buyPrice, sellPrice);
        player.sendMessage(ChatColor.GREEN + "Товар добавлен в магазин! ID: " + shopItem.getId());
    }

    private void handleRemoveItem(Player player, String[] args) {
        if (!player.hasPermission("auctionshop.admin")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав для управления магазином.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Использование: /shop removeitem <id>");
            return;
        }

        try {
            java.util.UUID id = java.util.UUID.fromString(args[1]);
            if (shopManager.removeItem(id)) {
                player.sendMessage(ChatColor.GREEN + "Товар удалён из магазина.");
            } else {
                player.sendMessage(ChatColor.RED + "Товар с таким ID не найден.");
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Некорректный ID.");
        }
    }
}
