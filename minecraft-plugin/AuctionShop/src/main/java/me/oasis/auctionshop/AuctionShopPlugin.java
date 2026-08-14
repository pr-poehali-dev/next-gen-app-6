package me.oasis.auctionshop;

import me.oasis.auctionshop.auction.AuctionManager;
import me.oasis.auctionshop.commands.AuctionCommand;
import me.oasis.auctionshop.commands.BalanceCommand;
import me.oasis.auctionshop.commands.ShopCommand;
import me.oasis.auctionshop.economy.EconomyManager;
import me.oasis.auctionshop.gui.GuiListener;
import me.oasis.auctionshop.shop.ShopManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Точка входа плагина AuctionShop — аукцион и постоянный магазин для сервера.
 * Регистрирует команды /ah (и алиасы /auc, /auction), /shop, /balance,
 * а также единый слушатель кликов по GUI.
 */
public class AuctionShopPlugin extends JavaPlugin {

    private EconomyManager economyManager;
    private AuctionManager auctionManager;
    private ShopManager shopManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        economyManager = new EconomyManager(this);
        auctionManager = new AuctionManager(this, economyManager);
        shopManager = new ShopManager(this, economyManager);

        getCommand("ah").setExecutor(new AuctionCommand(auctionManager));
        getCommand("shop").setExecutor(new ShopCommand(shopManager));
        getCommand("balance").setExecutor(new BalanceCommand(economyManager));

        getServer().getPluginManager().registerEvents(new GuiListener(auctionManager, shopManager), this);

        getLogger().info("AuctionShop включён! Команды: /ah (/auc, /auction), /shop, /balance");
    }

    @Override
    public void onDisable() {
        if (auctionManager != null) {
            auctionManager.save();
        }
        if (shopManager != null) {
            shopManager.save();
        }
        if (economyManager != null) {
            economyManager.save();
        }
        getLogger().info("AuctionShop выключен, все данные сохранены.");
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public AuctionManager getAuctionManager() {
        return auctionManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }
}
