package org.maks.questsystem.database;

import org.maks.questsystem.objects.PlayerQuestData;
import org.maks.questsystem.objects.Quest;
import org.maks.questsystem.objects.Quest.QuestObjective;
import org.maks.questsystem.objects.Quest.QuestType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class QuestDatabase {

    private final DatabaseManager databaseManager;
    private org.maks.questsystem.QuestSystem plugin;

    public QuestDatabase(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.plugin = org.maks.questsystem.QuestSystem.getInstance();
    }

    public void createTables() throws SQLException {
        try (Connection conn = databaseManager.getConnection()) {
            // Player data table
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS quest_player_data (" +
                "player_uuid VARCHAR(36) PRIMARY KEY," +
                "completed_daily_quests INT DEFAULT 0," +
                "completed_weekly_quests INT DEFAULT 0," +
                "online_time BIGINT DEFAULT 0," +
                "last_active TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "last_daily_reset TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "last_weekly_reset TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "last_monthly_reset TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "last_daily_completion TIMESTAMP NULL," +
                "last_weekly_completion TIMESTAMP NULL," +
                "last_monthly_completion TIMESTAMP NULL" +
                ")"
            );

            // Add new columns if they don't exist (for existing databases)
            try {
                conn.createStatement().execute(
                    "ALTER TABLE quest_player_data ADD COLUMN last_daily_completion TIMESTAMP NULL"
                );
            } catch (SQLException e) {
                // Column already exists
            }
            try {
                conn.createStatement().execute(
                    "ALTER TABLE quest_player_data ADD COLUMN last_weekly_completion TIMESTAMP NULL"
                );
            } catch (SQLException e) {
                // Column already exists
            }
            try {
                conn.createStatement().execute(
                    "ALTER TABLE quest_player_data ADD COLUMN last_monthly_completion TIMESTAMP NULL"
                );
            } catch (SQLException e) {
                // Column already exists
            }

            // Quests table
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS quest_active_quests (" +
                "quest_id VARCHAR(36) PRIMARY KEY," +
                "player_uuid VARCHAR(36)," +
                "quest_type VARCHAR(20)," +
                "objective VARCHAR(50)," +
                "level_min INT," +
                "level_max INT," +
                "required_amount INT," +
                "current_progress INT DEFAULT 0," +
                "description TEXT," +
                "specific_target VARCHAR(100)," +
                "completed BOOLEAN DEFAULT FALSE," +
                "reward_claimed BOOLEAN DEFAULT FALSE," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "INDEX idx_player (player_uuid)," +
                "INDEX idx_type (quest_type)" +
                ")"
            );

            // Reroll tracking table
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS quest_rerolls (" +
                "player_uuid VARCHAR(36)," +
                "quest_type VARCHAR(20)," +
                "rerolls_used INT DEFAULT 0," +
                "last_reroll TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (player_uuid, quest_type)" +
                ")"
            );

            // Rewards table
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS quest_rewards (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "quest_type VARCHAR(20)," +
                "level_range VARCHAR(20)," +
                "items TEXT," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")"
            );

            // Quest completion history
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS quest_history (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "player_uuid VARCHAR(36)," +
                "quest_id VARCHAR(36)," +
                "quest_type VARCHAR(20)," +
                "completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "INDEX idx_player_history (player_uuid)" +
                ")"
            );

            // Q1-Q10 completion tracking
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS quest_q_completion (" +
                "player_uuid VARCHAR(36)," +
                "difficulty VARCHAR(10)," +
                "quest_num VARCHAR(2)," +
                "completed BOOLEAN DEFAULT FALSE," +
                "PRIMARY KEY (player_uuid, difficulty, quest_num)" +
                ")"
            );
        }
    }

    // Player data operations
    public void savePlayerData(PlayerQuestData data) {
        String sql = "INSERT INTO quest_player_data (player_uuid, completed_daily_quests, " +
                    "completed_weekly_quests, online_time, last_active, last_daily_reset, " +
                    "last_weekly_reset, last_monthly_reset, last_daily_completion, " +
                    "last_weekly_completion, last_monthly_completion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE completed_daily_quests = VALUES(completed_daily_quests), " +
                    "completed_weekly_quests = VALUES(completed_weekly_quests), " +
                    "online_time = VALUES(online_time), last_active = VALUES(last_active), " +
                    "last_daily_reset = VALUES(last_daily_reset), " +
                    "last_weekly_reset = VALUES(last_weekly_reset), " +
                    "last_monthly_reset = VALUES(last_monthly_reset), " +
                    "last_daily_completion = VALUES(last_daily_completion), " +
                    "last_weekly_completion = VALUES(last_weekly_completion), " +
                    "last_monthly_completion = VALUES(last_monthly_completion)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, data.getPlayerUUID().toString());
            ps.setInt(2, data.getCompletedDailyQuests());
            ps.setInt(3, data.getCompletedWeeklyQuests());
            ps.setLong(4, data.getOnlineTime());
            ps.setTimestamp(5, Timestamp.valueOf(data.getLastActiveTime()));
            ps.setTimestamp(6, data.getLastDailyReset() != null ?
                Timestamp.valueOf(data.getLastDailyReset()) : null);
            ps.setTimestamp(7, data.getLastWeeklyReset() != null ?
                Timestamp.valueOf(data.getLastWeeklyReset()) : null);
            ps.setTimestamp(8, data.getLastMonthlyReset() != null ?
                Timestamp.valueOf(data.getLastMonthlyReset()) : null);
            ps.setTimestamp(9, data.getLastDailyCompletion() != null ?
                Timestamp.valueOf(data.getLastDailyCompletion()) : null);
            ps.setTimestamp(10, data.getLastWeeklyCompletion() != null ?
                Timestamp.valueOf(data.getLastWeeklyCompletion()) : null);
            ps.setTimestamp(11, data.getLastMonthlyCompletion() != null ?
                Timestamp.valueOf(data.getLastMonthlyCompletion()) : null);

            ps.executeUpdate();

            // Save quests
            saveQuest(data.getPlayerUUID(), data.getDailyQuest());
            saveQuest(data.getPlayerUUID(), data.getWeeklyQuest());
            saveQuest(data.getPlayerUUID(), data.getMonthlyQuest());

            // Save rerolls
            saveRerolls(data);

            // Save Q completion tracking
            saveQCompletions(data);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PlayerQuestData loadPlayerData(UUID playerUUID) {
        PlayerQuestData data = new PlayerQuestData(playerUUID);

        String sql = "SELECT * FROM quest_player_data WHERE player_uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, playerUUID.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Load basic data
                int completedDaily = rs.getInt("completed_daily_quests");
                int completedWeekly = rs.getInt("completed_weekly_quests");
                long onlineTime = rs.getLong("online_time");

                // Set data using setters in PlayerQuestData
                data.setOnlineTime(onlineTime);

                for (int i = 0; i < completedDaily; i++) {
                    data.incrementCompletedDailyQuests();
                }
                for (int i = 0; i < completedWeekly; i++) {
                    data.incrementCompletedWeeklyQuests();
                }

                // Load reset timestamps
                Timestamp dailyReset = rs.getTimestamp("last_daily_reset");
                if (dailyReset != null) {
                    data.setLastDailyReset(dailyReset.toLocalDateTime());
                }
                Timestamp weeklyReset = rs.getTimestamp("last_weekly_reset");
                if (weeklyReset != null) {
                    data.setLastWeeklyReset(weeklyReset.toLocalDateTime());
                }
                Timestamp monthlyReset = rs.getTimestamp("last_monthly_reset");
                if (monthlyReset != null) {
                    data.setLastMonthlyReset(monthlyReset.toLocalDateTime());
                }

                // Load completion timestamps
                Timestamp dailyCompletion = rs.getTimestamp("last_daily_completion");
                if (dailyCompletion != null) {
                    data.setLastDailyCompletion(dailyCompletion.toLocalDateTime());
                }
                Timestamp weeklyCompletion = rs.getTimestamp("last_weekly_completion");
                if (weeklyCompletion != null) {
                    data.setLastWeeklyCompletion(weeklyCompletion.toLocalDateTime());
                }
                Timestamp monthlyCompletion = rs.getTimestamp("last_monthly_completion");
                if (monthlyCompletion != null) {
                    data.setLastMonthlyCompletion(monthlyCompletion.toLocalDateTime());
                }

                // Load quests - clear cache if quest is null (expired)
                Quest dailyQuest = loadActiveQuest(playerUUID, QuestType.DAILY);
                if (dailyQuest != null) {
                    data.setQuest(dailyQuest);
                } else {
                    data.clearQuest(QuestType.DAILY);
                }

                Quest weeklyQuest = loadActiveQuest(playerUUID, QuestType.WEEKLY);
                if (weeklyQuest != null) {
                    data.setQuest(weeklyQuest);
                } else {
                    data.clearQuest(QuestType.WEEKLY);
                }

                Quest monthlyQuest = loadActiveQuest(playerUUID, QuestType.MONTHLY);
                if (monthlyQuest != null) {
                    data.setQuest(monthlyQuest);
                } else {
                    data.clearQuest(QuestType.MONTHLY);
                }

                // Load rerolls
                loadRerolls(data);

                // Load Q completion tracking
                loadQCompletions(data);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }

    public List<PlayerQuestData> loadAllPlayerData() {
        List<PlayerQuestData> allData = new ArrayList<>();

        String sql = "SELECT DISTINCT player_uuid FROM quest_player_data";
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));
                PlayerQuestData data = loadPlayerData(playerUUID);
                if (data != null) {
                    allData.add(data);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return allData;
    }

    // Quest operations
    public void saveQuest(UUID playerUUID, Quest quest) {
        if (quest == null) {
            return;
        }

        String sql = "INSERT INTO quest_active_quests (quest_id, player_uuid, quest_type, " +
                    "objective, level_min, level_max, required_amount, current_progress, " +
                    "description, specific_target, completed, reward_claimed) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE current_progress = VALUES(current_progress), " +
                    "completed = VALUES(completed), reward_claimed = VALUES(reward_claimed)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, quest.getQuestId().toString());
            ps.setString(2, playerUUID.toString());
            ps.setString(3, quest.getType().name());
            ps.setString(4, quest.getObjective().name());
            ps.setInt(5, quest.getLevelMin());
            ps.setInt(6, quest.getLevelMax());
            ps.setInt(7, quest.getRequiredAmount());
            ps.setInt(8, quest.getCurrentProgress());
            ps.setString(9, quest.getDescription());
            ps.setString(10, quest.getSpecificTarget());
            ps.setBoolean(11, quest.isCompleted());
            ps.setBoolean(12, quest.isRewardClaimed());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void expireQuest(UUID playerUUID, QuestType type) {
        // Instead of just marking as reward_claimed, delete the quest entirely
        String sql = "DELETE FROM quest_active_quests WHERE player_uuid = ? AND quest_type = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, playerUUID.toString());
            ps.setString(2, type.name());

            int deleted = ps.executeUpdate();
            if (deleted > 0 && plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().info("Expired and deleted " + type.name() + " quest for player " + playerUUID);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes all expired quests for a player (alternative approach)
     */
    public void removeExpiredQuests(UUID playerUUID, QuestType type) {
        String sql = "DELETE FROM quest_active_quests " +
                    "WHERE player_uuid = ? AND quest_type = ? AND reward_claimed = FALSE";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, playerUUID.toString());
            ps.setString(2, type.name());

            int deleted = ps.executeUpdate();
            if (deleted > 0 && plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().info("Deleted " + deleted + " expired " + type.name() + " quest(s) for player " + playerUUID);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void resetAllQuestsOfType(QuestType type) {
        // Delete all quests of this type for all players
        String sql = "DELETE FROM quest_active_quests WHERE quest_type = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type.name());

            int deleted = ps.executeUpdate();
            plugin.getLogger().info("Reset " + deleted + " " + type.name() + " quests for all players");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load active quest with reset time checking
     */
    public Quest loadActiveQuest(UUID playerUUID, QuestType type) {
        // Load the most recent quest regardless of reward_claimed status
        String sql = "SELECT q.*, p.last_daily_reset, p.last_weekly_reset, p.last_monthly_reset " +
                    "FROM quest_active_quests q " +
                    "JOIN quest_player_data p ON q.player_uuid = p.player_uuid " +
                    "WHERE q.player_uuid = ? AND q.quest_type = ? " +
                    "ORDER BY q.created_at DESC LIMIT 1";  // Removed reward_claimed filter

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, playerUUID.toString());
            ps.setString(2, type.name());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Timestamp questCreated = rs.getTimestamp("created_at");
                Timestamp lastReset = null;
                boolean rewardClaimed = rs.getBoolean("reward_claimed");

                // Get the appropriate reset time
                switch (type) {
                    case DAILY:
                        lastReset = rs.getTimestamp("last_daily_reset");
                        break;
                    case WEEKLY:
                        lastReset = rs.getTimestamp("last_weekly_reset");
                        break;
                    case MONTHLY:
                        lastReset = rs.getTimestamp("last_monthly_reset");
                        break;
                }

                // Check if quest should be expired
                boolean shouldExpire = false;
                LocalDateTime now = LocalDateTime.now();

                if (questCreated != null) {
                    LocalDateTime questCreatedTime = questCreated.toLocalDateTime();
                    int resetHour = plugin.getConfig().getInt("quests.daily_reset_hour", 0);

                    switch (type) {
                        case DAILY:
                            LocalDateTime todayResetTime = now.toLocalDate().atTime(resetHour, 0);

                            // Quest should expire if it was created before today's reset time and we're past it
                            if (questCreatedTime.isBefore(todayResetTime) && now.isAfter(todayResetTime)) {
                                shouldExpire = true;
                            }
                            // Or if quest was created on a previous day and we're past reset hour
                            else if (questCreatedTime.toLocalDate().isBefore(now.toLocalDate()) &&
                                    now.toLocalTime().getHour() >= resetHour) {
                                shouldExpire = true;
                            }
                            break;

                        case WEEKLY:
                            DayOfWeek resetDay = DayOfWeek.valueOf(
                                plugin.getConfig().getString("quests.weekly_reset_day", "SUNDAY")
                            );
                            LocalDateTime lastSundayMidnight = now.with(TemporalAdjusters.previousOrSame(resetDay))
                                                                  .withHour(0).withMinute(0).withSecond(0);

                            if (questCreatedTime.isBefore(lastSundayMidnight) && now.isAfter(lastSundayMidnight)) {
                                shouldExpire = true;
                            }
                            break;

                        case MONTHLY:
                            // Check if quest was created in a previous month
                            if (questCreatedTime.getMonth() != now.getMonth() ||
                                questCreatedTime.getYear() != now.getYear()) {
                                shouldExpire = true;
                            }
                            break;
                    }
                }

                if (shouldExpire) {
                    // Quest is expired, delete it from database
                    deleteExpiredQuest(playerUUID, type);
                    if (plugin.getConfig().getBoolean("debug", false)) {
                        plugin.getLogger().info("Quest expired and deleted for player " + playerUUID +
                                              " (type: " + type + ", created: " + questCreated + ")");
                    }
                    return null;
                }

                // If quest is completed and reward claimed, but not expired,
                // we still return it so the system knows the player already has a quest for this period
                if (plugin.getConfig().getBoolean("debug", false)) {
                    plugin.getLogger().info("Loading quest for player " + playerUUID + " (type: " + type + "):");
                    plugin.getLogger().info("  - Quest ID: " + rs.getString("quest_id"));
                    plugin.getLogger().info("  - Created at: " + questCreated);
                    plugin.getLogger().info("  - Completed: " + rs.getBoolean("completed"));
                    plugin.getLogger().info("  - Reward claimed: " + rewardClaimed);
                }

                return new Quest(
                    UUID.fromString(rs.getString("quest_id")),
                    QuestType.valueOf(rs.getString("quest_type")),
                    QuestObjective.valueOf(rs.getString("objective")),
                    rs.getInt("level_min"),
                    rs.getInt("level_max"),
                    rs.getInt("required_amount"),
                    rs.getInt("current_progress"),
                    rs.getString("description"),
                    rs.getString("specific_target"),
                    rs.getBoolean("completed"),
                    rewardClaimed
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Add new method to properly delete expired quests
    public void deleteExpiredQuest(UUID playerUUID, QuestType type) {
        String sql = "DELETE FROM quest_active_quests WHERE player_uuid = ? AND quest_type = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, playerUUID.toString());
            ps.setString(2, type.name());

            int deleted = ps.executeUpdate();
            if (deleted > 0 && plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().info("Deleted expired " + type.name() + " quest for player " + playerUUID);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Reroll operations
    private void saveRerolls(PlayerQuestData data) {
        String sql = "INSERT INTO quest_rerolls (player_uuid, quest_type, rerolls_used) " +
                    "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE rerolls_used = VALUES(rerolls_used)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (QuestType type : QuestType.values()) {
                ps.setString(1, data.getPlayerUUID().toString());
                ps.setString(2, type.name());
                ps.setInt(3, data.getRerollsUsed(type));
                ps.addBatch();
            }

            ps.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRerolls(PlayerQuestData data) {
        String sql = "SELECT * FROM quest_rerolls WHERE player_uuid = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, data.getPlayerUUID().toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                QuestType type = QuestType.valueOf(rs.getString("quest_type"));
                int rerollsUsed = rs.getInt("rerolls_used");

                // Use rerolls to match the saved count
                for (int i = 0; i < rerollsUsed; i++) {
                    data.useReroll(type);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Q completion tracking operations
    private void saveQCompletions(PlayerQuestData data) {
        String sql = "INSERT INTO quest_q_completion (player_uuid, difficulty, quest_num, completed) " +
                    "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE completed = VALUES(completed)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Save INF completions
            for (Map.Entry<String, Boolean> entry : data.getQInfCompleted().entrySet()) {
                ps.setString(1, data.getPlayerUUID().toString());
                ps.setString(2, "INF");
                ps.setString(3, entry.getKey());
                ps.setBoolean(4, entry.getValue());
                ps.addBatch();
            }

            // Save HELL completions
            for (Map.Entry<String, Boolean> entry : data.getQHellCompleted().entrySet()) {
                ps.setString(1, data.getPlayerUUID().toString());
                ps.setString(2, "HELL");
                ps.setString(3, entry.getKey());
                ps.setBoolean(4, entry.getValue());
                ps.addBatch();
            }

            // Save BLOOD completions
            for (Map.Entry<String, Boolean> entry : data.getQBloodCompleted().entrySet()) {
                ps.setString(1, data.getPlayerUUID().toString());
                ps.setString(2, "BLOOD");
                ps.setString(3, entry.getKey());
                ps.setBoolean(4, entry.getValue());
                ps.addBatch();
            }

            ps.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadQCompletions(PlayerQuestData data) {
        String sql = "SELECT * FROM quest_q_completion WHERE player_uuid = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, data.getPlayerUUID().toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String difficulty = rs.getString("difficulty");
                String questNum = rs.getString("quest_num");
                boolean completed = rs.getBoolean("completed");

                if (completed) {
                    data.markQuestCompleted(questNum, difficulty);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Reward operations
    public void saveRewards(String questType, String levelRange, List<ItemStack> items) {
        String sql = "INSERT INTO quest_rewards (quest_type, level_range, items) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE items = VALUES(items)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, questType);
            ps.setString(2, levelRange);
            ps.setString(3, itemStackListToBase64(items));

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ItemStack> loadRewards(String questType, String levelRange) {
        String sql = "SELECT items FROM quest_rewards WHERE quest_type = ? AND level_range = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, questType);
            ps.setString(2, levelRange);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return itemStackListFromBase64(rs.getString("items"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    // Utility methods for item serialization
    /**
     * Converts a list of ItemStack objects to a Base64 encoded string.
     * This method preserves all item attributes, including:
     * - Item type and amount
     * - Item metadata (display name, lore, enchantments)
     * - Custom model data
     * - Item flags
     * - Attribute modifiers
     * - NBT data
     * 
     * @param items The list of ItemStack objects to serialize
     * @return A Base64 encoded string representing the items
     */
    private String itemStackListToBase64(List<ItemStack> items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the list
            dataOutput.writeInt(items.size());

            // Write each item in the list
            for (ItemStack item : items) {
                // Handle null items gracefully
                if (item == null) {
                    dataOutput.writeObject(null);
                } else {
                    // Clone the item to avoid modifying the original
                    ItemStack clonedItem = item.clone();
                    dataOutput.writeObject(clonedItem);
                }
            }

            // Close the output stream
            dataOutput.close();

            // Encode the byte array to Base64
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error serializing item list: " + e.getMessage());
            return "";
        }
    }

    /**
     * Converts a Base64 encoded string back to a list of ItemStack objects.
     * This method restores all item attributes that were serialized.
     * 
     * @param data The Base64 encoded string to deserialize
     * @return A list of ItemStack objects
     */
    private List<ItemStack> itemStackListFromBase64(String data) {
        List<ItemStack> items = new ArrayList<>();

        // Handle empty or null data
        if (data == null || data.isEmpty()) {
            return items;
        }

        try {
            // Decode the Base64 string to a byte array
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            // Read the size of the list
            int size = dataInput.readInt();

            // Read each item in the list
            for (int i = 0; i < size; i++) {
                try {
                    // Read the item and add it to the list
                    ItemStack item = (ItemStack) dataInput.readObject();
                    if (item != null) {
                        // Clone the item to ensure we have a fresh copy
                        items.add(item.clone());
                    }
                } catch (Exception e) {
                    System.err.println("Error deserializing item at index " + i + ": " + e.getMessage());
                    e.printStackTrace();
                    // Continue with the next item
                }
            }

            // Close the input stream
            dataInput.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error deserializing item list: " + e.getMessage());
        }

        return items;
    }

}
