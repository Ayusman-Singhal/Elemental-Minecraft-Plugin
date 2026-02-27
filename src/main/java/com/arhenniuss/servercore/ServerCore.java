package com.arhenniuss.servercore;

import com.arhenniuss.servercore.ability.AbilityRegistry;
import com.arhenniuss.servercore.ability.AbilityService;
import com.arhenniuss.servercore.ability.CooldownManager;
import com.arhenniuss.servercore.ability.impl.fire.FireBasic;
import com.arhenniuss.servercore.ability.impl.fire.FirePassive;
import com.arhenniuss.servercore.ability.impl.fire.FireSecondary;
import com.arhenniuss.servercore.ability.impl.fire.FireSpecialCharged;
import com.arhenniuss.servercore.ability.impl.fire.FireSpecialSimple;
import com.arhenniuss.servercore.ability.impl.fire.FlamePropel;
import com.arhenniuss.servercore.ability.impl.earth.EarthPassive;
import com.arhenniuss.servercore.ability.impl.earth.GroundSnare;
import com.arhenniuss.servercore.ability.impl.earth.RockSlam;
import com.arhenniuss.servercore.ability.impl.earth.StoneJab;
import com.arhenniuss.servercore.ability.impl.earth.StoneStomp;
import com.arhenniuss.servercore.ability.impl.earth.TectonicBurst;
import com.arhenniuss.servercore.ability.impl.air.AirCutter;
import com.arhenniuss.servercore.ability.impl.air.AirPassive;
import com.arhenniuss.servercore.ability.impl.air.CycloneSpin;
import com.arhenniuss.servercore.ability.impl.air.Skyfall;
import com.arhenniuss.servercore.ability.impl.air.Updraft;
import com.arhenniuss.servercore.ability.impl.air.WindStep;
import com.arhenniuss.servercore.ability.impl.water.WaterBasic;
import com.arhenniuss.servercore.ability.impl.water.WaterDash;
import com.arhenniuss.servercore.ability.impl.water.WaterPassive;
import com.arhenniuss.servercore.ability.impl.water.WaterSecondary;
import com.arhenniuss.servercore.ability.impl.water.WaterSpecialCharged;
import com.arhenniuss.servercore.ability.impl.water.WaterSpecialSimple;
import com.arhenniuss.servercore.combat.AbilityDebugManager;
import com.arhenniuss.servercore.combat.CombatTagManager;
import com.arhenniuss.servercore.combat.CooldownDisplayService;
import com.arhenniuss.servercore.combat.DamageAttributionManager;
import com.arhenniuss.servercore.combat.reaction.ReactionService;
import com.arhenniuss.servercore.combat.reaction.rules.EarthHardensWetRule;
import com.arhenniuss.servercore.combat.reaction.rules.FireEvaporatesWetRule;
import com.arhenniuss.servercore.combat.reaction.rules.FireIgnitesBurningAmplifyRule;
import com.arhenniuss.servercore.combat.reaction.rules.WaterExtinguishesBurningRule;
import com.arhenniuss.servercore.combat.reaction.rules.AirBreaksRootRule;
import com.arhenniuss.servercore.combat.reaction.rules.FireScorchesAirRule;
import com.arhenniuss.servercore.combat.reaction.rules.RootedSlowsAirMobilityRule;
import com.arhenniuss.servercore.combat.reaction.rules.WetDampensFireRule;
import com.arhenniuss.servercore.combat.reaction.rules.WaterExtendsRootRule;
import com.arhenniuss.servercore.combat.status.StatusService;
import com.arhenniuss.servercore.combat.targeting.ReachHelper;
import com.arhenniuss.servercore.command.DummyCommand;
import com.arhenniuss.servercore.command.ServerCoreCommand;
import com.arhenniuss.servercore.config.AbilityBalanceConfig;
import com.arhenniuss.servercore.gui.ElementSelectionGUI;
import com.arhenniuss.servercore.listener.DummyListener;
import com.arhenniuss.servercore.listener.GUIClickListener;
import com.arhenniuss.servercore.listener.NPCInteractListener;
import com.arhenniuss.servercore.listener.NPCProtectionListener;
import com.arhenniuss.servercore.listener.PlayerDataListener;
import com.arhenniuss.servercore.listener.ability.AbilityInputListener;
import com.arhenniuss.servercore.listener.ability.AirPassiveListener;
import com.arhenniuss.servercore.listener.ability.DoubleJumpGroundTask;
import com.arhenniuss.servercore.listener.ability.EarthPassiveListener;
import com.arhenniuss.servercore.listener.ability.FirePassiveListener;
import com.arhenniuss.servercore.listener.ability.WaterPassiveListener;
import com.arhenniuss.servercore.listener.combat.CombatDamageListener;
import com.arhenniuss.servercore.listener.combat.CombatTagListener;
import com.arhenniuss.servercore.npc.ElementAssignerNPC;
import com.arhenniuss.servercore.player.PlayerDataManager;
import com.arhenniuss.servercore.storage.YamlPlayerStorage;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class ServerCore extends JavaPlugin {

        private PlayerDataManager playerDataManager;

        @Override
        public void onEnable() {
                // ── Keys ────────────────────────────────────────────────────
                NamespacedKey npcKey = new NamespacedKey(this, "element_assigner");

                // ── Phase 1: Element Selection ──────────────────────────────
                YamlPlayerStorage storage = new YamlPlayerStorage(this);
                playerDataManager = new PlayerDataManager(storage, getLogger());

                ElementAssignerNPC assignerNPC = new ElementAssignerNPC(npcKey);
                ElementSelectionGUI gui = new ElementSelectionGUI();

                // ── Phase 2: Ability Framework ──────────────────────────────
                CooldownManager cooldownManager = new CooldownManager();

                // ── Phase 3: Combat Infrastructure ──────────────────────────
                AbilityBalanceConfig balanceConfig = new AbilityBalanceConfig(this);
                DamageAttributionManager damageManager = new DamageAttributionManager();
                CombatTagManager combatTagManager = new CombatTagManager();
                AbilityDebugManager debugManager = new AbilityDebugManager();
                ReachHelper reachHelper = new ReachHelper();
                StatusService statusService = new StatusService();

                CooldownDisplayService cooldownDisplay = new CooldownDisplayService(
                                cooldownManager, playerDataManager);

                // Registry — register all Fire abilities
                // Abilities are visual-only; AbilityService handles all combat
                AbilityRegistry registry = new AbilityRegistry();
                registry.register(new FirePassive());
                registry.register(new FireBasic(balanceConfig));
                registry.register(new FireSecondary(balanceConfig));
                registry.register(new FireSpecialSimple(balanceConfig));
                registry.register(new FireSpecialCharged(balanceConfig));

                // Water abilities
                registry.register(new WaterPassive());
                registry.register(new WaterBasic(balanceConfig));
                registry.register(new WaterSecondary(balanceConfig));
                registry.register(new WaterSpecialSimple(balanceConfig));
                registry.register(new WaterSpecialCharged(balanceConfig));

                // Earth abilities
                registry.register(new EarthPassive());
                registry.register(new StoneJab(balanceConfig));
                registry.register(new GroundSnare(balanceConfig));
                registry.register(new RockSlam(balanceConfig));
                registry.register(new TectonicBurst(balanceConfig));

                // Mobility abilities
                registry.register(new WaterDash(balanceConfig));
                registry.register(new FlamePropel(balanceConfig));
                registry.register(new StoneStomp(balanceConfig));

                // Air abilities
                registry.register(new AirPassive());
                registry.register(new AirCutter(balanceConfig));
                registry.register(new Updraft(balanceConfig));
                registry.register(new CycloneSpin(balanceConfig));
                registry.register(new Skyfall(balanceConfig));
                registry.register(new WindStep(balanceConfig));

                // ── Phase 6: Reaction Engine ────────────────────────────────
                ReactionService reactionService = new ReactionService();
                reactionService.registerRule(new FireEvaporatesWetRule(statusService));
                reactionService.registerRule(new WaterExtinguishesBurningRule(statusService));
                reactionService.registerRule(new EarthHardensWetRule(statusService));
                reactionService.registerRule(new FireIgnitesBurningAmplifyRule());

                // Phase 11: Reaction Depth
                reactionService.registerRule(new FireScorchesAirRule(playerDataManager));
                reactionService.registerRule(new WetDampensFireRule());
                reactionService.registerRule(new AirBreaksRootRule(statusService));
                reactionService.registerRule(new WaterExtendsRootRule(statusService));
                reactionService.registerRule(new RootedSlowsAirMobilityRule(playerDataManager, cooldownManager));

                // Ability service — sole owner of damage, knockback, targeting, reactions
                AbilityService abilityService = new AbilityService(
                                registry, cooldownManager, playerDataManager,
                                damageManager, debugManager, balanceConfig,
                                statusService, reactionService, reachHelper, this);

                // ── All Listeners ───────────────────────────────────────────

                // Phase 1 listeners
                getServer().getPluginManager().registerEvents(
                                new NPCInteractListener(assignerNPC, playerDataManager, gui), this);
                getServer().getPluginManager().registerEvents(
                                new NPCProtectionListener(npcKey), this);
                getServer().getPluginManager().registerEvents(
                                new GUIClickListener(playerDataManager), this);
                getServer().getPluginManager().registerEvents(
                                new PlayerDataListener(playerDataManager, cooldownManager,
                                                damageManager, combatTagManager, debugManager,
                                                cooldownDisplay),
                                this);

                // Unified ability input listener (input-only, zero combat logic)
                getServer().getPluginManager().registerEvents(
                                new AbilityInputListener(abilityService), this);

                // Double-jump ground task (enables allowFlight for mobility input)
                new DoubleJumpGroundTask(playerDataManager).register(this);
                getServer().getPluginManager().registerEvents(
                                new FirePassiveListener(playerDataManager), this);
                getServer().getPluginManager().registerEvents(
                                new WaterPassiveListener(playerDataManager), this);
                getServer().getPluginManager().registerEvents(
                                new EarthPassiveListener(playerDataManager), this);
                getServer().getPluginManager().registerEvents(
                                new AirPassiveListener(playerDataManager), this);

                // Combat listeners
                getServer().getPluginManager().registerEvents(
                                new CombatDamageListener(damageManager), this);
                getServer().getPluginManager().registerEvents(
                                new CombatTagListener(combatTagManager, damageManager, balanceConfig), this);

                // ── Command ─────────────────────────────────────────────────
                ServerCoreCommand coreCommand = new ServerCoreCommand(
                                assignerNPC, playerDataManager, debugManager, balanceConfig);
                PluginCommand command = getCommand("servercore");
                if (command != null) {
                        command.setExecutor(coreCommand);
                        command.setTabCompleter(coreCommand);
                }

                // Dummy command
                DummyCommand dummyCommand = new DummyCommand(this);
                PluginCommand dummyCmd = getCommand("dummy");
                if (dummyCmd != null) {
                        dummyCmd.setExecutor(dummyCommand);
                        dummyCmd.setTabCompleter(dummyCommand);
                }

                // Dummy listener
                getServer().getPluginManager().registerEvents(
                                new DummyListener(dummyCommand.getDummyKey()), this);

                // ── Tasks ───────────────────────────────────────────────────

                // Passive abilities — every 20 ticks (1 second)
                getServer().getScheduler().runTaskTimer(this,
                                abilityService::tickPassives, 20L, 20L);

                // Cooldown HUD — every 10 ticks (0.5 seconds)
                getServer().getScheduler().runTaskTimer(this,
                                cooldownDisplay::tick, 10L, 10L);

                // Damage attribution cleanup — every 1200 ticks (60 seconds)
                getServer().getScheduler().runTaskTimer(this,
                                damageManager::cleanupExpired, 1200L, 1200L);

                // Status effect cleanup — every 20 ticks (1 second)
                getServer().getScheduler().runTaskTimer(this,
                                statusService::tick, 20L, 20L);

                getLogger().info("ServerCore enabled successfully!");
        }

        @Override
        public void onDisable() {
                if (playerDataManager != null) {
                        for (UUID uuid : playerDataManager.getCachedUUIDs()) {
                                playerDataManager.savePlayerSync(uuid);
                        }
                }

                getLogger().info("ServerCore disabled!");
        }
}