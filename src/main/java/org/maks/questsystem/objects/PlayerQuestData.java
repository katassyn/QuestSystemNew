package org.maks.questsystem.objects;

import org.maks.questsystem.objects.Quest.QuestType;

import java.time.DayOfWeek;
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

    // Track when player last completed and claimed a quest
    private LocalDateTime lastDailyCompletion;
    private LocalDateTime lastWeeklyCompletion;
    private LocalDateTime lastMonthlyCompletion;
    private long onlineTime; // in minutes
    private LocalDateTime lastActiveTime;

    // Q1-Q10 completion tracking for each difficulty
    private final Map<String, Boolean> qInfCompleted; // Q1-Q10 completion status for INF
    private final Map<String, Boolean> qHellCompleted; // Q1-Q10 completion status for HELL
    private final Map<String, Boolean> qBloodCompleted; // Q1-Q10 completion status for BLOOD

    public PlayerQuestData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.rerollsUsed = new HashMap<>();
        this.lastRerollTime = new HashMap<>();
        this.completedDailyQuests = 0;
        this.completedWeeklyQuests = 0;
        this.onlineTime = 0;
        this.lastActiveTime = LocalDateTime.now();

        // Initialize Q1-Q10 completion tracking
        this.qInfCompleted = new HashMap<>();
        this.qHellCompleted = new HashMap<>();
        this.qBloodCompleted = new HashMap<>();

        // Initialize Q1-Q10 as not completed
        for (int i = 1; i <= 10; i++) {
            qInfCompleted.put(String.valueOf(i), false);
            qHellCompleted.put(String.valueOf(i), false);
            qBloodCompleted.put(String.valueOf(i), false);
        }

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

    // Add method to explicitly clear quest by type
    public void clearQuest(QuestType type) {
        switch (type) {
            case DAILY:
                this.dailyQuest = null;
                break;
            case WEEKLY:
                this.weeklyQuest = null;
                break;
            case MONTHLY:
                this.monthlyQuest = null;
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
        // A player has an active quest if:
        // 1. They have a quest AND
        // 2. The quest exists (not null)
        // Even if the reward is claimed, it still counts as having a quest for this period
        return quest != null;
    }

    public boolean canGetNewQuest(QuestType type, int resetHour, DayOfWeek weeklyResetDay) {
        // Check if player already has a quest (completed or not) for this period
        Quest existingQuest = getQuest(type);

        // If player has any quest (even completed), they can't get a new one until reset
        if (existingQuest != null) {
            return false;
        }

        // If no quest exists, player can get a new one
        return true;
    }

    public LocalDateTime getLastCompletion(QuestType type) {
        switch (type) {
            case DAILY:
                return lastDailyCompletion;
            case WEEKLY:
                return lastWeeklyCompletion;
            case MONTHLY:
                return lastMonthlyCompletion;
            default:
                return null;
        }
    }

    // Mark quest as completed and claimed
    public void markQuestCompleted(QuestType type) {
        LocalDateTime now = LocalDateTime.now();
        switch (type) {
            case DAILY:
                lastDailyCompletion = now;
                break;
            case WEEKLY:
                lastWeeklyCompletion = now;
                break;
            case MONTHLY:
                lastMonthlyCompletion = now;
                break;
        }
    }

    // Reroll management
    public boolean canReroll(QuestType type, boolean isPremium, boolean isDeluxe) {
        if (isDeluxe) {
            return true; // Unlimited rerolls for deluxe
        }

        if (isPremium) {
            return rerollsUsed.get(type) < 2; // Two rerolls per type for premium
        }

        return rerollsUsed.get(type) < 1; // One free reroll for regular players
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

    // Q1-Q10 completion tracking methods
    public void markQuestCompleted(String questNum, String difficulty) {
        Map<String, Boolean> targetMap = null;
        switch (difficulty.toUpperCase()) {
            case "INF":
                targetMap = qInfCompleted;
                break;
            case "HELL":
                targetMap = qHellCompleted;
                break;
            case "BLOOD":
                targetMap = qBloodCompleted;
                break;
        }

        if (targetMap != null && questNum != null) {
            targetMap.put(questNum, true);
        }
    }

    public boolean isAllQCompleted(String difficulty) {
        Map<String, Boolean> targetMap = null;
        switch (difficulty.toUpperCase()) {
            case "INF":
                targetMap = qInfCompleted;
                break;
            case "HELL":
                targetMap = qHellCompleted;
                break;
            case "BLOOD":
                targetMap = qBloodCompleted;
                break;
        }

        if (targetMap != null) {
            for (int i = 1; i <= 10; i++) {
                if (!targetMap.getOrDefault(String.valueOf(i), false)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void resetQCompletion(String difficulty) {
        Map<String, Boolean> targetMap = null;
        switch (difficulty.toUpperCase()) {
            case "INF":
                targetMap = qInfCompleted;
                break;
            case "HELL":
                targetMap = qHellCompleted;
                break;
            case "BLOOD":
                targetMap = qBloodCompleted;
                break;
        }

        if (targetMap != null) {
            for (int i = 1; i <= 10; i++) {
                targetMap.put(String.valueOf(i), false);
            }
        }
    }

    // Reset tracking
    public void updateDailyReset() {
        lastDailyReset = LocalDateTime.now();
        resetRerolls(QuestType.DAILY);
        // Always remove daily quest during reset, regardless of progress or claim status
        // This ensures fresh quests are available for all players at reset time
        dailyQuest = null;
    }

    public void updateWeeklyReset() {
        lastWeeklyReset = LocalDateTime.now();
        resetRerolls(QuestType.WEEKLY);
        // Always remove weekly quest during reset, regardless of progress or claim status
        // This ensures fresh quests are available for all players at reset time
        weeklyQuest = null;
    }

    public void updateMonthlyReset() {
        lastMonthlyReset = LocalDateTime.now();
        resetRerolls(QuestType.MONTHLY);
        resetOnlineTime();
        resetCompletedDailyQuests();
        resetCompletedWeeklyQuests();
        // Reset Q1-Q10 completion tracking for all difficulties
        resetQCompletion("INF");
        resetQCompletion("HELL");
        resetQCompletion("BLOOD");
        // Always remove monthly quest during reset, regardless of progress or claim status
        // This ensures fresh quests are available for all players at reset time
        monthlyQuest = null;
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
    public LocalDateTime getLastDailyCompletion() { return lastDailyCompletion; }
    public LocalDateTime getLastWeeklyCompletion() { return lastWeeklyCompletion; }
    public LocalDateTime getLastMonthlyCompletion() { return lastMonthlyCompletion; }

    // Setters for database loading
    public void setLastDailyReset(LocalDateTime lastDailyReset) { this.lastDailyReset = lastDailyReset; }
    public void setLastWeeklyReset(LocalDateTime lastWeeklyReset) { this.lastWeeklyReset = lastWeeklyReset; }
    public void setLastMonthlyReset(LocalDateTime lastMonthlyReset) { this.lastMonthlyReset = lastMonthlyReset; }
    public void setLastDailyCompletion(LocalDateTime lastDailyCompletion) { this.lastDailyCompletion = lastDailyCompletion; }
    public void setLastWeeklyCompletion(LocalDateTime lastWeeklyCompletion) { this.lastWeeklyCompletion = lastWeeklyCompletion; }
    public void setLastMonthlyCompletion(LocalDateTime lastMonthlyCompletion) { this.lastMonthlyCompletion = lastMonthlyCompletion; }

    // Q completion tracking getters
    public Map<String, Boolean> getQInfCompleted() { return qInfCompleted; }
    public Map<String, Boolean> getQHellCompleted() { return qHellCompleted; }
    public Map<String, Boolean> getQBloodCompleted() { return qBloodCompleted; }
}
