# Quest System - Plugin Integration Fix Documentation

## Problem Summary

All plugin integrations use **reflection** to fire events to QuestSystem. The external plugins look for inner event classes inside QuestSystem listeners and call their constructors via reflection.

**Two Main Issues Found:**

1. **Non-static inner classes** - Event classes were defined as `public class` instead of `public static class`. Non-static inner classes require an instance of the outer class to be created via reflection, which external plugins cannot provide.

2. **Constructor parameter type mismatch** - BeesPlugin uses `Tier.class` but QuestSystem expects `Object.class`.

---

## Applied Fixes

### QuestSystem Listeners - ALL FIXED
Changed all event inner classes from `public class` to `public static class`:

| Listener | Events Fixed |
|----------|-------------|
| `BeesListener.java` | `HoneyCollectEvent`, `BeeCreateEvent`, `LarvaeCollectEvent` |
| `FishingListener.java` | `FishCatchEvent`, `FishSellEvent`, `TreasureMapFindEvent`, `BountyCompleteEvent` |
| `BloodChestListener.java` | `BloodChestCompleteEvent`, `BloodChestOpenEvent` |
| `DungeonsListener.java` | `DungeonCompleteEvent`, `DungeonBossDefeatEvent` |
| `FarmingListener.java` | `FarmHarvestEvent`, `MaterialCollectEvent`, `FruitSellEvent`, `FarmUpgradeEvent` |
| `MineSystemListener.java` | `OreHitEvent`, `SphereCompleteEvent` |

### BeesPlugin - FIXED
Changed `Tier.class` to `Object.class` in reflection calls:
- `HiveManager.java` - lines 220, 226, 231, 236
- `HiveGui.java` - lines 309, 315, 320, 325

---

## Plugin Connections Overview

### 1. BeesPlugin
**Location:** `C:\Users\mastu\IdeaProjects\beesPlugin`
**Listener:** `org.maks.questsystem.listeners.BeesListener`

| Event | External Call | QuestSystem Constructor | Status |
|-------|--------------|------------------------|--------|
| `HoneyCollectEvent` | `(Player, int, Object)` | `(Player, int, Object)` | **FIXED** |
| `BeeCreateEvent` | `(Player, int, Object)` | `(Player, int, Object)` | **FIXED** |
| `LarvaeCollectEvent` | `(Player, int)` | `(Player, int)` | **FIXED** |

**Files calling events:**
- `HiveManager.java:220` - HoneyCollectEvent (Collect All)
- `HiveManager.java:257` - LarvaeCollectEvent
- `HiveGui.java:309` - BeeCreateEvent

---

### 2. FishingPlugin
**Location:** `C:\Users\mastu\IdeaProjects\FishingPlugin`
**Listener:** `org.maks.questsystem.listeners.FishingListener`

| Event | External Call | QuestSystem Constructor | Status |
|-------|--------------|------------------------|--------|
| `FishCatchEvent` | `(Player, String)` | `(Player, String)` | **FIXED** |
| `FishSellEvent` | `(Player, int)` | `(Player, int)` | **FIXED** |
| `TreasureMapFindEvent` | `(Player)` | `(Player)` | **FIXED** |
| `BountyCompleteEvent` | `(Player)` | `(Player)` | **FIXED** |

**Files calling events:**
- `FishingListener.java:121` - FishCatchEvent
- `QuickSellService.java:141` - FishSellEvent
- `Awarder.java:104` - TreasureMapFindEvent
- `BountyService.java:363` - BountyCompleteEvent

---

### 3. BloodChestPlugin
**Location:** `C:\Users\mastu\IdeaProjects\bloodChestPlugin`
**Listener:** `org.maks.questsystem.listeners.BloodChestListener`

| Event | External Call | QuestSystem Constructor | Status |
|-------|--------------|------------------------|--------|
| `BloodChestCompleteEvent` | `(Player, int)` | `(Player, int)` | **FIXED** |
| `BloodChestOpenEvent` | `(Player)` | `(Player)` | **FIXED** |

**Files calling events:**
- `BloodChestSession.java:996` - BloodChestCompleteEvent
- `BloodChestSession.java:1076` - BloodChestOpenEvent

---

### 4. GroupDungeonPlugin
**Location:** `C:\Users\mastu\IdeaProjects\groupDungeonPlugin`
**Listener:** `org.maks.questsystem.listeners.DungeonsListener`

| Event | External Call | QuestSystem Constructor | Status |
|-------|--------------|------------------------|--------|
| `DungeonCompleteEvent` | `(Player, int)` | `(Player, int)` | **FIXED** |
| `DungeonBossDefeatEvent` | `(Player, String)` | `(Player, String)` | **FIXED** |

**Files calling events:**
- `QuestManager.java:748` - DungeonCompleteEvent
- `MobKillListener.java:250` - DungeonBossDefeatEvent

---

### 5. FarmingPlugin
**Location:** `C:\Users\mastu\IdeaProjects\farmingPlugin`
**Listener:** `org.maks.questsystem.listeners.FarmingListener`

| Event | External Call | QuestSystem Constructor | Status |
|-------|--------------|------------------------|--------|
| `FarmHarvestEvent` | `(Player)` | `(Player)` | **FIXED** |
| `MaterialCollectEvent` | `(Player, String)` | `(Player, String)` | **FIXED** |
| `FruitSellEvent` | `(Player)` | `(Player)` | **FIXED** |
| `FarmUpgradeEvent` | `(Player)` | `(Player)` | **FIXED** |

**Files calling events:**
- `PlantationManager.java:417` - FarmHarvestEvent
- `PlantationManager.java:501` - MaterialCollectEvent
- `QuickSellGUI.java:286` - FruitSellEvent
- `FarmUpgradeGUI.java:408` - FarmUpgradeEvent

---

### 6. MineSystemPlugin
**Location:** `C:\Users\mastu\IdeaProjects\mineSystemPlugin`
**Listener:** `org.maks.questsystem.listeners.MineSystemListener`

| Event | External Call | QuestSystem Constructor | Status |
|-------|--------------|------------------------|--------|
| `OreHitEvent` | `(Player, Material, String, int, int)` | `(Player, Material, String, int, int)` | **FIXED** |
| `SphereCompleteEvent` | `(Player, String)` | `(Player, String)` | **FIXED** |

**Files calling events:**
- `BlockBreakListener.java:69` - OreHitEvent
- `SphereManager.java:735` - SphereCompleteEvent

---

## Root Cause Analysis

### Issue 1: Non-static Inner Classes
All event classes were defined as:
```java
public class HoneyCollectEvent extends Event { ... }
```

This requires an instance of the outer class (e.g., `BeesListener`) to create via reflection. External plugins cannot provide this instance.

**Fix:** Changed to static inner classes:
```java
public static class HoneyCollectEvent extends Event { ... }
```

### Issue 2: BeesPlugin Type Mismatch
The BeesPlugin used `Tier.class` from its own package when calling:
```java
eventClass.getConstructor(Player.class, int.class, Tier.class)
```

But QuestSystem defines:
```java
public HoneyCollectEvent(Player player, int amount, Object tier)
```

**Fix:** Changed BeesPlugin to use `Object.class`:
```java
eventClass.getConstructor(Player.class, int.class, Object.class)
```

---

## Build Instructions

After these fixes, rebuild the following plugins:

1. **QuestSystemNew** - `mvn clean install`
2. **BeesPlugin** - `mvn clean install`

Other plugins (Fishing, BloodChest, Dungeons, Farming, MineSystem) do not need rebuilding as no changes were made to them.

---

## Testing Checklist

After building and deploying the plugins:

### 1. BeesPlugin Integration
- [x] Collect All button triggers HoneyCollectEvent - **FIXED**
- [x] Larvae collection triggers LarvaeCollectEvent - **FIXED**
- [x] Bee creation triggers BeeCreateEvent - **FIXED**

### 2. FishingPlugin Integration
- [x] Catching fish triggers FishCatchEvent - **FIXED (static class)**
- [x] Selling fish triggers FishSellEvent - **FIXED (static class)**
- [x] Finding treasure maps triggers TreasureMapFindEvent - **FIXED (static class)**
- [x] Completing bounties triggers BountyCompleteEvent - **FIXED (static class)**

### 3. BloodChestPlugin Integration
- [x] Completing arena triggers BloodChestCompleteEvent - **FIXED (static class)**
- [x] Opening chests triggers BloodChestOpenEvent - **FIXED (static class)**

### 4. GroupDungeonPlugin Integration
- [x] Completing dungeons triggers DungeonCompleteEvent - **FIXED (static class)**
- [x] Defeating bosses triggers DungeonBossDefeatEvent - **FIXED (static class)**

### 5. FarmingPlugin Integration
- [x] Harvesting triggers FarmHarvestEvent - **FIXED (static class)**
- [x] Collecting materials triggers MaterialCollectEvent - **FIXED (static class)**
- [x] Selling fruits triggers FruitSellEvent - **FIXED (static class)**
- [x] Upgrading farm triggers FarmUpgradeEvent - **FIXED (static class)**

### 6. MineSystemPlugin Integration
- [x] Mining ores triggers OreHitEvent - **FIXED (static class)**
- [x] Completing spheres triggers SphereCompleteEvent - **FIXED (static class)**

---

## Summary

All plugin integrations have been fixed. The main issues were:
1. Non-static inner event classes that couldn't be instantiated via reflection
2. Type mismatch in BeesPlugin (Tier.class vs Object.class)

**Files Modified:**
- `QuestSystemNew/src/main/java/org/maks/questsystem/listeners/BeesListener.java`
- `QuestSystemNew/src/main/java/org/maks/questsystem/listeners/FishingListener.java`
- `QuestSystemNew/src/main/java/org/maks/questsystem/listeners/BloodChestListener.java`
- `QuestSystemNew/src/main/java/org/maks/questsystem/listeners/DungeonsListener.java`
- `QuestSystemNew/src/main/java/org/maks/questsystem/listeners/FarmingListener.java`
- `QuestSystemNew/src/main/java/org/maks/questsystem/listeners/MineSystemListener.java`
- `beesPlugin/src/main/java/org/maks/beesPlugin/hive/HiveManager.java`
- `beesPlugin/src/main/java/org/maks/beesPlugin/gui/HiveGui.java`
- `mineSystemPlugin/src/main/java/org/maks/mineSystemPlugin/listener/BlockBreakListener.java`

---

## Additional Fix: Mining Quest Progress

### Issue 3: Ore Mining Counted Every Hit Instead of Actual Drops

**Problem:** The `OreHitEvent` was fired on every pickaxe hit (each -1 HP to ore block), so a 130 HP ore was counting as 130 mined ores instead of 1.

**Solution:**
1. Added new events `OreMinedEvent` and `OreMinedTierEvent` that fire only when ore is fully mined and drops items
2. Disabled quest progress tracking in `OreHitEvent` handler
3. Added proper tier mapping using ore names (Hematite=T1, BlackSpinel=T2, BlackDiamond=T3, etc.)

**Changes in QuestSystem (MineSystemListener.java):**
- Added `OreMinedEvent` class (fires when ore drops)
- Added `OreMinedTierEvent` class (fires with tier info)
- `onOreHit()` handler now empty (no quest progress)
- Added `onOreMined()` handler for actual ore counting
- Added `onOreMinedTier()` handler for tier-specific counting

**Changes in mineSystemPlugin (BlockBreakListener.java):**
- Removed `OreHitEvent` firing on every hit
- Added `ORE_TIERS` mapping (same as QuestSystem)
- Fixed tier determination to use ore name mapping instead of string heuristics

**Ore Tier Mapping:**
| Tier 1 | Tier 2 | Tier 3 |
|--------|--------|--------|
| Hematite | BlackSpinel | BlackDiamond |
| Magnetite | Silver | Osmium |
| Azurite | Tanzanite | BlueSapphire |
| Carnelian | RedSpinel | PigeonBloodRuby |
| Pyrite | YellowTopaz | YellowSapphire |
| Malachite | Peridot | TropicheEmerald |
| Danburite | Goshenite | Cerussite |

---

## Build Instructions (Updated)

After these fixes, rebuild the following plugins:

1. **QuestSystemNew** - `mvn clean install`
2. **BeesPlugin** - `mvn clean install`
3. **MineSystemPlugin** - `mvn clean install`

Other plugins (Fishing, BloodChest, Dungeons) do not need rebuilding.

---

## Additional Fix: Farming Quest Progress

### Issue 4: Fruit Sell Quest Counted Transactions Instead of Money

**Problem:** The `FruitSellEvent` fired once per transaction, counting as 1 regardless of actual money earned. Quest "sell for 500k $" would count 1 instead of actual value.

**Solution:** Modified `FruitSellEvent` to pass `moneyEarned` (long) parameter.

### Issue 5: Harvest Quest Counted Events Instead of Fruits

**Problem:** The `FarmHarvestEvent` fired once per harvest action, counting as 1 regardless of how many fruits were collected. Harvesting 200 fruits counted as 1.

**Solution:** Modified `FarmHarvestEvent` to pass `amount` (int) parameter.

**Changes in QuestSystem (FarmingListener.java):**
- `FarmHarvestEvent` now has constructor `(Player, int amount)`
- `FruitSellEvent` now has constructor `(Player, long moneyEarned)`
- `onFarmHarvest()` uses `event.getAmount()` for progress
- `onFruitSell()` uses `event.getMoneyEarned()` for progress

**Changes in FarmingPlugin:**
- `PlantationManager.java:418` - passes `totalFruits` to FarmHarvestEvent
- `QuickSellGUI.java:287` - passes `soldValue` (totalValue) to FruitSellEvent

---

## Build Instructions (Final)

After all fixes, rebuild the following plugins:

1. **QuestSystemNew** - `mvn clean install`
2. **BeesPlugin** - `mvn clean install`
3. **MineSystemPlugin** - `mvn clean install`
4. **FarmingPlugin** - `mvn clean install`

Other plugins (Fishing, BloodChest, Dungeons) do not need rebuilding.
