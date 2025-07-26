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
import java.util.*;

public class QuestDatabase {

    private final DatabaseManager databaseManager;

    public QuestDatabase(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
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
                "last_monthly_reset TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

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
        }
    }

    // Player data operations
    public void savePlayerData(PlayerQuestData data) {
        String sql = "INSERT INTO quest_player_data (player_uuid, completed_daily_quests, " +
                    "completed_weekly_quests, online_time, last_active, last_daily_reset, " +
                    "last_weekly_reset, last_monthly_reset) VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE completed_daily_quests = VALUES(completed_daily_quests), " +
                    "completed_weekly_quests = VALUES(completed_weekly_quests), " +
                    "online_time = VALUES(online_time), last_active = VALUES(last_active), " +
                    "last_daily_reset = VALUES(last_daily_reset), " +
                    "last_weekly_reset = VALUES(last_weekly_reset), " +
                    "last_monthly_reset = VALUES(last_monthly_reset)";

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

            ps.executeUpdate();

            // Save quests
            saveQuest(data.getPlayerUUID(), data.getDailyQuest());
            saveQuest(data.getPlayerUUID(), data.getWeeklyQuest());
            saveQuest(data.getPlayerUUID(), data.getMonthlyQuest());

            // Save rerolls
            saveRerolls(data);

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

                // Load quests
                data.setQuest(loadActiveQuest(playerUUID, QuestType.DAILY));
                data.setQuest(loadActiveQuest(playerUUID, QuestType.WEEKLY));
                data.setQuest(loadActiveQuest(playerUUID, QuestType.MONTHLY));

                // Load rerolls
                loadRerolls(data);
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

    public Quest loadActiveQuest(UUID playerUUID, QuestType type) {
        String sql = "SELECT * FROM quest_active_quests WHERE player_uuid = ? AND quest_type = ? " +
                    "AND reward_claimed = FALSE ORDER BY created_at DESC LIMIT 1";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, playerUUID.toString());
            ps.setString(2, type.name());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
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
                    rs.getBoolean("reward_claimed")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
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
