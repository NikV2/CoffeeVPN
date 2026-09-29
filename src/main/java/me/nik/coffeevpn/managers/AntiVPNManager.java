package me.nik.coffeevpn.managers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.nik.coffeevpn.CoffeeVPN;
import me.nik.coffeevpn.files.Config;
import me.nik.coffeevpn.files.commentedfiles.CommentedFileConfiguration;
import me.nik.coffeevpn.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AntiVPNManager implements Listener {

    private final CoffeeVPN plugin;

    public AntiVPNManager(CoffeeVPN plugin) {
        this.plugin = plugin;
    }

    private final List<ProxyProvider> providers = new ArrayList<>();

    private LoadingCache<String, Boolean> detections;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        if (Config.Setting.WHITELISTED_PLAYERS.getStringList().contains(e.getName())) return;

        if (this.detections.getUnchecked(e.getAddress().getHostAddress())) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatUtils.format(Config.Setting.MESSAGE.getString()));
        }
    }

    public void initialize() {

        CommentedFileConfiguration config = this.plugin.getConfiguration().getConfig();

        ConfigurationSection section = config.getConfigurationSection("providers");

        if (section != null) {
            section.getKeys(false).forEach(value -> {

                String provider = "providers." + value;

                try {
                    this.providers.add(
                            new ProxyProvider(
                                    config.getString(provider + ".url"),
                                    config.getString(provider + ".api_key"),
                                    config.getString(provider + ".check")
                            )
                    );
                } catch (Throwable t) {
                    ChatUtils.log("Error while registering VPN Provider: " + value);
                }

                ChatUtils.log("Registered VPN Provider: " + value);
            });
        } else {
            ChatUtils.log("No VPN providers found!");
        }

        this.detections = CacheBuilder.newBuilder()
                .expireAfterWrite(Config.Setting.CACHE_CLEANUP.getInt(), TimeUnit.MINUTES)
                .build(new CacheLoader<String, Boolean>() {
                    @Override
                    public @NotNull Boolean load(@NotNull String ip) {
                        return providers.stream().anyMatch(provider -> provider.check(ip));
                    }
                });

        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    public void shutdown() {
        this.providers.clear();
        HandlerList.unregisterAll(this);
    }
}