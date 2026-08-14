package me.oasis.auctionshop.commands;

import me.oasis.auctionshop.economy.EconomyManager;
import me.oasis.auctionshop.gui.ItemLore;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда /balance — показывает баланс монет игрока.
 * /balance give <ник> <сумма> — админ выдаёт деньги игроку.
 */
public class BalanceCommand implements CommandExecutor {

    private final EconomyManager economy;

    public BalanceCommand(EconomyManager economy) {
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Укажите ник игрока: /balance <ник>");
                return true;
            }
            double balance = economy.getBalance(player.getUniqueId());
            player.sendMessage(ChatColor.GOLD + "Ваш баланс: " + ItemLore.money(balance) + " монет");
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("auctionshop.admin")) {
                sender.sendMessage(ChatColor.RED + "У вас нет прав для этой команды.");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Использование: /balance give <ник> <сумма>");
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            double amount;
            try {
                amount = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Сумма должна быть числом.");
                return true;
            }

            economy.deposit(target.getUniqueId(), amount);
            sender.sendMessage(ChatColor.GREEN + "Начислено " + ItemLore.money(amount) + " монет игроку " + args[1]);
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        double balance = economy.getBalance(target.getUniqueId());
        sender.sendMessage(ChatColor.GOLD + "Баланс " + args[0] + ": " + ItemLore.money(balance) + " монет");
        return true;
    }
}
