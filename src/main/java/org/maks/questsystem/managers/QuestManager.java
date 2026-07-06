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
        // Clear existing cache to ensure fresh data load after server restart
        playerData.clear();

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

    public Quest assignQuest(Player player, QuestType type) {
        PlayerQuestData data = getPlayerData(player.getUniqueId());
        int playerLevel = getPlayerLevel(player);

        // Check if player already has a quest for this period
        Quest existingQuest = data.getQuest(type);

        // Debug logging
        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info("Quest assignment check for " + player.getName() + " (" + type + "):");
            plugin.getLogger().info("  - Existing quest: " + (existingQuest != null ? existingQuest.getQuestId() : "null"));
            if (existingQuest != null) {
                plugin.getLogger().info("  - Quest completed: " + existingQuest.isCompleted());
                plugin.getLogger().info("  - Reward claimed: " + existingQuest.isRewardClaimed());
            }
        }

        // If player has any quest (even completed), return it
        if (existingQuest != null) {
            if (existingQuest.isRewardClaimed()) {
                // Quest is complete and reward claimed - can't get new quest until reset
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                    ChatColor.YELLOW + "You have already completed your " + type.name().toLowerCase() +
                    " quest for this period. Wait for the reset to get a new quest!");
            }
            return existingQuest;
        }

        // Generate new quest only if player has no quest at all
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
            if (target == null) {
                return;
            }
            // Support for compound targets like "2|3" or "S|A" (matches if target equals any part)
            String questTarget = quest.getSpecificTarget();
            if (questTarget.contains("|")) {
                // Compound target - check if any part matches
                String[] parts = questTarget.split("\\|");
                boolean matches = false;
                for (String part : parts) {
                    if (part.contains(":")) {
                        // Quality format like "S:20" - match the prefix
                        String prefix = part.split(":")[0];
                        if (target.equals(prefix) || target.startsWith(prefix)) {
                            matches = true;
                            break;
                        }
                    } else if (target.equals(part)) {
                        matches = true;
                        break;
                    }
                }
                if (!matches) {
                    return;
                }
            } else if (!target.equals(questTarget)) {
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

        // Mark quest completion time to prevent multiple quests in same period
        data.markQuestCompleted(type);

        // Update completion counters for monthly quests
        if (type == QuestType.DAILY) {
            data.incrementCompletedDailyQuests();
            updateProgress(player, QuestObjective.COMPLETE_DAILY_QUESTS, 1, null);
        } else if (type == QuestType.WEEKLY) {
            data.incrementCompletedWeeklyQuests();
            updateProgress(player, QuestObjective.COMPLETE_WEEKLY_QUESTS, 1, null);
        }

        // Reset online time progress for SPEND_HOURS_ONLINE quests
        if (quest.getObjective() == QuestObjective.SPEND_HOURS_ONLINE) {
            data.resetOnlineTime();
        }

        // Save to database
        plugin.getQuestDatabase().savePlayerData(data);

        player.sendMessage(plugin.getConfigManager().getMessage("reward_claimed"));
        return true;
    }

    // Reset methods
    public void checkAndPerformResets() {
        LocalDateTime now = LocalDateTime.now();

        // Check for daily reset
        checkDailyReset(now);

        // Check for weekly reset
        checkWeeklyReset(now);

        // Check for monthly reset
        checkMonthlyReset(now);
    }

    private void checkDailyReset(LocalDateTime now) {
        int resetHour = plugin.getConfig().getInt("quests.daily_reset_hour", 0);
        LocalTime resetTime = LocalTime.of(resetHour, 0);

        // Only check during the exact reset minute
        if (now.toLocalTime().getHour() == resetTime.getHour() &&
            now.toLocalTime().getMinute() == 0) {

            playerData.values().forEach(data -> {
                // Check if this player's quest needs resetting
                if (data.getLastDailyReset() == null ||
                    data.getLastDailyReset().toLocalDate().isBefore(now.toLocalDate())) {

                    // Expire the old quest
                    plugin.getQuestDatabase().expireQuest(data.getPlayerUUID(), QuestType.DAILY);

                    // Update reset time
                    data.updateDailyReset();
                    plugin.getQuestDatabase().savePlayerData(data);

                    // Notify online players
                    Player player = Bukkit.getPlayer(data.getPlayerUUID());
                    if (player != null && player.isOnline()) {
                        player.sendMessage(plugin.getConfigManager().getMessage("daily_quest_reset_notification"));
                    }
                }
            });

            // Broadcast once
            Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("daily_reset"));
            plugin.getLogger().info("Daily quests have been reset at " + now);
        }
    }

    private void checkWeeklyReset(LocalDateTime now) {
        DayOfWeek resetDay = DayOfWeek.valueOf(
            plugin.getConfig().getString("quests.weekly_reset_day", "SUNDAY")
        );

        // Only check on reset day at midnight
        if (now.getDayOfWeek() == resetDay &&
            now.toLocalTime().getHour() == 0 &&
            now.toLocalTime().getMinute() == 0) {

            playerData.values().forEach(data -> {
                // Check if this player's quest needs resetting
                LocalDateTime lastWeeklyReset = data.getLastWeeklyReset();

                // Reset if we haven't reset this week yet
                if (lastWeeklyReset == null ||
                    lastWeeklyReset.plusDays(6).isBefore(now)) {

                    // Expire the old quest
                    plugin.getQuestDatabase().expireQuest(data.getPlayerUUID(), QuestType.WEEKLY);

                    // Update reset time
                    data.updateWeeklyReset();
                    plugin.getQuestDatabase().savePlayerData(data);

                    // Notify online players
                    Player player = Bukkit.getPlayer(data.getPlayerUUID());
                    if (player != null && player.isOnline()) {
                        player.sendMessage(plugin.getConfigManager().getMessage("weekly_quest_reset_notification"));
                    }
                }
            });

            // Broadcast once
            Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("weekly_reset"));
            plugin.getLogger().info("Weekly quests have been reset at " + now);
        }
    }

    private void checkMonthlyReset(LocalDateTime now) {
        // Check if it's the first day of month at midnight (00:00)
        // This happens when we transition from last day of previous month to first day of new month
        if (now.getDayOfMonth() == 1 &&
            now.toLocalTime().getHour() == 0 &&
            now.toLocalTime().getMinute() == 0) {

            playerData.values().forEach(data -> {
                // Check if this player's quest needs resetting
                LocalDateTime lastMonthlyReset = data.getLastMonthlyReset();

                // Reset if last reset was in a previous month (or never)
                if (lastMonthlyReset == null ||
                    lastMonthlyReset.getMonth() != now.getMonth() ||
                    lastMonthlyReset.getYear() != now.getYear()) {

                    // Expire the old quest
                    plugin.getQuestDatabase().expireQuest(data.getPlayerUUID(), QuestType.MONTHLY);

                    // Update reset time
                    data.updateMonthlyReset();
                    plugin.getQuestDatabase().savePlayerData(data);

                    // Notify online players
                    Player player = Bukkit.getPlayer(data.getPlayerUUID());
                    if (player != null && player.isOnline()) {
                        player.sendMessage(plugin.getConfigManager().getMessage("monthly_quest_reset_notification"));
                    }
                }
            });

            // Broadcast once
            Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("monthly_reset"));
            plugin.getLogger().info("Monthly quests have been reset at " + now);
        }
    }

    // Manual reset methods for admin commands
    public void resetDailyQuests() {
        plugin.getQuestDatabase().resetAllQuestsOfType(QuestType.DAILY);
        playerData.values().forEach(data -> {
            data.updateDailyReset();
            plugin.getQuestDatabase().savePlayerData(data);
        });
        Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("daily_reset"));
    }

    public void resetWeeklyQuests() {
        plugin.getQuestDatabase().resetAllQuestsOfType(QuestType.WEEKLY);
        playerData.values().forEach(data -> {
            data.updateWeeklyReset();
            plugin.getQuestDatabase().savePlayerData(data);
        });
        Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("weekly_reset"));
    }

    public void resetMonthlyQuests() {
        plugin.getQuestDatabase().resetAllQuestsOfType(QuestType.MONTHLY);
        playerData.values().forEach(data -> {
            data.updateMonthlyReset();
            plugin.getQuestDatabase().savePlayerData(data);
        });
        Bukkit.broadcastMessage(plugin.getConfigManager().getMessage("monthly_reset"));
    }

    // Dodaj metodę do manualnego resetu dla komendy admina
    public void forceResetQuest(Player player, QuestType type) {
        PlayerQuestData data = getPlayerData(player.getUniqueId());

        // Usuń quest z bazy danych
        plugin.getQuestDatabase().expireQuest(player.getUniqueId(), type);

        // Aktualizuj dane w pamięci
        switch (type) {
            case DAILY:
                data.updateDailyReset();
                break;
            case WEEKLY:
                data.updateWeeklyReset();
                break;
            case MONTHLY:
                data.updateMonthlyReset();
                break;
        }

        // Zapisz do bazy
        plugin.getQuestDatabase().savePlayerData(data);

        player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
            ChatColor.GREEN + "Your " + type.name().toLowerCase() + " quest has been reset!");
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
