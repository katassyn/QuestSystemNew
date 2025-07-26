package org.maks.questsystem.managers;

import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.Quest.QuestType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RewardManager {

    private final QuestSystem plugin;

    public RewardManager(QuestSystem plugin) {
        this.plugin = plugin;
    }

    public void giveRewards(Player player, QuestType questType, int playerLevel) {
        String levelRange = getLevelRange(playerLevel);

        // Load rewards from database
        List<ItemStack> rewards = plugin.getQuestDatabase().loadRewards(questType.name(), levelRange);

        if (rewards.isEmpty()) {
            // No rewards configured - give default rewards
            giveDefaultRewards(player, questType, playerLevel);
            return;
        }

        // Give all reward items
        for (ItemStack reward : rewards) {
            if (reward != null) {
                // Try to add to inventory
                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(reward.clone());
                } else {
                    // Drop at player's feet if inventory is full
                    player.getWorld().dropItemNaturally(player.getLocation(), reward.clone());
                }
            }
        }
    }

    private void giveDefaultRewards(Player player, QuestType questType, int playerLevel) {
        // Default rewards based on quest type and level
        // This is a fallback if no rewards are configured

        // No default rewards - rewards should be configured through the GUI
        player.sendMessage(plugin.getConfigManager().getMessage("prefix") + 
            "No rewards configured for this quest type and level range.");
    }

    public String getLevelRange(int playerLevel) {
        if (playerLevel >= 1 && playerLevel <= 49) {
            return "1-49";
        } else if (playerLevel >= 50 && playerLevel <= 64) {
            return "50-64";
        } else if (playerLevel >= 65 && playerLevel <= 80) {
            return "65-80";
        } else {
            return "80+";
        }
    }
}
