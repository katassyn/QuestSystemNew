package org.maks.questsystem.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.Quest.QuestObjective;

/**
 * Listener for MineSystem plugin integration
 * Tracks: ore mining hits, sphere completions, pickaxe usage, tier-specific mining
 */
public class MineSystemListener implements Listener {

    private final QuestSystem plugin;

    // Tier mapping for ores
    private static final java.util.Map<String, Integer> ORE_TIERS = java.util.Map.ofEntries(
            // Tier 1
            java.util.Map.entry("Hematite", 1),
            java.util.Map.entry("Magnetite", 1),
            java.util.Map.entry("Azurite", 1),
            java.util.Map.entry("Carnelian", 1),
            java.util.Map.entry("Pyrite", 1),
            java.util.Map.entry("Malachite", 1),
            java.util.Map.entry("Danburite", 1),
            // Tier 2
            java.util.Map.entry("BlackSpinel", 2),
            java.util.Map.entry("Silver", 2),
            java.util.Map.entry("Tanzanite", 2),
            java.util.Map.entry("RedSpinel", 2),
            java.util.Map.entry("YellowTopaz", 2),
            java.util.Map.entry("Peridot", 2),
            java.util.Map.entry("Goshenite", 2),
            // Tier 3
            java.util.Map.entry("BlackDiamond", 3),
            java.util.Map.entry("Osmium", 3),
            java.util.Map.entry("BlueSapphire", 3),
            java.util.Map.entry("PigeonBloodRuby", 3),
            java.util.Map.entry("YellowSapphire", 3),
            java.util.Map.entry("TropicheEmerald", 3),
            java.util.Map.entry("Cerussite", 3)
    );

    // Inner event classes that mineSystemPlugin will call via reflection
    public static class OreHitEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final Player player;
        private final Material oreType;
        private final String oreId;
        private final int pickaxeLevel;
        private final int remainingHits;

        public OreHitEvent(Player player, Material oreType, String oreId, int pickaxeLevel, int remainingHits) {
            this.player = player;
            this.oreType = oreType;
            this.oreId = oreId;
            this.pickaxeLevel = pickaxeLevel;
            this.remainingHits = remainingHits;
        }

        public Player getPlayer() { return player; }
        public Material getOreType() { return oreType; }
        public String getOreId() { return oreId; }
        public int getPickaxeLevel() { return pickaxeLevel; }
        public int getRemainingHits() { return remainingHits; }

        @Override
        public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }

    /**
     * Event fired when an ore is fully mined and drops items.
     * This is the correct event for quest tracking (not OreHitEvent which fires on every hit).
     */
    public static class OreMinedEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final Player player;
        private final String oreId;
        private final int amount;

        public OreMinedEvent(Player player, String oreId, int amount) {
            this.player = player;
            this.oreId = oreId;
            this.amount = amount;
        }

        public Player getPlayer() { return player; }
        public String getOreId() { return oreId; }
        public int getAmount() { return amount; }

        @Override
        public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }

    /**
     * Event fired when a tier-specific ore is mined.
     */
    public static class OreMinedTierEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final Player player;
        private final int tier;
        private final int amount;

        public OreMinedTierEvent(Player player, int tier, int amount) {
            this.player = player;
            this.tier = tier;
            this.amount = amount;
        }

        public Player getPlayer() { return player; }
        public int getTier() { return tier; }
        public int getAmount() { return amount; }

        @Override
        public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }

    public static class SphereCompleteEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final Player player;
        private final String type;

        public SphereCompleteEvent(Player player, String type) {
            this.player = player;
            this.type = type;
        }

        public Player getPlayer() { return player; }
        public String getType() { return type; }

        @Override
        public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }

    public MineSystemListener(QuestSystem plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles each individual ore hit.
     * NOTE: This event fires on EVERY hit, not just when ore is fully mined.
     * Do NOT use this for counting mined ores - use OreMinedEvent instead.
     * This can be used for hit-based mechanics if needed.
     */
    @EventHandler
    public void onOreHit(OreHitEvent event) {
        // Intentionally empty - do not count hits as mined ores
        // Quest progress is tracked via OreMinedEvent which fires only when ore drops
    }

    /**
     * Handles ore fully mined events (when ore drops items).
     * This is the correct event for counting mined ores in quests.
     */
    @EventHandler
    public void onOreMined(OreMinedEvent event) {
        Player player = event.getPlayer();
        String oreId = event.getOreId();
        int amount = event.getAmount();

        // Update general ore mining (counts actual mined ores, not hits)
        plugin.getQuestManager().updateProgress(player, QuestObjective.MINING_MINE_ORES, amount, null);

        // Update specific ore type mining
        plugin.getQuestManager().updateProgress(player, QuestObjective.MINING_MINE_SPECIFIC_ORE, amount, oreId);

        // Update tier-specific ore mining based on oreId
        Integer tier = ORE_TIERS.get(oreId);
        if (tier != null) {
            plugin.getQuestManager().updateProgress(player, QuestObjective.MINING_MINE_TIER_ORES, amount, String.valueOf(tier));
        }
    }

    /**
     * Handles tier-specific ore mined events.
     */
    @EventHandler
    public void onOreMinedTier(OreMinedTierEvent event) {
        Player player = event.getPlayer();
        int tier = event.getTier();
        int amount = event.getAmount();

        // Update tier-specific ore mining
        plugin.getQuestManager().updateProgress(player, QuestObjective.MINING_MINE_TIER_ORES, amount, String.valueOf(tier));
    }

    /**
     * Handles sphere completion events
     */
    @EventHandler
    public void onSphereComplete(SphereCompleteEvent event) {
        Player player = event.getPlayer();
        String type = event.getType();

        // Update general sphere completion
        plugin.getQuestManager().updateProgress(player, QuestObjective.MINING_COMPLETE_SPHERES, 1, null);

        // Update sphere type-specific completion
        plugin.getQuestManager().updateProgress(player, QuestObjective.MINING_COMPLETE_SPHERE_TYPE, 1, type);
    }
}
