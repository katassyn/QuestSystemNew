package org.maks.questsystem.managers;

import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.PlayerQuestData;
import org.maks.questsystem.objects.Quest;
import org.maks.questsystem.objects.Quest.QuestObjective;
import org.maks.questsystem.objects.Quest.QuestType;
import org.maks.questsystem.utils.QuestFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuestManager {

    private final QuestSystem plugin;
    private final Map<UUID, PlayerQuestData> playerData;
    private final QuestFactory questFactory;

    public QuestManager(QuestSystem plugin) {
        this.plugin = plugin;
        this.playerData = new ConcurrentHashMap<>();
        this.questFactory = new QuestFactory(plugin);

        // Load all player data from database
        loadAllPlayerData();
    }

    private void loadAllPlayerData() {
        plugin.getQuestDatabase().loadAllPlayerData().forEach(data -> {
            playerData.put(data.getPlayerUUID(), data);
        });
    }

    public void saveAllPlayerData() {
        playerData.values().forEach(data -> {
            plugin.getQuestDatabase().savePlayerData(data);
        });
    }

    public PlayerQuestData getPlayerData(UUID playerUUID) {
        return playerData.computeIfAbsent(playerUUID, uuid -> {
            PlayerQuestData data = plugin.getQuestDatabase().loadPlayerData(uuid);
            return data != null ? data : new PlayerQuestData(uuid);
        });
    }

    // Quest assignment
    public Quest assignQuest(Player player, QuestType type) {
        PlayerQuestData data = getPlayerData(player.getUniqueId());
        int playerLevel = getPlayerLevel(player);

        // Check if player already has active quest
        if (data.hasActiveQuest(type)) {
            return data.getQuest(type);
        }

        // Generate new quest
        Quest quest = questFactory.generateQuest(type, playerLevel);
        data.setQuest(quest);

        // Save to database
        plugin.getQuestDatabase().saveQuest(player.getUniqueId(), quest);

        return quest;
    }

    // Quest reroll
    public Quest rerollQuest(Player player, QuestType type) {
        PlayerQuestData data = getPlayerData(player.getUniqueId());

        // Check permissions
        boolean isPremium = hasPermission(player, "questsystem.reroll.premium");
        boolean isDeluxe = hasPermission(player, "questsystem.reroll.deluxe");

        if (!data.canReroll(type, isPremium, isDeluxe)) {
            return null; // Cannot reroll
        }

        // Use reroll
        data.useReroll(type);

        // Generate new quest
        int playerLevel = getPlayerLevel(player);
        Quest newQuest = questFactory.generateQuest(type, playerLevel);
        data.setQuest(newQuest);

        // Save to database
        plugin.getQuestDatabase().saveQuest(player.getUniqueId(), newQuest);
        plugin.getQuestDatabase().savePlayerData(data);

        return newQuest;
    }

    // Progress updates
    public void updateProgress(Player player, QuestObjective objective, int amount, String target) {
        PlayerQuestData data = getPlayerData(player.getUniqueId());
        int playerLevel = getPlayerLevel(player);

        // Check all active quests
        updateQuestProgress(data.getDailyQuest(), objective, amount, target, playerLevel);
        updateQuestProgress(data.getWeeklyQuest(), objective, amount, target, playerLevel);
        updateQuestProgress(data.getMonthlyQuest(), objective, amount, target, playerLevel);

        // Save progress
        plugin.getQuestDatabase().savePlayerData(data);
    }

    private void updateQuestProgress(Quest quest, QuestObjective objective, int amount, 
                                   String target, int playerLevel) {
        if (quest == null || quest.isCompleted()) {
            return;
        }

        // Check if quest objective matches
        if (quest.getObjective() != objective) {
            return;
        }

        // Check if player level is appropriate
        if (!quest.isApplicableForLevel(playerLevel)) {
            return;
        }

        // Check specific target if needed
        if (quest.getSpecificTarget() != null && !quest.getSpecificTarget().isEmpty()) {
            if (target == null || !target.equals(quest.getSpecificTarget())) {
                return;
            }
        }

        // Update progress
        quest.addProgress(amount);

        // Notify player if completed
        if (quest.isCompleted()) {
            // Find the player who owns this quest
            for (Map.Entry<UUID, PlayerQuestData> entry : playerData.entrySet()) {
                PlayerQuestData pData = entry.getValue();
                if ((pData.getDailyQuest() != null && pData.getDailyQuest().getQuestId().equals(quest.getQuestId())) ||
                    (pData.getWeeklyQuest() != null && pData.getWeeklyQuest().getQuestId().equals(quest.getQuestId())) ||
                    (pData.getMonthlyQuest() != null && pData.getMonthlyQuest().getQuestId().equals(quest.getQuestId()))) {

                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null && player.isOnline()) {
                        // Display notification in the center of the screen
                        player.sendTitle(
                            ChatColor.GREEN + "Quest Completed!",
                            ChatColor.GOLD + quest.getDescription(),
                            10, 70, 20
                        );

                        // Also send a chat message
                        player.sendMessage(plugin.getConfigManager().getMessage("quest_complete", 
                            new HashMap<String, String>() {{
                                put("quest", quest.getDescription());
                                put("type", quest.getType().toString());
                            }}
                        ));

                        // Fire quest complete event
                        Bukkit.getPluginManager().callEvent(new org.maks.questsystem.events.QuestCompleteEvent(player, quest));
                    }
                    break;
                }
            }
        }
    }

    // Quest completion
    public boolean claimReward(Player player, QuestType type) {
        PlayerQuestData data = getPlayerData(player.getUniqueId());
        Quest quest = data.getQuest(type);

        if (quest == null || !quest.isCompleted() || quest.isRewardClaimed()) {
            return false;
        }

        // Check inventory space
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(plugin.getConfigManager().getMessage("inventory_full"));
            return false;
        }

        // Give rewards
        plugin.getRewardManager().giveRewards(player, type, getPlayerLevel(player));

        // Mark as claimed
        quest.claimReward();

        // Update completion counters for monthly quests
        if (type == QuestType.DAILY) {
            data.incrementCompletedDailyQuests();
            updateProgress(player, QuestObjective.COMPLETE_DAILY_QUESTS, 1, null);
        } else if (type == QuestType.WEEKLY) {
            data.incrementCompletedWeeklyQuests();
            updateProgress(player, QuestObjective.COMPLETE_WEEKLY_QUESTS, 1, null);
        }

        // Save to database
        plugin.getQuestDatabase().savePlayerData(data);

        player.sendMessage(plugin.getConfigManager().getMessage("reward_claimed"));
        return true;
    }

    // Reset methods
    public void resetDailyQuests() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime resetTime = LocalTime.of(plugin.getConfig().getInt("quests.daily_reset_hour", 0), 0);

        // Check if it's within the reset hour (not just exact time)
        if (now.toLocalTime().getHour() == resetTime.getHour() && now.toLocalTime().getMinute() < 5) {
            playerData.values().forEach(data -> {
                // Check if we haven't already reset today
                if (data.getLastDailyReset() == null || 
                    !data.getLastDailyReset().toLocalDate().equals(now.toLocalDate())) {

                    data.updateDailyReset();
                    plugin.getQuestDatabase().savePlayerData(data);
                }
            });

            // Only broadcast once per reset
            if (now.toLocalTime().getMinute() == 0) {
                Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("daily_reset"));
            }
        }
    }

    public void resetWeeklyQuests() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek resetDay = DayOfWeek.valueOf(
            plugin.getConfig().getString("quests.weekly_reset_day", "SUNDAY")
        );

        // Check if it's the right day and within first 5 minutes of reset hour
        if (now.getDayOfWeek() == resetDay && now.toLocalTime().getHour() == 0 && now.toLocalTime().getMinute() < 5) {
            playerData.values().forEach(data -> {
                // Check if we haven't already reset this week
                if (data.getLastWeeklyReset() == null || 
                    data.getLastWeeklyReset().plusDays(6).isBefore(now)) {

                    data.updateWeeklyReset();
                    plugin.getQuestDatabase().savePlayerData(data);
                }
            });

            // Only broadcast once per reset
            if (now.toLocalTime().getMinute() == 0) {
                Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("weekly_reset"));
            }
        }
    }

    public void resetMonthlyQuests() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastDayOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());

        // Check if it's the last day of month within first 5 minutes of midnight
        if (now.toLocalDate().equals(lastDayOfMonth.toLocalDate()) && 
            now.toLocalTime().getHour() == 0 && now.toLocalTime().getMinute() < 5) {

            playerData.values().forEach(data -> {
                // Check if we haven't already reset this month
                if (data.getLastMonthlyReset() == null || 
                    data.getLastMonthlyReset().getMonth() != now.getMonth()) {

                    data.updateMonthlyReset();
                    plugin.getQuestDatabase().savePlayerData(data);
                }
            });

            // Only broadcast once per reset
            if (now.toLocalTime().getMinute() == 0) {
                Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("monthly_reset"));
            }
        }
    }

    // Utility methods
    private int getPlayerLevel(Player player) {
        // Integration with MyExperiencePlugin
        if (Bukkit.getPluginManager().getPlugin("MyExperiencePlugin") != null) {
            // Use reflection or API to get player level
            return player.getLevel(); // Placeholder - implement actual integration
        }
        return player.getLevel();
    }

    private boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }

    // Getters
    public Map<UUID, PlayerQuestData> getAllPlayerData() {
        return new HashMap<>(playerData);
    }
}
