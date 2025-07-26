package org.maks.questsystem.objects;

import org.maks.questsystem.objects.Quest.QuestType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerQuestData {

    private final UUID playerUUID;
    private Quest dailyQuest;
    private Quest weeklyQuest;
    private Quest monthlyQuest;
    private final Map<QuestType, Integer> rerollsUsed;
    private final Map<QuestType, LocalDateTime> lastRerollTime;
    private int completedDailyQuests;
    private int completedWeeklyQuests;
    private LocalDateTime lastDailyReset;
    private LocalDateTime lastWeeklyReset;
    private LocalDateTime lastMonthlyReset;
    private long onlineTime; // in minutes
    private LocalDateTime lastActiveTime;

    public PlayerQuestData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.rerollsUsed = new HashMap<>();
        this.lastRerollTime = new HashMap<>();
        this.completedDailyQuests = 0;
        this.completedWeeklyQuests = 0;
        this.onlineTime = 0;
        this.lastActiveTime = LocalDateTime.now();

        // Initialize reroll counts
        for (QuestType type : QuestType.values()) {
            rerollsUsed.put(type, 0);
        }
    }

    // Quest management
    public void setQuest(Quest quest) {
        if (quest == null) {
            return;
        }

        switch (quest.getType()) {
            case DAILY:
                this.dailyQuest = quest;
                break;
            case WEEKLY:
                this.weeklyQuest = quest;
                break;
            case MONTHLY:
                this.monthlyQuest = quest;
                break;
        }
    }

    public Quest getQuest(QuestType type) {
        switch (type) {
            case DAILY:
                return dailyQuest;
            case WEEKLY:
                return weeklyQuest;
            case MONTHLY:
                return monthlyQuest;
            default:
                return null;
        }
    }

    public boolean hasActiveQuest(QuestType type) {
        Quest quest = getQuest(type);
        return quest != null && !quest.isRewardClaimed();
    }

    // Reroll management
    public boolean canReroll(QuestType type, boolean isPremium, boolean isDeluxe) {
        if (isDeluxe) {
            return true; // Unlimited rerolls for deluxe
        }

        if (isPremium) {
            return rerollsUsed.get(type) < 1; // One reroll per type for premium
        }

        return false; // No rerolls for regular players
    }

    public void useReroll(QuestType type) {
        rerollsUsed.put(type, rerollsUsed.get(type) + 1);
        lastRerollTime.put(type, LocalDateTime.now());
    }

    public void resetRerolls(QuestType type) {
        rerollsUsed.put(type, 0);
    }

    // Daily/Weekly quest completion tracking
    public void incrementCompletedDailyQuests() {
        completedDailyQuests++;
    }

    public void incrementCompletedWeeklyQuests() {
        completedWeeklyQuests++;
    }

    public void resetCompletedDailyQuests() {
        completedDailyQuests = 0;
    }

    public void resetCompletedWeeklyQuests() {
        completedWeeklyQuests = 0;
    }

    // Online time tracking
    public void updateOnlineTime() {
        LocalDateTime now = LocalDateTime.now();
        long minutesSinceLastUpdate = java.time.Duration.between(lastActiveTime, now).toMinutes();
        
        // Count every minute of online time, regardless of activity
        if (minutesSinceLastUpdate > 0) {
            onlineTime += minutesSinceLastUpdate;
        }

        lastActiveTime = now;
    }

    public long getOnlineTimeHours() {
        return onlineTime / 60;
    }

    public void resetOnlineTime() {
        onlineTime = 0;
    }
    
    public void setOnlineTime(long onlineTime) {
        this.onlineTime = onlineTime;
    }

    // Reset tracking
    public void updateDailyReset() {
        lastDailyReset = LocalDateTime.now();
        resetRerolls(QuestType.DAILY);
        if (dailyQuest != null && !dailyQuest.isRewardClaimed()) {
            // Remove unclaimed quest or completed quest with unclaimed reward
            // Note: !isRewardClaimed() will be true for both:
            // 1. Incomplete quests (which by definition have unclaimed rewards)
            // 2. Completed quests where the player hasn't claimed the reward
            dailyQuest = null;
        }
    }

    public void updateWeeklyReset() {
        lastWeeklyReset = LocalDateTime.now();
        resetRerolls(QuestType.WEEKLY);
        if (weeklyQuest != null && !weeklyQuest.isRewardClaimed()) {
            // Remove unclaimed quest or completed quest with unclaimed reward
            // Note: !isRewardClaimed() will be true for both:
            // 1. Incomplete quests (which by definition have unclaimed rewards)
            // 2. Completed quests where the player hasn't claimed the reward
            weeklyQuest = null;
        }
    }

    public void updateMonthlyReset() {
        lastMonthlyReset = LocalDateTime.now();
        resetRerolls(QuestType.MONTHLY);
        resetOnlineTime();
        resetCompletedDailyQuests();
        resetCompletedWeeklyQuests();
        if (monthlyQuest != null && !monthlyQuest.isRewardClaimed()) {
            // Remove unclaimed quest or completed quest with unclaimed reward
            // Note: !isRewardClaimed() will be true for both:
            // 1. Incomplete quests (which by definition have unclaimed rewards)
            // 2. Completed quests where the player hasn't claimed the reward
            monthlyQuest = null;
        }
    }

    // Getters
    public UUID getPlayerUUID() { return playerUUID; }
    public Quest getDailyQuest() { return dailyQuest; }
    public Quest getWeeklyQuest() { return weeklyQuest; }
    public Quest getMonthlyQuest() { return monthlyQuest; }
    public int getRerollsUsed(QuestType type) { return rerollsUsed.get(type); }
    public int getCompletedDailyQuests() { return completedDailyQuests; }
    public int getCompletedWeeklyQuests() { return completedWeeklyQuests; }
    public long getOnlineTime() { return onlineTime; }
    public LocalDateTime getLastActiveTime() { return lastActiveTime; }
    public LocalDateTime getLastDailyReset() { return lastDailyReset; }
    public LocalDateTime getLastWeeklyReset() { return lastWeeklyReset; }
    public LocalDateTime getLastMonthlyReset() { return lastMonthlyReset; }
}
