package net.okocraft.antispam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.Optional;
import java.util.Optional;
import java.util.regex.PatternSyntaxException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
//import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitRunnable;

public class ChatLimiter implements Listener {

    private static final AntiSpam instance = AntiSpam.getInstance();
    private static final Config config = Config.getConfig();

    // 同じ言葉の連続発言規制
    private static final Map<Player, String> previousChat = new HashMap<>();
    private static final Map<Player, Integer> sameChatCount = new HashMap<>();
    private static final Map<Player, Long> sameChatCooldown = new HashMap<>();

    // スパム規制
    private static List<String> ngWords = Config.getConfig().getNgWords();

    public ChatLimiter(AntiSpam plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // 同じプレイヤーの連続発言規制
    private static Player previousPlayer;
    private static int samePlayerCount = 0;
    private static long samePlayerCooldown;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();
        String message = event.getMessage();
        if (message.startsWith("/")) {
            return;
        }

        // 同じ内容の発言規制
        if (!player.hasPermission("antispam.bypass.similarword")) {
            String playerPreviousChat = previousChat.getOrDefault(player, "");
            boolean similar = false;
            if (playerPreviousChat.equalsIgnoreCase(message)) {
                similar = true;
            }

            if (!similar && playerPreviousChat.length() > 10
                    && message.startsWith(playerPreviousChat.substring(0, 9))) {
                similar = true;
            }

            if (!similar && playerPreviousChat.length() > 10
                    && message.endsWith(playerPreviousChat.substring(playerPreviousChat.length() - 10))) {
                similar = true;
            }

            if (similar) {
                sameChatCount.put(player, sameChatCount.getOrDefault(player, 1) + 1);
            } else {
                sameChatCount.put(player, 1);
                previousChat.put(player, message);
            }

            if (sameChatCount.get(player) > config.getTimesSimilarWordLimit() - 1) {
                String errorMsg = config.getMessage("Languages.ChatLimitMessage.SimilarChatDenied")
                        .replaceAll("%times%", Integer.toString(config.getTimesSimilarWordLimit()));
                player.sendMessage(errorMsg);
                event.setCancelled(true);
                event.setMessage("");
                return;
            }
        }

        // スパム規制
        if (!player.hasPermission("antispam.bypass.ngword")) {
            for (String regex : ngWords) {
                try {
                    if (!message.matches(".*" + regex + ".*")) {
                        continue;
                    }
                } catch (PatternSyntaxException e) {
                        continue;
                }
                player.sendMessage(config.getMessage("Languages.ChatLimitMessage.NgWordDenied"));
                event.setCancelled(true);
                event.setMessage("");
                return;
            }
        }

        // 連続発言規制
        if (!player.hasPermission("antispam.bypass.sameplayer")) {
            if (player == previousPlayer) {
                samePlayerCount++;

            } else {
                previousPlayer = player;
                samePlayerCount = 0;
                samePlayerCooldown = System.currentTimeMillis() + config.getCooldownSamePlayerLimit() * 1000L;
            }

            if (samePlayerCount > config.getTimesSamePlayerLimit() - 2) {

                if (samePlayerCooldown > System.currentTimeMillis()) {
                    String errorMsg = config.getMessage("Languages.ChatLimitMessage.SamePlayerChatDenied")
                            .replaceAll("%times%", Integer.toString(config.getTimesSamePlayerLimit()))
                            .replaceAll("%cooldown%",
                                    Long.toString((samePlayerCooldown - System.currentTimeMillis()) / 1000 + 1));
                    player.sendMessage(errorMsg);
                    event.setCancelled(true);
                    event.setMessage("");
                    return;
                } else {
                    samePlayerCount = 0;
                    samePlayerCooldown = System.currentTimeMillis() + 1000 * config.getCooldownSamePlayerLimit();
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (previousChat.containsKey(event.getPlayer())) {
            previousChat.remove(event.getPlayer());
        }

        if (sameChatCooldown.containsKey(event.getPlayer())) {
            sameChatCooldown.remove(event.getPlayer());
        }
    }

    static void addNgWord(String word) {
        ngWords.add(word);
        instance.getConfig().set("NgWordLimit.NgWords", ngWords);
        instance.saveConfig();
    }

    static void removeNgWord(String word) {
        ngWords.remove(word);
        instance.getConfig().set("NgWordLimit.NgWords", ngWords);
        instance.saveConfig();
    }

    private void debugMsg(String message) {
        new BukkitRunnable() {

            @Override
            public void run() {
                Optional.ofNullable(Bukkit.getPlayer("lazy_gon")).ifPresent(lazy -> lazy.sendMessage(message));
            }

        }.runTask(instance);
    }
}