package org.maks.questsystem.listeners;

import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.Quest;
import org.maks.questsystem.objects.Quest.QuestObjective;
import org.maks.questsystem.objects.PlayerQuestData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import java.util.List;

public class MythicMobKillListenerNew implements Listener {

    private final QuestSystem plugin;

    public MythicMobKillListenerNew(QuestSystem plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("[QuestSystem] MythicMobs integration enabled using direct API!");
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        boolean debug = plugin.getConfig().getBoolean("debug", false);

        // Get the killer
        Player killer = null;
        if (event.getKiller() instanceof Player) {
            killer = (Player) event.getKiller();
        } else {
            if (debug) {
                plugin.getLogger().info("[QuestSystem] Killer is not a player, skipping");
            }
            return;
        }

        // Get the mob ID
        String mobId = event.getMobType().getInternalName();

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

        // Get player quest data
        PlayerQuestData playerData = plugin.getQuestManager().getPlayerData(player.getUniqueId());

        // Check if player has any dungeon-related quests before proceeding
        boolean hasInfQuest = false;
        boolean hasHellQuest = false;
        boolean hasBloodQuest = false;
        boolean hasAnyDungeonQuest = false;

        // Check daily quest
        Quest dailyQuest = playerData.getDailyQuest();
        if (dailyQuest != null && !dailyQuest.isCompleted()) {
            QuestObjective objective = dailyQuest.getObjective();
            if (objective == QuestObjective.FINISH_Q_INF || objective == QuestObjective.FINISH_ALL_Q_INF) {
                hasInfQuest = true;
                hasAnyDungeonQuest = true;
            } else if (objective == QuestObjective.FINISH_Q_HELL || objective == QuestObjective.FINISH_ALL_Q_HELL) {
                hasHellQuest = true;
                hasAnyDungeonQuest = true;
            } else if (objective == QuestObjective.FINISH_Q_BLOOD || objective == QuestObjective.FINISH_ALL_Q_BLOOD) {
                hasBloodQuest = true;
                hasAnyDungeonQuest = true;
            }
        }

        // Check weekly quest
        Quest weeklyQuest = playerData.getWeeklyQuest();
        if (weeklyQuest != null && !weeklyQuest.isCompleted()) {
            QuestObjective objective = weeklyQuest.getObjective();
            if (objective == QuestObjective.FINISH_Q_INF || objective == QuestObjective.FINISH_ALL_Q_INF) {
                hasInfQuest = true;
                hasAnyDungeonQuest = true;
            } else if (objective == QuestObjective.FINISH_Q_HELL || objective == QuestObjective.FINISH_ALL_Q_HELL) {
                hasHellQuest = true;
                hasAnyDungeonQuest = true;
            } else if (objective == QuestObjective.FINISH_Q_BLOOD || objective == QuestObjective.FINISH_ALL_Q_BLOOD) {
                hasBloodQuest = true;
                hasAnyDungeonQuest = true;
            }
        }

        // Check monthly quest
        Quest monthlyQuest = playerData.getMonthlyQuest();
        if (monthlyQuest != null && !monthlyQuest.isCompleted()) {
            QuestObjective objective = monthlyQuest.getObjective();
            if (objective == QuestObjective.FINISH_Q_INF || objective == QuestObjective.FINISH_ALL_Q_INF) {
                hasInfQuest = true;
                hasAnyDungeonQuest = true;
            } else if (objective == QuestObjective.FINISH_Q_HELL || objective == QuestObjective.FINISH_ALL_Q_HELL) {
                hasHellQuest = true;
                hasAnyDungeonQuest = true;
            } else if (objective == QuestObjective.FINISH_Q_BLOOD || objective == QuestObjective.FINISH_ALL_Q_BLOOD) {
                hasBloodQuest = true;
                hasAnyDungeonQuest = true;
            }
        }

        // If player doesn't have any dungeon quests, skip all the checks
        if (!hasAnyDungeonQuest) {
            if (debug) {
                plugin.getLogger().info("[QuestSystem] Player doesn't have any dungeon quests, skipping boss checks");
            }
            return;
        }

        if (debug) {
            plugin.getLogger().info("[QuestSystem] Checking if mob " + mobId + " is a dungeon boss...");
        }

        // Check Q Inf bosses only if player has INF quest
        if (hasInfQuest) {
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
        }

        // Check Q Hell bosses only if player has HELL quest
        if (hasHellQuest) {
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
        }

        // Check Q Blood bosses only if player has BLOOD quest
        if (hasBloodQuest) {
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
        }

        if (debug) {
            plugin.getLogger().info("[QuestSystem] Mob " + mobId + " is not a dungeon boss");
        }
    }

    private int getPlayerLevel(Player player) {
        // Fallback to vanilla level
        return player.getLevel();
    }
}
