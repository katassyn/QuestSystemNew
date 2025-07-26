package org.maks.questsystem.listeners;

import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.Quest.QuestObjective;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.lang.reflect.Method;
import java.util.List;

public class MythicMobKillListener implements Listener {

    private final QuestSystem plugin;
    private boolean mythicMobsEnabled = false;
    private Class<?> mythicMobsAPIClass;
    private Method getMythicMobInstanceMethod;
    private Method getMobTypeMethod;
    private Method getInternalNameMethod;

    public MythicMobKillListener(QuestSystem plugin) {
        this.plugin = plugin;
        setupMythicMobs();
    }

    private void setupMythicMobs() {
        boolean debug = plugin.getConfig().getBoolean("debug", false);

        try {
            // Check if MythicMobs is loaded
            if (plugin.getServer().getPluginManager().getPlugin("MythicMobs") != null) {
                if (debug) {
                    plugin.getLogger().info("[QuestSystem] MythicMobs plugin found, setting up integration...");
                }

                // Get MythicMobs API class
                if (debug) {
                    plugin.getLogger().info("[QuestSystem] Loading MythicBukkit class...");
                }
                mythicMobsAPIClass = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");

                if (debug) {
                    plugin.getLogger().info("[QuestSystem] Getting API instance method...");
                }
                // Get API instance method
                Method getAPIMethod = mythicMobsAPIClass.getDeclaredMethod("inst");
                Object apiInstance = getAPIMethod.invoke(null);

                if (debug) {
                    plugin.getLogger().info("[QuestSystem] Getting methods for mob identification...");
                }
                // Get methods for mob identification
                Class<?> apiClass = apiInstance.getClass();
                getMythicMobInstanceMethod = apiClass.getDeclaredMethod("getMythicMobInstance", Entity.class);

                if (debug) {
                    plugin.getLogger().info("[QuestSystem] Getting methods for mob type...");
                }
                // Get methods for mob type
                Class<?> mobInstanceClass = Class.forName("io.lumine.mythic.bukkit.mobs.ActiveMob");
                getMobTypeMethod = mobInstanceClass.getDeclaredMethod("getType");

                if (debug) {
                    plugin.getLogger().info("[QuestSystem] Getting method for internal name...");
                }
                // Get method for internal name
                Class<?> mobTypeClass = Class.forName("io.lumine.mythic.api.mobs.MythicMob");
                getInternalNameMethod = mobTypeClass.getDeclaredMethod("getInternalName");

                mythicMobsEnabled = true;
                plugin.getLogger().info("[QuestSystem] MythicMobs integration enabled successfully!");

                if (debug) {
                    plugin.getLogger().info("[QuestSystem] MythicMobs reflection setup details:");
                    plugin.getLogger().info("  - MythicMobsAPIClass: " + mythicMobsAPIClass.getName());
                    plugin.getLogger().info("  - getMythicMobInstanceMethod: " + getMythicMobInstanceMethod.getName());
                    plugin.getLogger().info("  - getMobTypeMethod: " + getMobTypeMethod.getName());
                    plugin.getLogger().info("  - getInternalNameMethod: " + getInternalNameMethod.getName());
                }
            } else {
                plugin.getLogger().warning("[QuestSystem] MythicMobs plugin not found! MythicMob quests will not work.");
                mythicMobsEnabled = false;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[QuestSystem] Failed to setup MythicMobs integration: " + e.getMessage());
            if (debug) {
                plugin.getLogger().severe("[QuestSystem] Detailed error information:");
                e.printStackTrace();
            }
            mythicMobsEnabled = false;
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        boolean debug = plugin.getConfig().getBoolean("debug", false);

        if (!mythicMobsEnabled) {
            if (debug) {
                plugin.getLogger().info("[QuestSystem] MythicMobs integration is not enabled, skipping entity death event");
            }
            return;
        }

        Entity entity = event.getEntity();

        // Only living entities can have killers
        if (!(entity instanceof org.bukkit.entity.LivingEntity)) {
            if (debug) {
                plugin.getLogger().info("[QuestSystem] Entity is not a living entity, skipping: " + entity.getType());
            }
            return;
        }

        org.bukkit.entity.LivingEntity livingEntity = (org.bukkit.entity.LivingEntity) entity;
        Player killer = livingEntity.getKiller();

        if (killer == null) {
            if (debug) {
                plugin.getLogger().info("[QuestSystem] Entity has no killer, skipping: " + entity.getType());
            }
            return;
        }

        if (debug) {
            plugin.getLogger().info("[QuestSystem] Processing entity death: " + entity.getType() + " killed by " + killer.getName());
        }

        try {
            // Check if entity is a MythicMob
            if (debug) {
                plugin.getLogger().info("[QuestSystem] Checking if entity is a MythicMob...");
            }

            Object mobInstance = getMythicMobInstanceMethod.invoke(null, entity);
            if (mobInstance == null) {
                if (debug) {
                    plugin.getLogger().info("[QuestSystem] Entity is not a MythicMob, skipping");
                }
                return;
            }

            if (debug) {
                plugin.getLogger().info("[QuestSystem] Entity is a MythicMob, getting mob type...");
            }

            // Get mob type
            Object mobType = getMobTypeMethod.invoke(mobInstance);
            if (mobType == null) {
                if (debug) {
                    plugin.getLogger().info("[QuestSystem] Failed to get MythicMob type, skipping");
                }
                return;
            }

            if (debug) {
                plugin.getLogger().info("[QuestSystem] Got MythicMob type, getting internal name...");
            }

            // Get internal name
            String mobId = (String) getInternalNameMethod.invoke(mobType);
            if (mobId == null) {
                if (debug) {
                    plugin.getLogger().info("[QuestSystem] Failed to get MythicMob internal name, skipping");
                }
                return;
            }

            // Debug logging
            if (debug) {
                plugin.getLogger().info("[QuestSystem] MythicMob killed: " + mobId + " by " + killer.getName());
            }

            int playerLevel = getPlayerLevel(killer);

            // Debug logging for player level
            if (debug) {
                plugin.getLogger().info("[QuestSystem] Player " + killer.getName() + " level: " + playerLevel);
            }

            // Check what type of mob this is
            String mobTypeCategory = getMobType(mobId, playerLevel);

            if (mobTypeCategory != null) {
                switch (mobTypeCategory) {
                    case "normal":
                        plugin.getQuestManager().updateProgress(killer, QuestObjective.KILL_NORMAL, 1, null);
                        break;
                    case "elite":
                        plugin.getQuestManager().updateProgress(killer, QuestObjective.KILL_ELITE, 1, null);
                        break;
                    case "mini_boss":
                        plugin.getQuestManager().updateProgress(killer, QuestObjective.KILL_MINI_BOSS, 1, null);
                        break;
                    case "boss":
                        plugin.getQuestManager().updateProgress(killer, QuestObjective.KILL_BOSS, 1, null);
                        break;
                }
            }

            // Check if it's a current expo mob
            String currentExpoType = getCurrentExpoMobType(mobId, playerLevel);
            if (currentExpoType != null) {
                switch (currentExpoType) {
                    case "normal":
                        plugin.getQuestManager().updateProgress(killer, QuestObjective.KILL_CURRENT_EXPO_NORMAL, 1, null);
                        break;
                    case "elite":
                        plugin.getQuestManager().updateProgress(killer, QuestObjective.KILL_CURRENT_EXPO_ELITE, 1, null);
                        break;
                    case "mini_boss":
                        plugin.getQuestManager().updateProgress(killer, QuestObjective.KILL_CURRENT_EXPO_MINI_BOSS, 1, null);
                        break;
                }
            }

            // Check if it's a dungeon boss
            checkDungeonBoss(killer, mobId);

        } catch (Exception e) {
            plugin.getLogger().warning("[QuestSystem] Error processing MythicMob death: " + e.getMessage());
            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().severe("[QuestSystem] Detailed error information:");
                e.printStackTrace();

                // Log reflection details to help diagnose issues
                plugin.getLogger().info("[QuestSystem] Reflection debug info:");
                plugin.getLogger().info("  - MythicMobs enabled: " + mythicMobsEnabled);
                plugin.getLogger().info("  - MythicMobsAPIClass: " + (mythicMobsAPIClass != null ? mythicMobsAPIClass.getName() : "null"));
                plugin.getLogger().info("  - getMythicMobInstanceMethod: " + (getMythicMobInstanceMethod != null ? getMythicMobInstanceMethod.getName() : "null"));
                plugin.getLogger().info("  - getMobTypeMethod: " + (getMobTypeMethod != null ? getMobTypeMethod.getName() : "null"));
                plugin.getLogger().info("  - getInternalNameMethod: " + (getInternalNameMethod != null ? getInternalNameMethod.getName() : "null"));
            }
        }
    }

    private String getMobType(String mobId, int playerLevel) {
        boolean debug = plugin.getConfig().getBoolean("debug", false);

        // Check general elite/mini_boss/boss lists
        List<String> elites = plugin.getConfig().getStringList("elites");
        for (String elite : elites) {
            if (elite.equalsIgnoreCase(mobId)) {
                if (debug) {
                    plugin.getLogger().info("Mob " + mobId + " identified as elite");
                }
                return "elite";
            }
        }

        List<String> miniBosses = plugin.getConfig().getStringList("mini_bosses");
        for (String miniBoss : miniBosses) {
            if (miniBoss.equalsIgnoreCase(mobId)) {
                if (debug) {
                    plugin.getLogger().info("Mob " + mobId + " identified as mini_boss");
                }
                return "mini_boss";
            }
        }

        List<String> bosses = plugin.getConfig().getStringList("bosses");
        for (String boss : bosses) {
            if (boss.equalsIgnoreCase(mobId)) {
                if (debug) {
                    plugin.getLogger().info("Mob " + mobId + " identified as boss");
                }
                return "boss";
            }
        }

        // Check expowiska mobs
        ConfigurationSection expowiska = plugin.getConfig().getConfigurationSection("expowiska");
        if (expowiska != null) {
            for (String levelRange : expowiska.getKeys(false)) {
                ConfigurationSection expo = expowiska.getConfigurationSection(levelRange);
                if (expo != null) {
                    List<String> normalMobs = expo.getStringList("normal");
                    for (String normalMob : normalMobs) {
                        if (normalMob.equalsIgnoreCase(mobId)) {
                            if (debug) {
                                plugin.getLogger().info("Mob " + mobId + " identified as normal in level range " + levelRange);
                            }
                            return "normal";
                        }
                    }

                    List<String> eliteMobs = expo.getStringList("elite");
                    for (String eliteMob : eliteMobs) {
                        if (eliteMob.equalsIgnoreCase(mobId)) {
                            if (debug) {
                                plugin.getLogger().info("Mob " + mobId + " identified as elite in level range " + levelRange);
                            }
                            return "elite";
                        }
                    }

                    List<String> miniBossMobs = expo.getStringList("mini_boss");
                    for (String miniBossMob : miniBossMobs) {
                        if (miniBossMob.equalsIgnoreCase(mobId)) {
                            if (debug) {
                                plugin.getLogger().info("Mob " + mobId + " identified as mini_boss in level range " + levelRange);
                            }
                            return "mini_boss";
                        }
                    }

                    List<String> bossMobs = expo.getStringList("boss");
                    for (String bossMob : bossMobs) {
                        if (bossMob.equalsIgnoreCase(mobId)) {
                            if (debug) {
                                plugin.getLogger().info("Mob " + mobId + " identified as boss in level range " + levelRange);
                            }
                            return "boss";
                        }
                    }
                }
            }
        }

        return null;
    }

    private String getCurrentExpoMobType(String mobId, int playerLevel) {
        boolean debug = plugin.getConfig().getBoolean("debug", false);
        String currentExpo = getCurrentExpoRange(playerLevel);
        if (currentExpo == null) {
            return null;
        }

        if (debug) {
            plugin.getLogger().info("Checking if mob " + mobId + " is in current expo range: " + currentExpo);
        }

        ConfigurationSection expo = plugin.getConfig().getConfigurationSection("expowiska." + currentExpo);
        if (expo != null) {
            List<String> normalMobs = expo.getStringList("normal");
            for (String normalMob : normalMobs) {
                if (normalMob.equalsIgnoreCase(mobId)) {
                    if (debug) {
                        plugin.getLogger().info("Mob " + mobId + " identified as current expo normal mob");
                    }
                    return "normal";
                }
            }

            List<String> eliteMobs = expo.getStringList("elite");
            for (String eliteMob : eliteMobs) {
                if (eliteMob.equalsIgnoreCase(mobId)) {
                    if (debug) {
                        plugin.getLogger().info("Mob " + mobId + " identified as current expo elite mob");
                    }
                    return "elite";
                }
            }

            List<String> miniBossMobs = expo.getStringList("mini_boss");
            for (String miniBossMob : miniBossMobs) {
                if (miniBossMob.equalsIgnoreCase(mobId)) {
                    if (debug) {
                        plugin.getLogger().info("Mob " + mobId + " identified as current expo mini_boss mob");
                    }
                    return "mini_boss";
                }
            }
        }

        return null;
    }

    private String getCurrentExpoRange(int level) {
        if (level >= 1 && level < 5) return "1-5";
        if (level >= 5 && level < 10) return "5-10";
        if (level >= 10 && level < 15) return "10-15";
        if (level >= 15 && level < 20) return "15-20";
        if (level >= 20 && level < 25) return "20-25";
        if (level >= 25 && level < 30) return "25-30";
        if (level >= 30 && level < 35) return "30-35";
        if (level >= 35 && level < 40) return "35-40";
        if (level >= 40 && level < 45) return "40-45";
        if (level >= 45 && level < 50) return "45-50";
        if (level >= 50 && level < 60) return "50-60";
        if (level >= 60 && level < 70) return "60-70";
        if (level >= 70 && level < 80) return "70-80";
        if (level >= 80 && level < 90) return "80-90";
        if (level >= 90) return "90-100";
        return null;
    }

    private void checkDungeonBoss(Player player, String mobId) {
        boolean debug = plugin.getConfig().getBoolean("debug", false);

        if (debug) {
            plugin.getLogger().info("[QuestSystem] Checking if mob " + mobId + " is a dungeon boss...");
        }

        // Check Q Inf bosses
        ConfigurationSection qinf = plugin.getConfig().getConfigurationSection("mythic_mobs.qinf");
        if (qinf != null) {
            if (debug) {
                plugin.getLogger().info("[QuestSystem] Checking against Q INF bosses...");
            }

            for (String questNum : qinf.getKeys(false)) {
                String configMobId = qinf.getString(questNum);
                if (debug) {
                    plugin.getLogger().info("[QuestSystem] Comparing Q" + questNum + " INF boss: config=" + configMobId + ", killed=" + mobId);
                }

                if (configMobId != null && configMobId.equalsIgnoreCase(mobId)) {
                    if (debug) {
                        plugin.getLogger().info("[QuestSystem] Match found! Updating progress for Q" + questNum + " INF");
                    }
                    plugin.getQuestManager().updateProgress(player, QuestObjective.FINISH_Q_INF, 1, questNum);
                    plugin.getQuestManager().updateProgress(player, QuestObjective.FINISH_ALL_Q_INF, 1, null);
                    return;
                }
            }
        } else if (debug) {
            plugin.getLogger().info("[QuestSystem] No Q INF bosses found in config");
        }

        // Check Q Hell bosses
        ConfigurationSection qhell = plugin.getConfig().getConfigurationSection("mythic_mobs.qhell");
        if (qhell != null) {
            if (debug) {
                plugin.getLogger().info("[QuestSystem] Checking against Q HELL bosses...");
            }

            for (String questNum : qhell.getKeys(false)) {
                String configMobId = qhell.getString(questNum);
                if (debug) {
                    plugin.getLogger().info("[QuestSystem] Comparing Q" + questNum + " HELL boss: config=" + configMobId + ", killed=" + mobId);
                }

                if (configMobId != null && configMobId.equalsIgnoreCase(mobId)) {
                    if (debug) {
                        plugin.getLogger().info("[QuestSystem] Match found! Updating progress for Q" + questNum + " HELL");
                    }
                    plugin.getQuestManager().updateProgress(player, QuestObjective.FINISH_Q_HELL, 1, questNum);
                    plugin.getQuestManager().updateProgress(player, QuestObjective.FINISH_ALL_Q_HELL, 1, null);
                    return;
                }
            }
        } else if (debug) {
            plugin.getLogger().info("[QuestSystem] No Q HELL bosses found in config");
        }

        // Check Q Blood bosses
        ConfigurationSection qblood = plugin.getConfig().getConfigurationSection("mythic_mobs.qblood");
        if (qblood != null) {
            if (debug) {
                plugin.getLogger().info("[QuestSystem] Checking against Q BLOOD bosses...");
            }

            for (String questNum : qblood.getKeys(false)) {
                String configMobId = qblood.getString(questNum);
                if (debug) {
                    plugin.getLogger().info("[QuestSystem] Comparing Q" + questNum + " BLOOD boss: config=" + configMobId + ", killed=" + mobId);
                }

                if (configMobId != null && configMobId.equalsIgnoreCase(mobId)) {
                    if (debug) {
                        plugin.getLogger().info("[QuestSystem] Match found! Updating progress for Q" + questNum + " BLOOD");
                    }
                    plugin.getQuestManager().updateProgress(player, QuestObjective.FINISH_Q_BLOOD, 1, questNum);
                    plugin.getQuestManager().updateProgress(player, QuestObjective.FINISH_ALL_Q_BLOOD, 1, null);
                    return;
                }
            }
        } else if (debug) {
            plugin.getLogger().info("[QuestSystem] No Q BLOOD bosses found in config");
        }

        if (debug) {
            plugin.getLogger().info("[QuestSystem] Mob " + mobId + " is not a dungeon boss");
        }
    }

    private int getPlayerLevel(Player player) {
        // Try to get level from MyExperiencePlugin
        try {
            // Check if MyExperiencePlugin is loaded
            if (plugin.getServer().getPluginManager().getPlugin("MyExperiencePlugin") != null) {
                // Get the main class of MyExperiencePlugin
                Class<?> myExpPluginClass = Class.forName("com.maks.myexperienceplugin.MyExperiencePlugin");

                // Get the getInstance method
                Method getInstanceMethod = myExpPluginClass.getDeclaredMethod("getInstance");
                Object myExpPluginInstance = getInstanceMethod.invoke(null);

                // Get the getPlayerLevel method
                Method getPlayerLevelMethod = myExpPluginClass.getDeclaredMethod("getPlayerLevel", Player.class);

                // Call the method to get the player's level
                int level = (int) getPlayerLevelMethod.invoke(myExpPluginInstance, player);

                if (plugin.getConfig().getBoolean("debug", false)) {
                    plugin.getLogger().info("[QuestSystem] Got player level from MyExperiencePlugin: " + level + " for " + player.getName());
                }

                return level;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[QuestSystem] Failed to get player level from MyExperiencePlugin: " + e.getMessage());
            if (plugin.getConfig().getBoolean("debug", false)) {
                e.printStackTrace();
            }
        }

        // Fallback to vanilla level
        return player.getLevel();
    }
}
