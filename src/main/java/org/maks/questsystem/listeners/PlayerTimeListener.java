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
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import org.maks.questsystem.objects.Quest.QuestType;

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

        // Only check for expired quests, don't reset on login
        checkExpiredQuestsOnLogin(player);
    }

    private void checkExpiredQuestsOnLogin(Player player) {
        PlayerQuestData data = plugin.getQuestManager().getPlayerData(player.getUniqueId());
        LocalDateTime now = LocalDateTime.now();
        int dailyResetHour = plugin.getConfig().getInt("quests.daily_reset_hour", 0);
        boolean needsSave = false;

        // Check daily quest - only reset if we actually passed the reset time
        if (data.getDailyQuest() != null && data.getLastDailyReset() != null) {
            LocalDateTime lastReset = data.getLastDailyReset();
            LocalDateTime todayResetTime = now.toLocalDate().atTime(dailyResetHour, 0);

            // Reset if:
            // 1. Last reset was yesterday or earlier AND we're past today's reset time
            // 2. Last reset was today but before reset hour AND we're past reset hour
            boolean shouldReset = false;

            if (lastReset.toLocalDate().isBefore(now.toLocalDate())) {
                // Last reset was on a previous day
                if (now.toLocalTime().getHour() >= dailyResetHour) {
                    // We're past today's reset time
                    shouldReset = true;
                }
            } else if (lastReset.toLocalDate().equals(now.toLocalDate())) {
                // Last reset was today - check if it was before reset hour
                if (lastReset.toLocalTime().getHour() < dailyResetHour &&
                    now.toLocalTime().getHour() >= dailyResetHour) {
                    shouldReset = true;
                }
            }

            if (shouldReset) {
                plugin.getQuestDatabase().expireQuest(player.getUniqueId(), Quest.QuestType.DAILY);
                data.updateDailyReset();
                needsSave = true;
                player.sendMessage(plugin.getConfigManager().getMessage("daily_quest_reset_notification"));

                if (plugin.getConfig().getBoolean("debug", false)) {
                    plugin.getLogger().info("Reset expired daily quest for " + player.getName() +
                                          " (last reset: " + lastReset + ", now: " + now + ")");
                }
            }
        }

        // Check weekly quest
        DayOfWeek resetDay = DayOfWeek.valueOf(
            plugin.getConfig().getString("quests.weekly_reset_day", "SUNDAY")
        );

        if (data.getWeeklyQuest() != null && data.getLastWeeklyReset() != null) {
            LocalDateTime lastReset = data.getLastWeeklyReset();

            // Find the most recent reset point (Sunday at midnight)
            LocalDateTime lastSundayMidnight = now.with(TemporalAdjusters.previousOrSame(resetDay))
                                                  .withHour(0).withMinute(0).withSecond(0);

            // Only reset if last reset was before the most recent reset point
            if (lastReset.isBefore(lastSundayMidnight)) {
                plugin.getQuestDatabase().expireQuest(player.getUniqueId(), Quest.QuestType.WEEKLY);
                data.updateWeeklyReset();
                needsSave = true;
                player.sendMessage(plugin.getConfigManager().getMessage("weekly_quest_reset_notification"));

                if (plugin.getConfig().getBoolean("debug", false)) {
                    plugin.getLogger().info("Reset expired weekly quest for " + player.getName() +
                                          " (last reset: " + lastReset + ", reset point: " + lastSundayMidnight + ")");
                }
            }
        }

        // Check monthly quest
        if (data.getMonthlyQuest() != null && data.getLastMonthlyReset() != null) {
            LocalDateTime lastReset = data.getLastMonthlyReset();

            // Check if we're in a new month
            if ((lastReset.getMonth() != now.getMonth() || lastReset.getYear() != now.getYear())) {
                plugin.getQuestDatabase().expireQuest(player.getUniqueId(), Quest.QuestType.MONTHLY);
                data.updateMonthlyReset();
                needsSave = true;
                player.sendMessage(plugin.getConfigManager().getMessage("monthly_quest_reset_notification"));

                if (plugin.getConfig().getBoolean("debug", false)) {
                    plugin.getLogger().info("Reset expired monthly quest for " + player.getName() +
                                          " (last reset month: " + lastReset.getMonth() +
                                          ", current month: " + now.getMonth() + ")");
                }
            }
        }

        if (needsSave) {
            plugin.getQuestDatabase().savePlayerData(data);
        }
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
