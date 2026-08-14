package me.oasis.auctionshop.economy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Простая встроенная экономика на основе файла balances.yml.
 * Если на сервере уже стоит Vault + другой экономический плагин —
 * этот класс всё равно работает независимо, храня баланс отдельно
 * (чтобы AuctionShop можно было использовать и без Vault).
 */
public class EconomyManager {

    private final JavaPlugin plugin;
    private final File file;
    private FileConfiguration config;

    public EconomyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "balances.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Не удалось создать balances.yml", e);
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось сохранить balances.yml", e);
        }
    }

    public double getBalance(UUID uuid) {
        return config.getDouble(uuid.toString(), 0.0);
    }

    public void setBalance(UUID uuid, double amount) {
        config.set(uuid.toString(), Math.max(0.0, amount));
        save();
    }

    public boolean has(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    public void deposit(UUID uuid, double amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }

    public boolean withdraw(UUID uuid, double amount) {
        double current = getBalance(uuid);
        if (current < amount) {
            return false;
        }
        setBalance(uuid, current - amount);
        return true;
    }
}
