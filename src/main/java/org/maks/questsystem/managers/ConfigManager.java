package org.maks.questsystem.managers;

import org.maks.questsystem.QuestSystem;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    
    private final QuestSystem plugin;
    private final Map<String, String> messages = new HashMap<>();
    
    public ConfigManager(QuestSystem plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    
    private void loadMessages() {
        FileConfiguration config = plugin.getConfig();
        
        // Load all messages from config
        messages.put("prefix", translateColors(config.getString("messages.prefix", "&6[Quests] &r")));
        messages.put("quest_complete", translateColors(config.getString("messages.quest_complete")));
        messages.put("quest_progress", translateColors(config.getString("messages.quest_progress")));
        messages.put("daily_reset", translateColors(config.getString("messages.daily_reset")));
        messages.put("weekly_reset", translateColors(config.getString("messages.weekly_reset")));
        messages.put("monthly_reset", translateColors(config.getString("messages.monthly_reset")));
        messages.put("no_permission", translateColors(config.getString("messages.no_permission")));
        messages.put("reroll_used", translateColors(config.getString("messages.reroll_used")));
        messages.put("reroll_limit", translateColors(config.getString("messages.reroll_limit")));
        messages.put("reward_claimed", translateColors(config.getString("messages.reward_claimed")));
        messages.put("inventory_full", translateColors(config.getString("messages.inventory_full")));
    }
    
    public String getMessage(String key) {
        return messages.getOrDefault(key, "Message not found: " + key);
    }
    
    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return message;
    }
    
    private String translateColors(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public void reload() {
        plugin.reloadConfig();
        messages.clear();
        loadMessages();
    }
}