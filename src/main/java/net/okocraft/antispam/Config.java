package net.okocraft.antispam;

import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import net.md_5.bungee.api.ChatColor;

public class Config {

    private static Config singleton;
    private final AntiSpam instance;
    private FileConfiguration config;

    private Config(AntiSpam plugin) {
        this.instance = (AntiSpam) plugin;
        this.config = instance.getConfig();
    }

    public static Config getConfig() {
        if (singleton == null) {
            singleton = new Config(AntiSpam.getInstance());
        }

        return singleton;
    }

    public void reloadConfig() {
        instance.reloadConfig();
        this.config = instance.getConfig();
    }

    boolean isEnabledSamePlayerLimit() {
        return config.getBoolean("SamePlayerLimit.Enabled");
    }

    int getTimesSamePlayerLimit() {
        return config.getInt("SamePlayerLimit.Times", 4);
    }

    int getCooldownSamePlayerLimit() {
        return config.getInt("SamePlayerLimit.Cooldown", 8);
    }

    boolean isEnabledSimilarWordLimit() {
        return config.getBoolean("SimilarWordLimit.Enabled");
    }

    int getTimesSimilarWordLimit() {
        return config.getInt("SimilarWordLimit.Times", 5);
    }

    boolean isEnabledNgWordLimit() {
        return config.getBoolean("NgWordLimit.Enabled");
    }

    List<String> getNgWords() {
        return config.getStringList("NgWordLimit.NgWords");
    }

    String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes(
                '&',
                config.getString(path, "Path is Invalid. Please report this error to admins.")
        );
    }
}