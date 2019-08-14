package net.okocraft.antispam;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiSpam extends JavaPlugin {

    private static AntiSpam instance;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        new ChatLimiter(instance);
        new AntiSpamCommand(instance);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(instance);
    }

    public static AntiSpam getInstance() {
        if (instance == null) {
            instance = (AntiSpam) Bukkit.getPluginManager().getPlugin("AntiSpam");
        }

        return instance;
    }
}
