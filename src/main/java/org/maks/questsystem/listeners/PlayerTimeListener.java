package org.maks.questsystem.listeners;

import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.PlayerQuestData;
import org.maks.questsystem.objects.Quest;
import org.maks.questsystem.objects.Quest.QuestObjective;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerTimeListener implements Listener {

    private final QuestSystem plugin;
    private final Map<UUID, Long> sessionStartTime = new HashMap<>();

    public PlayerTimeListener(QuestSystem plugin) {
        this.plugin = plugin;

        // Start tracking task
        startTrackingTask();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        sessionStartTime.put(uuid, System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Update online time
        updatePlayerOnlineTime(player);

        // Clean up
        sessionStartTime.remove(uuid);
    }

    private void startTrackingTask() {
        // Run every minute
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                
                // Update online time for all players
                PlayerQuestData data = plugin.getQuestManager().getPlayerData(uuid);
                data.updateOnlineTime();

                // Check if we need to update quest progress for online hours
                long currentHours = data.getOnlineTimeHours();
                long previousHours = (data.getOnlineTime() - 1) / 60; // Previous minute in hours

                if (currentHours > previousHours) {
                    plugin.getQuestManager().updateProgress(player, 
                        QuestObjective.SPEND_HOURS_ONLINE, 1, null);
                }
                
                // Auto-save player data
                plugin.getQuestDatabase().savePlayerData(data);
            }
        }, 1200L, 1200L); // 20 ticks = 1 second, 1200 ticks = 1 minute
    }

    private void updatePlayerOnlineTime(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Update online time for the player
        PlayerQuestData data = plugin.getQuestManager().getPlayerData(uuid);
        data.updateOnlineTime();

        // Save to database
        plugin.getQuestDatabase().savePlayerData(data);
    }
}
