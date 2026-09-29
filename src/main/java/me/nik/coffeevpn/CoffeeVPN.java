package me.nik.coffeevpn;

import me.nik.coffeevpn.files.Config;
import me.nik.coffeevpn.managers.AntiVPNManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class CoffeeVPN extends JavaPlugin {

    private final String[] STARTUP_MESSAGE = new String[]{
            " ",
            ChatColor.RED + "Coffee VPN v" + this.getDescription().getVersion(),
            " ",
            ChatColor.WHITE + "  Author: Nik",
            " "
    };

    private final Config config = new Config(this);

    private final AntiVPNManager antiVPNManager = new AntiVPNManager(this);

    private static CoffeeVPN instance;

    @Override
    public void onEnable() {

        this.getServer().getConsoleSender().sendMessage(STARTUP_MESSAGE);

        instance = this;

        //Load Files
        this.config.setup();

        //Load Managers
        this.antiVPNManager.initialize();
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);

        Bukkit.getScheduler().cancelTasks(this);

        this.antiVPNManager.shutdown();

        this.config.reset();

        instance = null;
    }

    public Config getConfiguration() {
        return config;
    }

    public static CoffeeVPN getInstance() {
        return instance;
    }
}