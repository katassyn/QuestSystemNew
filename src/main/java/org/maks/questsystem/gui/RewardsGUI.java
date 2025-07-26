package org.maks.questsystem.gui;

import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.Quest.QuestType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RewardsGUI implements Listener {

    private final QuestSystem plugin;
    private final Map<UUID, RewardSession> sessions = new HashMap<>();

    public RewardsGUI(QuestSystem plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openMainMenu(Player player) {
        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info("[RewardsGUI] Opening main menu for player: " + player.getName());
        }

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "Quest Rewards Configuration");

        // Daily rewards button
        ItemStack daily = new ItemStack(Material.SUNFLOWER);
        ItemMeta dailyMeta = daily.getItemMeta();
        dailyMeta.setDisplayName(ChatColor.GOLD + "Daily Quest Rewards");
        dailyMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Configure rewards for",
            ChatColor.GRAY + "daily quests."
        ));
        daily.setItemMeta(dailyMeta);
        inv.setItem(11, daily);

        // Weekly rewards button
        ItemStack weekly = new ItemStack(Material.EMERALD);
        ItemMeta weeklyMeta = weekly.getItemMeta();
        weeklyMeta.setDisplayName(ChatColor.GREEN + "Weekly Quest Rewards");
        weeklyMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Configure rewards for",
            ChatColor.GRAY + "weekly quests."
        ));
        weekly.setItemMeta(weeklyMeta);
        inv.setItem(13, weekly);

        // Monthly rewards button
        ItemStack monthly = new ItemStack(Material.DIAMOND);
        ItemMeta monthlyMeta = monthly.getItemMeta();
        monthlyMeta.setDisplayName(ChatColor.AQUA + "Monthly Quest Rewards");
        monthlyMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Configure rewards for",
            ChatColor.GRAY + "monthly quests."
        ));
        monthly.setItemMeta(monthlyMeta);
        inv.setItem(15, monthly);

        // Update session BEFORE opening inventory
        RewardSession newSession = new RewardSession(inv, null, null);
        sessions.put(player.getUniqueId(), newSession);

        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info("[RewardsGUI] Session updated for main menu, player: " + player.getName());
        }

        // Add a small delay to prevent conflicts with other plugins
        new BukkitRunnable() {
            @Override
            public void run() {
                player.openInventory(inv);
            }
        }.runTaskLater(plugin, 1L);
    }

    public void openLevelRangeMenu(Player player, QuestType type) {
        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info("[RewardsGUI] Opening level range menu for player: " + player.getName() + 
                                   ", quest type: " + type);
        }

        if (type == null) {
            plugin.getLogger().warning("[RewardsGUI] Attempted to open level range menu with null quest type for player: " + player.getName());
            return;
        }

        String title = ChatColor.DARK_PURPLE + type.name() + " Rewards - Select Level Range";
        Inventory inv = Bukkit.createInventory(null, 27, title);

        String[] ranges = {"1-49", "50-64", "65-80", "80+"};
        Material[] materials = {Material.LEATHER_CHESTPLATE, Material.IRON_CHESTPLATE, 
                               Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE};
        int[] slots = {10, 12, 14, 16};

        for (int i = 0; i < ranges.length; i++) {
            ItemStack item = new ItemStack(materials[i]);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Level " + ranges[i]);

            // Load current rewards count for this range
            List<ItemStack> rewards = plugin.getQuestDatabase().loadRewards(type.name(), ranges[i]);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to view/edit rewards");
            lore.add(ChatColor.GRAY + "for level " + ranges[i] + " players.");
            lore.add("");
            lore.add(ChatColor.AQUA + "Current rewards: " + ChatColor.WHITE + rewards.size() + " items");

            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slots[i], item);
        }

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        back.setItemMeta(backMeta);
        inv.setItem(22, back);

        // Update session BEFORE opening inventory
        RewardSession newSession = new RewardSession(inv, type, null);
        sessions.put(player.getUniqueId(), newSession);

        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info("[RewardsGUI] Session updated for level range menu, player: " + player.getName() + 
                                   ", quest type: " + type);
        }

        // Add a small delay to prevent conflicts with other plugins
        new BukkitRunnable() {
            @Override
            public void run() {
                player.openInventory(inv);
            }
        }.runTaskLater(plugin, 1L);
    }

    public void openRewardEditor(Player player, QuestType type, String levelRange) {
        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info("[RewardsGUI] openRewardEditor called for player: " + player.getName() + 
                                   ", type: " + type + ", levelRange: " + levelRange);
        }

        if (type == null) {
            plugin.getLogger().warning("[RewardsGUI] Attempted to open reward editor with null quest type for player: " + player.getName());
            return;
        }

        boolean isAdmin = player.hasPermission("questsystem.admin");
        String titlePrefix = isAdmin ? "Edit " : "View ";
        String title = ChatColor.DARK_PURPLE + titlePrefix + type.name() + " Rewards - Level " + levelRange;

        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Load existing rewards
        List<ItemStack> rewards = plugin.getQuestDatabase().loadRewards(type.name(), levelRange);

        // Add rewards to inventory
        for (int i = 0; i < Math.min(rewards.size(), 45); i++) {
            inv.setItem(i, rewards.get(i));
        }

        // Info item
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();

        if (isAdmin) {
            infoMeta.setDisplayName(ChatColor.YELLOW + "Reward Editor");
            infoMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Place items in the slots above",
                ChatColor.GRAY + "to set them as quest rewards.",
                "",
                ChatColor.GREEN + "Items will be saved when",
                ChatColor.GREEN + "you close this menu.",
                "",
                ChatColor.AQUA + "You can use any items including",
                ChatColor.AQUA + "armor stands or custom items!",
                "",
                ChatColor.GOLD + "All item properties will be preserved:",
                ChatColor.GOLD + "- Names with colors",
                ChatColor.GOLD + "- Lore text",
                ChatColor.GOLD + "- Enchantments",
                ChatColor.GOLD + "- Custom attributes"
            ));
        } else {
            infoMeta.setDisplayName(ChatColor.YELLOW + "Quest Rewards");
            infoMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "These are the possible rewards",
                ChatColor.GRAY + "for completing " + type.name().toLowerCase() + " quests",
                ChatColor.GRAY + "at level " + levelRange + ".",
                "",
                ChatColor.AQUA + "Complete quests to earn",
                ChatColor.AQUA + "these rewards!"
            ));
        }

        info.setItemMeta(infoMeta);
        inv.setItem(49, info);

        // Save button (only for admins)
        if (isAdmin) {
            ItemStack save = new ItemStack(Material.EMERALD);
            ItemMeta saveMeta = save.getItemMeta();
            saveMeta.setDisplayName(ChatColor.GREEN + "Save Rewards");
            saveMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click to save all rewards",
                ChatColor.GRAY + "and return to the previous menu."
            ));
            save.setItemMeta(saveMeta);
            inv.setItem(52, save);
        }

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        backMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Return to the previous menu"
        ));
        back.setItemMeta(backMeta);
        inv.setItem(53, back);

        // Update session BEFORE opening inventory
        RewardSession newSession = new RewardSession(inv, type, levelRange);
        sessions.put(player.getUniqueId(), newSession);

        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info("[RewardsGUI] Session updated for player: " + player.getName() + 
                                   ", type: " + type + ", levelRange: " + levelRange);
        }

        // Add a small delay to prevent conflicts with other plugins
        new BukkitRunnable() {
            @Override
            public void run() {
                player.openInventory(inv);
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String clickedTitle = event.getView().getTitle();

        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info("[RewardsGUI] Click detected - Player: " + player.getName() + 
                                   ", Title: " + clickedTitle + ", Slot: " + event.getSlot());
        }

        RewardSession session = sessions.get(player.getUniqueId());

        if (session == null) {
            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().info("[RewardsGUI] No session found for player: " + player.getName());
            }
            return;
        }

        // Check if this is one of our GUIs by title
        boolean isOurGUI = isOurGUI(clickedTitle);

        if (!isOurGUI) {
            return;
        }

        // Additional safety check
        if (session.type == null && !clickedTitle.equals(ChatColor.DARK_PURPLE + "Quest Rewards Configuration")) {
            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().warning("[RewardsGUI] Session type is null but GUI title doesn't match main menu!");
            }
            return;
        }

        boolean isAdmin = player.hasPermission("questsystem.admin");

        // Handle clicks based on which menu they're in
        if (session.type == null) {
            // Main menu - always cancel clicks
            event.setCancelled(true);

            switch (event.getSlot()) {
                case 11:
                    openLevelRangeMenu(player, QuestType.DAILY);
                    break;
                case 13:
                    openLevelRangeMenu(player, QuestType.WEEKLY);
                    break;
                case 15:
                    openLevelRangeMenu(player, QuestType.MONTHLY);
                    break;
            }
        }
        else if (session.levelRange == null) {
            // Level range menu - always cancel clicks
            event.setCancelled(true);

            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().info("[RewardsGUI] Level range menu click - slot: " + event.getSlot() + 
                                       ", player: " + player.getName() + 
                                       ", quest type: " + session.type);
            }

            String levelRange = null;
            switch (event.getSlot()) {
                case 10:
                    levelRange = "1-49";
                    break;
                case 12:
                    levelRange = "50-64";
                    break;
                case 14:
                    levelRange = "65-80";
                    break;
                case 16:
                    levelRange = "80+";
                    break;
                case 22:
                    openMainMenu(player);
                    return;
            }

            if (levelRange != null) {
                if (plugin.getConfig().getBoolean("debug", false)) {
                    plugin.getLogger().info("[RewardsGUI] Opening reward editor for level range: " + 
                                           levelRange + ", quest type: " + session.type);
                }
                openRewardEditor(player, session.type, levelRange);
            }
        }
        else {
            // Reward editor
            int slot = event.getSlot();

            // For reward slots (0-44)
            if (slot < 45) {
                if (isAdmin) {
                    // Admins can edit - don't cancel the event
                    return;
                } else {
                    // Non-admins can only view - cancel the event
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.YELLOW + "You can view the rewards, but only admins can edit them.");
                }
            }
            // For bottom row (45-53)
            else {
                // Always cancel clicks in bottom row
                event.setCancelled(true);

                // Handle save button (admins only)
                if (slot == 52 && isAdmin) {
                    saveRewards(player, event.getInventory(), session);
                    openLevelRangeMenu(player, session.type);
                }
                // Handle back button
                else if (slot == 53) {
                    if (isAdmin && session.levelRange != null) {
                        player.sendMessage(ChatColor.YELLOW + "Changes not saved. Use the " + 
                                         ChatColor.GREEN + "Save Rewards" + ChatColor.YELLOW + 
                                         " button to save your changes.");
                    }
                    openLevelRangeMenu(player, session.type);
                }
            }
        }
    }

    /**
     * Saves the rewards from the inventory to the database
     */
    private void saveRewards(Player player, Inventory inventory, RewardSession session) {
        if (session != null && session.levelRange != null) {
            if (!player.hasPermission("questsystem.admin")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to save quest rewards.");
                return;
            }

            List<ItemStack> rewards = new ArrayList<>();

            for (int i = 0; i < 45; i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    rewards.add(item.clone());
                }
            }

            plugin.getQuestDatabase().saveRewards(session.type.name(), session.levelRange, rewards);
            player.sendMessage(ChatColor.GREEN + "Rewards saved for " + session.type.name() + 
                             " quests, level " + session.levelRange);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info("[RewardsGUI] InventoryCloseEvent - Title: " + title + ", Player: " + player.getName());
        }

        // Don't remove the session immediately - it might be a menu transition
        // Schedule a delayed task to check if the player has opened another inventory
        new BukkitRunnable() {
            @Override
            public void run() {
                // Only remove the session if the player is not in an inventory
                if (!player.getOpenInventory().getTitle().equals(title)) {
                    RewardSession session = sessions.get(player.getUniqueId());

                    if (session != null && !isOurGUI(player.getOpenInventory().getTitle())) {
                        sessions.remove(player.getUniqueId());

                        if (plugin.getConfig().getBoolean("debug", false)) {
                            plugin.getLogger().info("[RewardsGUI] Session removed for player: " + player.getName());
                        }
                    }
                }
            }
        }.runTaskLater(plugin, 2L);
    }

    // Helper method to check if a title belongs to one of our GUIs
    private boolean isOurGUI(String title) {
        return title.equals(ChatColor.DARK_PURPLE + "Quest Rewards Configuration") ||
               title.startsWith(ChatColor.DARK_PURPLE + "DAILY Rewards") ||
               title.startsWith(ChatColor.DARK_PURPLE + "WEEKLY Rewards") ||
               title.startsWith(ChatColor.DARK_PURPLE + "MONTHLY Rewards") ||
               title.startsWith(ChatColor.DARK_PURPLE + "Edit ") ||
               title.startsWith(ChatColor.DARK_PURPLE + "View ");
    }

    private static class RewardSession {
        final Inventory inventory;
        final QuestType type;
        final String levelRange;

        RewardSession(Inventory inventory, QuestType type, String levelRange) {
            this.inventory = inventory;
            this.type = type;
            this.levelRange = levelRange;
        }
    }
}
