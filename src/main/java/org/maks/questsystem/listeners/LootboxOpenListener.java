package org.maks.questsystem.listeners;

import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.Quest;
import org.maks.questsystem.objects.Quest.QuestObjective;
import org.maks.questsystem.objects.PlayerQuestData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class LootboxOpenListener implements Listener {

    private final QuestSystem plugin;

    public LootboxOpenListener(QuestSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if right-click with item
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) {
            return;
        }

        // Get player quest data
        PlayerQuestData playerData = plugin.getQuestManager().getPlayerData(player.getUniqueId());

        // Check if player has any lootbox-related quests before proceeding
        boolean hasLootboxQuest = false;

        // Check daily quest
        Quest dailyQuest = playerData.getDailyQuest();
        if (dailyQuest != null && !dailyQuest.isCompleted() && 
            dailyQuest.getObjective() == QuestObjective.OPEN_LOOTBOXES) {
            hasLootboxQuest = true;
        }

        // Check weekly quest
        if (!hasLootboxQuest) {
            Quest weeklyQuest = playerData.getWeeklyQuest();
            if (weeklyQuest != null && !weeklyQuest.isCompleted() && 
                weeklyQuest.getObjective() == QuestObjective.OPEN_LOOTBOXES) {
                hasLootboxQuest = true;
            }
        }

        // Check monthly quest
        if (!hasLootboxQuest) {
            Quest monthlyQuest = playerData.getMonthlyQuest();
            if (monthlyQuest != null && !monthlyQuest.isCompleted() && 
                monthlyQuest.getObjective() == QuestObjective.OPEN_LOOTBOXES) {
                hasLootboxQuest = true;
            }
        }

        // If player doesn't have any lootbox quests, skip the check
        if (!hasLootboxQuest) {
            return;
        }

        // Check if item is a lootbox
        if (isLootbox(item)) {
            // Update quest progress
            plugin.getQuestManager().updateProgress(player, QuestObjective.OPEN_LOOTBOXES, 1, null);
        }
    }

    private boolean isLootbox(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) {
            return false;
        }

        String itemName = ChatColor.stripColor(meta.getDisplayName());
        List<String> lootboxes = plugin.getConfig().getStringList("lootboxes");

        for (String lootbox : lootboxes) {
            if (itemName.toLowerCase().contains(lootbox.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}
