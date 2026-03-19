package com.arhenniuss.servercore.ability;

import com.arhenniuss.servercore.combat.AbilityDebugManager;
import com.arhenniuss.servercore.combat.DamageAttributionManager;
import com.arhenniuss.servercore.combat.reaction.ReactionContext;
import com.arhenniuss.servercore.combat.reaction.ReactionExecutor;
import com.arhenniuss.servercore.combat.reaction.ReactionResult;
import com.arhenniuss.servercore.combat.reaction.ReactionService;
import com.arhenniuss.servercore.combat.status.StatusService;
import com.arhenniuss.servercore.combat.status.StatusType;
import com.arhenniuss.servercore.combat.targeting.ReachHelper;
import com.arhenniuss.servercore.config.AbilityBalanceConfig;
import com.arhenniuss.servercore.config.AbilityStats;
import com.arhenniuss.servercore.element.Element;
import com.arhenniuss.servercore.player.PlayerData;
import com.arhenniuss.servercore.player.PlayerDataManager;
import com.arhenniuss.servercore.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Central ability executor — sole owner of all combat logic.
 *
 * Responsibilities:
 * - Cooldown enforcement
 * - Target resolution (SINGLE via ray trace, AOE via radius)
 * - Damage application
 * - Knockback application
 * - Status effects via StatusService (WET, BURNING, etc.)
 * - Reaction processing via ReactionService
 * - Reaction damage routing (applyReactionDamageInternal)
 * - Damage attribution recording
 * - Visual effect delegation to Ability.playEffects()
 * - Debug snapshot
 *
 * Abilities and ReactionRules NEVER apply damage directly.
 * All combat routes through this service.
 */
public class AbilityService {

    private static final double MIN_DOUBLE_JUMP_VERTICAL = 1.8; // Increased for better jump height

    private final AbilityRegistry registry;
    private final CooldownManager cooldownManager;
    private final PlayerDataManager playerDataManager;
    private final DamageAttributionManager damageManager;
    private final AbilityDebugManager debugManager;
    private final AbilityBalanceConfig config;
    private final StatusService statusService;
    private final ReactionService reactionService;
    private final ReachHelper reachHelper;
    private final JavaPlugin plugin;
    private final com.arhenniuss.servercore.listener.ability.MobilityLandingListener mobilityLandingListener;

    /** ReactionExecutor that routes damage through internal methods. */
    private final ReactionExecutor reactionExecutor;

    public AbilityService(AbilityRegistry registry, CooldownManager cooldownManager,
            PlayerDataManager playerDataManager, DamageAttributionManager damageManager,
            AbilityDebugManager debugManager, AbilityBalanceConfig config,
            StatusService statusService, ReactionService reactionService,
            ReachHelper reachHelper, JavaPlugin plugin,
            com.arhenniuss.servercore.listener.ability.MobilityLandingListener mobilityLandingListener) {
        this.registry = registry;
        this.cooldownManager = cooldownManager;
        this.playerDataManager = playerDataManager;
        this.damageManager = damageManager;
        this.debugManager = debugManager;
        this.config = config;
        this.statusService = statusService;
        this.reactionService = reactionService;
        this.reachHelper = reachHelper;
        this.plugin = plugin;
        this.mobilityLandingListener = mobilityLandingListener;

        // Build the executor once — delegates to internal methods
        this.reactionExecutor = new ReactionExecutor() {
            @Override
            public void applyReactionDamage(LivingEntity target, double damage) {
                applyReactionDamageInternal(target, damage);
            }

            @Override
            public void applyReactionAoE(Player caster, double radius, double damage) {
                applyReactionAoEInternal(caster, radius, damage);
            }
        };
    }

    /**
     * Attempts to execute an ability for the given player.
     * Full execution flow:
     * 1. Resolve element + ability
     * 2. Check cooldown
     * 3. Play visual effects (delegated to ability)
     * 4. Find targets
     * 5. Per target: snapshot → damage → knockback → statuses → reaction →
     * attribution
     * 6. Set cooldown
     * 7. Send debug (includes reaction results)
     *
     * @param player the player
     * @param type   the ability type to execute
     * @return true if ability was executed
     */
    public boolean tryExecute(Player player, AbilityType type) {
        Element element = playerDataManager.getElement(player.getUniqueId());
        if (element == null)
            return false;

        Ability ability = registry.getAbility(element, type);
        if (ability == null)
            return false;

        if (cooldownManager.isOnCooldown(player, ability)) {
            long remaining = cooldownManager.getRemainingMillis(player, ability);
            double seconds = remaining / 1000.0;
            player.sendMessage(ChatUtil.format(
                    "§cAbility on cooldown! §e" + String.format("%.1f", seconds) + "s §cremaining."));
            return false;
        }

        PlayerData data = playerDataManager.getPlayerData(player.getUniqueId());
        if (data == null)
            return false;

        AbilityStats stats = config.getStats(element, type);

        // Build context — populate mobility fields from stats
        AbilityContext context;
        if (stats.isMobility()) {
            Vector lookDir = player.getLocation().getDirection().normalize();
            Vector velocity = lookDir.multiply(stats.velocityMagnitude())
                    .setY(stats.velocityY());
            context = new AbilityContext(
                    player, data, plugin,
                    player.getLocation(),
                    System.currentTimeMillis(),
                    damageManager,
                    true, velocity, stats.immunityTicks());
        } else {
            context = new AbilityContext(
                    player, data, plugin,
                    player.getLocation(),
                    System.currentTimeMillis(),
                    damageManager);
        }

        // ── Context-driven execution branch ──
        ReactionResult reactionResult = null;

        if (context.isSelfTargeted()) {
            // ── Mobility status gate ──
            // ROOTED or STUNNED blocks mobility entirely (no cooldown, no effects)
            if (statusService.hasStatus(player, StatusType.ROOTED)
                    || statusService.hasStatus(player, StatusType.STUNNED)) {
                player.sendMessage(ChatUtil.format(
                        "§cYou cannot use mobility while " +
                                (statusService.hasStatus(player, StatusType.ROOTED) ? "rooted!" : "stunned!")));
                return false;
            }

            // Earth mobility: remove ROOTED before velocity (self-cleanse)
            if (ability.getElement() == Element.EARTH
                    && statusService.hasStatus(player, StatusType.ROOTED)) {
                statusService.removeStatus(player, StatusType.ROOTED);
            }

            // Fire mobility: WET reduces velocity by 30%
            Vector velocity = context.getSelfVelocity();
            if (ability.getElement() == Element.FIRE
                    && statusService.hasStatus(player, StatusType.WET)) {
                velocity = velocity.clone().multiply(0.7);
            }

            // Ensure mobility jump height remains high enough for the intended 5+ block jump.
            if (ability.getType() == AbilityType.MOBILITY
                    && velocity.getY() < MIN_DOUBLE_JUMP_VERTICAL) {
                velocity = velocity.clone().setY(MIN_DOUBLE_JUMP_VERTICAL);
            }

            // Play visual effects (only after status gate passes)
            ability.playEffects(context);

            // Apply velocity + optional immunity
            applySelfVelocityInternal(player, velocity);
            if (context.getImmunityTicks() > 0) {
                applyTemporaryImmunity(player, context.getImmunityTicks());
            }

            // Track mobility use for landing damage detection
            mobilityLandingListener.onMobilityUsed(player);
        } else {
            // Play visual effects for non-mobility abilities
            ability.playEffects(context);

            if (ability.getElement() == Element.WATER
                    && ability.getType() == AbilityType.SPECIAL_CHARGED) {
                // Special case: Maelstrom two-phase
                reactionResult = executeMaelstrom(player, ability, stats, context);
            } else {
                // Standard target-based execution
                List<LivingEntity> targets = resolveTargets(player, ability, stats);
                reactionResult = applyDamage(player, targets, ability, stats);
            }
        }

        // Set cooldown (only reached on successful execution)
        cooldownManager.setCooldown(player, ability);

        // Debug info
        if (debugManager.isDebugEnabled(player)) {
            long remainingCd = cooldownManager.getRemainingMillis(player, ability);
            debugManager.sendDebug(player, ability, stats, stats.damage(), remainingCd);
            if (context.isSelfTargeted()) {
                debugManager.sendMobilityDebug(player, context);
            }
            if (reactionResult != null && reactionResult.hasReactions()) {
                debugManager.sendReactionDebug(player, reactionResult);
            }
        }

        return true;
    }

    /**
     * Resolves targets based on ability's TargetMode.
     */
    private List<LivingEntity> resolveTargets(Player player, Ability ability, AbilityStats stats) {
        List<LivingEntity> targets = new ArrayList<>();

        switch (ability.getTargetMode()) {
            case SINGLE -> {
                LivingEntity target = reachHelper.findTarget(player, stats.reach());
                if (target != null) {
                    targets.add(target);
                }
            }
            case AOE -> {
                double radius = stats.radius();
                for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                    if (entity instanceof LivingEntity living
                            && !entity.equals(player)
                            && !(entity instanceof Player p && p.getGameMode() == GameMode.SPECTATOR)
                            && !(entity instanceof org.bukkit.entity.ArmorStand as
                                    && !as.getPersistentDataContainer().has(
                                            new org.bukkit.NamespacedKey(plugin, "training_dummy"),
                                            org.bukkit.persistence.PersistentDataType.BYTE))) {
                        targets.add(living);
                    }
                }
            }
        }

        return targets;
    }

    /**
     * Applies damage, knockback, statuses, reactions, and records attribution.
     * Uses target.damage(amount) WITHOUT attacker parameter to prevent recursion.
     *
     * Per-target flow:
     * 1. Snapshot statusesBefore
     * 2. Apply base damage
     * 3. Apply knockback
     * 4. Apply element statuses via StatusService
     * 5. Build ReactionContext
     * 6. Call reactionService.process()
     * 7. Record damage attribution
     *
     * @return accumulated ReactionResult across all targets
     */
    private ReactionResult applyDamage(Player attacker, List<LivingEntity> targets,
            Ability ability, AbilityStats stats) {

        ReactionResult combinedResult = new ReactionResult();

        for (LivingEntity target : targets) {
            // 1. Snapshot statusesBefore (BEFORE applying new statuses)
            Set<StatusType> statusesBefore = statusService.getActiveStatuses(target);

            // 2. Apply base damage (no attacker param → no event recursion)
            target.damage(stats.damage());

            // 2.5 Hit confirmation sound (per-element, at target location)
            switch (ability.getElement()) {
                case FIRE -> attacker.getWorld().playSound(target.getLocation(),
                        Sound.ENTITY_BLAZE_HURT, 0.6f, 1.2f);
                case WATER -> attacker.getWorld().playSound(target.getLocation(),
                        Sound.ENTITY_GENERIC_SPLASH, 0.7f, 1.3f);
                case EARTH -> attacker.getWorld().playSound(target.getLocation(),
                        Sound.ENTITY_IRON_GOLEM_HURT, 0.6f, 0.9f);
                case AIR -> attacker.getWorld().playSound(target.getLocation(),
                        Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.7f, 1.5f);
                default -> {
                }
            }

            // 3. Apply knockback with vertical component for more impact
            boolean isEarthPlayer = (target instanceof Player p) && (playerDataManager.getElement(p.getUniqueId()) == Element.EARTH);
            
            if (stats.knockback() > 0 && !isEarthPlayer) {
                Vector diff = target.getLocation().toVector().subtract(attacker.getLocation().toVector());
                if (diff.lengthSquared() > 0) {
                    Vector knockback = diff.normalize()
                            .multiply(stats.knockback())
                            .setY(0.6); // Increased vertical knockback for more impact
                    if (Double.isFinite(knockback.getX()) && Double.isFinite(knockback.getY()) && Double.isFinite(knockback.getZ())) {
                        target.setVelocity(target.getVelocity().add(knockback));
                    }
                }
                
                // Add screen shake effect for high knockback abilities
                if (stats.knockback() > 1.5) {
                    // This would require a client-side plugin or packet manipulation
                    // For now, we'll just add a visual cue with particles
                    target.getWorld().spawnParticle(
                            org.bukkit.Particle.EXPLOSION_LARGE,
                            target.getLocation(),
                            1,
                            0, 0, 0,
                            0);
                }
            }

            // 4. Apply element-specific statuses via StatusService
            Set<StatusType> statusesApplied = EnumSet.noneOf(StatusType.class);

            if (ability.getElement() == Element.FIRE) {
                int burnTicks = (ability.getType() == AbilityType.SPECIAL_CHARGED) ? 120 : 60;
                statusService.applyStatus(target, StatusType.BURNING, burnTicks);
                statusesApplied.add(StatusType.BURNING);
                
                // Visual effect for burning
                target.getWorld().spawnParticle(
                        org.bukkit.Particle.FLAME,
                        target.getLocation().add(0, 1, 0),
                        10,
                        0.3, 0.5, 0.3,
                        0.05);
            }

            if (ability.getElement() == Element.WATER) {
                if (ability.getType() == AbilityType.BASIC
                        || ability.getType() == AbilityType.SECONDARY) {
                    statusService.applyStatus(target, StatusType.WET, 80); // Longer duration
                    statusesApplied.add(StatusType.WET);
                    
                    // Visual effect for wet
                    target.getWorld().spawnParticle(
                            org.bukkit.Particle.DRIP_WATER,
                            target.getLocation().add(0, 1, 0),
                            15,
                            0.3, 0.5, 0.3,
                            0);
                }
            }

            if (ability.getElement() == Element.EARTH) {
                if (ability.getType() == AbilityType.BASIC) {
                    // Stone Strike: brief root
                    statusService.applyStatus(target, StatusType.ROOTED, 40); // Longer duration
                    statusesApplied.add(StatusType.ROOTED);
                    
                    // Visual effect for rooted
                    target.getWorld().spawnParticle(
                            org.bukkit.Particle.BLOCK_CRACK,
                            target.getLocation().add(0, 0.1, 0),
                            20,
                            0.3, 0.1, 0.3,
                            0,
                            org.bukkit.Material.STONE.createBlockData());
                } else if (ability.getType() == AbilityType.SECONDARY) {
                    // Fault Line: slow + minor knockup
                    statusService.applyStatus(target, StatusType.SLOWED, 80); // Longer duration
                    statusesApplied.add(StatusType.SLOWED);
                    if (!isEarthPlayer) {
                        target.setVelocity(target.getVelocity().add(new Vector(0, 0.6, 0))); // Stronger knockup
                    }
                } else if (ability.getType() == AbilityType.SPECIAL_SIMPLE) {
                    // Seismic Slam: root + strong knockup
                    statusService.applyStatus(target, StatusType.ROOTED, 60); // Longer duration
                    statusesApplied.add(StatusType.ROOTED);
                    if (!isEarthPlayer) {
                        target.setVelocity(target.getVelocity().add(new Vector(0, 1.0, 0))); // Stronger knockup
                    }
                    
                    // Visual effect for seismic slam
                    target.getWorld().spawnParticle(
                            org.bukkit.Particle.EXPLOSION_NORMAL,
                            target.getLocation(),
                            15,
                            0.5, 0.3, 0.5,
                            0.1);
                } else if (ability.getType() == AbilityType.SPECIAL_CHARGED) {
                    // Tectonic Break: root + strong knockup
                    statusService.applyStatus(target, StatusType.ROOTED, 100); // Much longer duration
                    statusesApplied.add(StatusType.ROOTED);
                    if (!isEarthPlayer) {
                        target.setVelocity(target.getVelocity().add(new Vector(0, 1.2, 0))); // Stronger knockup
                    }
                    
                    // Visual effect for tectonic break
                    target.getWorld().spawnParticle(
                            org.bukkit.Particle.EXPLOSION_LARGE,
                            target.getLocation(),
                            3,
                            0.5, 0.5, 0.5,
                            0);
                }
            }

            if (ability.getElement() == Element.AIR) {
                if (ability.getType() == AbilityType.BASIC) {
                    // Air Cutter: brief slow
                    statusService.applyStatus(target, StatusType.SLOWED, 30); // Moderate duration
                    statusesApplied.add(StatusType.SLOWED);
                } else if (ability.getType() == AbilityType.SECONDARY) {
                    // Updraft: knockup (strong lift)
                    if (!isEarthPlayer) {
                        target.setVelocity(target.getVelocity().add(new Vector(0, 0.8, 0))); // Stronger knockup
                    }
                    
                    // Visual effect for updraft
                    target.getWorld().spawnParticle(
                            org.bukkit.Particle.CLOUD,
                            target.getLocation().add(0, 0.5, 0),
                            10,
                            0.3, 0.5, 0.3,
                            0.05);
                } else if (ability.getType() == AbilityType.SPECIAL_SIMPLE) {
                    // Cyclone Spin: outward push
                    Vector diff = target.getLocation().toVector().subtract(attacker.getLocation().toVector());
                    if (diff.lengthSquared() > 0) {
                        Vector push = diff.normalize().multiply(1.5).setY(0.5); // Stronger push
                        if (Double.isFinite(push.getX()) && Double.isFinite(push.getY()) && Double.isFinite(push.getZ())) {
                            if (!isEarthPlayer) {
                                target.setVelocity(target.getVelocity().add(push));
                            }
                        }
                    }
                    
                    // Visual effect for cyclone
                    target.getWorld().spawnParticle(
                            org.bukkit.Particle.CLOUD,
                            target.getLocation().add(0, 1, 0),
                            20,
                            0.5, 0.5, 0.5,
                            0.1);
                } else if (ability.getType() == AbilityType.SPECIAL_CHARGED) {
                    // Skyfall: knockup slam
                    if (!isEarthPlayer) {
                        target.setVelocity(target.getVelocity().add(new Vector(0, 1.0, 0))); // Stronger knockup
                    }
                    
                    // Visual effect for skyfall
                    target.getWorld().spawnParticle(
                            org.bukkit.Particle.EXPLOSION_HUGE,
                            target.getLocation(),
                            1,
                            0, 0, 0,
                            0);
                }
            }

            // 5. Build ReactionContext
            ReactionContext reactionCtx = new ReactionContext(
                    attacker, target,
                    ability.getElement(), ability.getType(),
                    stats.damage(),
                    statusesBefore, statusesApplied);

            // 6. Process reactions
            ReactionResult targetResult = reactionService.process(reactionCtx, reactionExecutor);
            for (String name : targetResult.getTriggeredReactionNames()) {
                combinedResult.addReaction(name, targetResult.getTotalBonusDamage());
            }

            // 7. Record damage attribution for kill credit
            damageManager.recordDamage(
                    attacker.getUniqueId(),
                    target.getUniqueId(),
                    ability.getType());
        }

        return combinedResult;
    }

    /**
     * Executes the Maelstrom (Water SPECIAL_CHARGED) two-phase displacement:
     * Phase 1 (immediate): pull targets inward toward caster
     * Phase 2 (5 ticks later): damage + statuses + reactions + push outward
     *
     * @return ReactionResult (only from Phase 2, via scheduled task)
     */
    private ReactionResult executeMaelstrom(Player player, Ability ability, AbilityStats stats,
            AbilityContext context) {
        List<LivingEntity> targets = resolveTargets(player, ability, stats);

        if (targets.isEmpty())
            return new ReactionResult();

        // Phase 1: Pull inward with visual effects
        for (LivingEntity target : targets) {
            // Check if target is still valid
            if (target == null || target.isDead() || !target.isValid()) {
                continue;
            }
            
            Vector playerLoc = player.getLocation().toVector();
            Vector targetLoc = target.getLocation().toVector();
            
            // Check if vectors are valid
            if (Double.isFinite(playerLoc.getX()) && Double.isFinite(playerLoc.getY()) && Double.isFinite(playerLoc.getZ()) &&
                Double.isFinite(targetLoc.getX()) && Double.isFinite(targetLoc.getY()) && Double.isFinite(targetLoc.getZ())) {
                
                Vector diff = playerLoc.subtract(targetLoc);
                if (diff.lengthSquared() > 0) { // Avoid division by zero
                    Vector pullDirection = diff.normalize()
                            .multiply(1.2) // Stronger pull
                            .setY(0.5); // More vertical pull
                    
                    // Check if pullDirection is valid
                    if (Double.isFinite(pullDirection.getX()) && Double.isFinite(pullDirection.getY()) && Double.isFinite(pullDirection.getZ())) {
                        target.setVelocity(target.getVelocity().add(pullDirection));
                        
                        // Visual effect for pull
                        player.getWorld().spawnParticle(
                                org.bukkit.Particle.DRIP_WATER,
                                target.getLocation().add(0, 1, 0),
                                15,
                                0.3, 0.5, 0.3,
                                0.1);
                    }
                }
            }
        }
        
        // Sound effect for maelstrom activation
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_GUARDIAN_AMBIENT, 1.0f, 0.5f);

        // Phase 2: Damage + statuses + reactions + push outward (5 ticks later)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Check if player is still valid
            if (!player.isOnline()) {
                return;
            }
            
            // Visual effect for maelstrom explosion
            player.getWorld().spawnParticle(
                    org.bukkit.Particle.EXPLOSION_HUGE,
                    player.getLocation().add(0, 1, 0),
                    1,
                    0, 0, 0,
                    0);
            
            player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.5f);

            for (LivingEntity target : targets) {
                // Check if target is still valid
                if (target == null || target.isDead() || !target.isValid()) {
                    continue;
                }

                // Snapshot statusesBefore
                Set<StatusType> statusesBefore = statusService.getActiveStatuses(target);

                // Damage with visual effect
                target.damage(stats.damage());
                target.getWorld().playSound(target.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_HURT, 1.0f, 0.5f);

                // Push outward with stronger force
                if (stats.knockback() > 0) {
                    Vector playerLoc = player.getLocation().toVector();
                    Vector targetLoc = target.getLocation().toVector();
                    
                    // Check if vectors are valid
                    if (Double.isFinite(playerLoc.getX()) && Double.isFinite(playerLoc.getY()) && Double.isFinite(playerLoc.getZ()) &&
                        Double.isFinite(targetLoc.getX()) && Double.isFinite(targetLoc.getY()) && Double.isFinite(targetLoc.getZ())) {
                        
                        Vector diff = targetLoc.subtract(playerLoc);
                        if (diff.lengthSquared() > 0) { // Avoid division by zero
                            Vector pushDirection = diff.normalize()
                                    .multiply(stats.knockback() * 1.5) // Stronger push
                                    .setY(0.8); // Higher vertical component
                            
                            // Check if pushDirection is valid
                            if (Double.isFinite(pushDirection.getX()) && Double.isFinite(pushDirection.getY()) && Double.isFinite(pushDirection.getZ())) {
                                target.setVelocity(target.getVelocity().add(pushDirection));
                                
                                // Visual effect for push
                                target.getWorld().spawnParticle(
                                        org.bukkit.Particle.WATER_WAKE,
                                        target.getLocation().add(0, 1, 0),
                                        20,
                                        0.5, 0.5, 0.5,
                                        0.2);
                            }
                        }
                    }
                }

                // Maelstrom applies WET with longer duration
                statusService.applyStatus(target, StatusType.WET, 120);
                Set<StatusType> statusesApplied = EnumSet.of(StatusType.WET);

                // Build ReactionContext and process
                ReactionContext reactionCtx = new ReactionContext(
                        player, target,
                        ability.getElement(), ability.getType(),
                        stats.damage(),
                        statusesBefore, statusesApplied);
                reactionService.process(reactionCtx, reactionExecutor);

                // Record attribution
                damageManager.recordDamage(
                        player.getUniqueId(),
                        target.getUniqueId(),
                        ability.getType());
            }
        }, 10L); // Increased delay for more anticipation

        // Return empty result since Phase 2 is async
        return new ReactionResult();
    }

    // ── Internal Damage Methods (Future Hook Points) ──

    /**
     * Internal method for applying reaction bonus damage.
     * Routes through here so future systems (shields, lifesteal,
     * damage modifiers, analytics) can hook into it.
     */
    private void applyReactionDamageInternal(LivingEntity target, double damage) {
        if (target.isDead())
            return;
        target.damage(damage);
    }

    /**
     * Internal method for applying reaction AoE damage around the caster.
     */
    private void applyReactionAoEInternal(Player caster, double radius, double damage) {
        for (Entity entity : caster.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity living
                    && !entity.equals(caster)
                    && !(entity instanceof Player p && p.getGameMode() == GameMode.SPECTATOR)) {
                if (!living.isDead()) {
                    living.damage(damage);
                }
            }
        }
    }

    // ── Internal Mobility Methods ──

    /**
     * Sole velocity application point for mobility abilities.
     * All setVelocity() calls route through here.
     * Future hook point for velocity modifiers, dash distance scaling, etc.
     */
    private void applySelfVelocityInternal(Player caster, Vector velocity) {
        caster.setVelocity(velocity);
    }

    /**
     * Applies temporary damage immunity to the caster.
     * Used by mobility abilities like Flame Propel.
     * Sets invulnerability ticks on the player.
     */
    private void applyTemporaryImmunity(Player caster, int ticks) {
        caster.setNoDamageTicks(ticks);
    }

    /**
     * Called every 20 ticks by the global passive task.
     * Iterates all online players and calls their element's passive onTick.
     */
    public void tickPassives() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Element element = playerDataManager.getElement(player.getUniqueId());
            if (element == null)
                continue;

            PassiveAbility passive = registry.getPassive(element);
            if (passive == null)
                continue;

            PlayerData data = playerDataManager.getPlayerData(player.getUniqueId());
            if (data == null)
                continue;

            AbilityContext context = new AbilityContext(
                    player, data, plugin,
                    player.getLocation(),
                    System.currentTimeMillis(),
                    damageManager);

            passive.onTick(context);
        }
    }
}
