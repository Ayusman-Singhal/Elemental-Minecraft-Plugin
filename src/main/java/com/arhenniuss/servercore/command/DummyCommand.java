package com.arhenniuss.servercore.command;

import com.arhenniuss.servercore.util.ChatUtil;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.attribute.Attribute;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * /dummy — Spawns a training dummy (ArmorStand) for ability testing.
 * /dummy remove — Removes the nearest training dummy within 10 blocks.
 *
 * Dummies are tagged with PersistentDataContainer key "training_dummy"
 * so they can be identified by DummyListener and target resolution.
 */
public class DummyCommand implements CommandExecutor, TabCompleter {

    private final NamespacedKey dummyKey;

    public DummyCommand(JavaPlugin plugin) {
        this.dummyKey = new NamespacedKey(plugin, "training_dummy");
    }

    public NamespacedKey getDummyKey() {
        return dummyKey;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("servercore.dummy")) {
            player.sendMessage(ChatUtil.format("§cYou don't have permission to use this command."));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("remove")) {
            return handleRemove(player);
        }

        return handleSpawn(player);
    }

    private boolean handleSpawn(Player player) {
        Location loc = player.getLocation().add(
                player.getLocation().getDirection().normalize().multiply(3).setY(0));
        loc.setY(player.getLocation().getY());

        ArmorStand dummy = (ArmorStand) player.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);

        // Configure the dummy
        dummy.setGravity(false);
        dummy.setInvulnerable(false); // Must take damage for display
        dummy.setVisible(true);
        dummy.customName(net.kyori.adventure.text.Component.text("Training Dummy",
                net.kyori.adventure.text.format.NamedTextColor.YELLOW));
        dummy.setCustomNameVisible(true);
        dummy.setBasePlate(false);
        dummy.setArms(true);
        dummy.setCanPickupItems(false);

        // Tag it
        dummy.getPersistentDataContainer().set(dummyKey, PersistentDataType.BYTE, (byte) 1);

        // Set max health high — ArmorStands have 20 HP by default
        // We use the tag to prevent death instead
        double maxHealth = dummy.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        dummy.setHealth(maxHealth);

        player.sendMessage(ChatUtil.format("§aTraining Dummy spawned!"));
        return true;
    }

    private boolean handleRemove(Player player) {
        ArmorStand nearest = null;
        double nearestDist = 10.0;

        for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
            if (entity instanceof ArmorStand stand
                    && stand.getPersistentDataContainer().has(dummyKey, PersistentDataType.BYTE)) {
                double dist = entity.getLocation().distance(player.getLocation());
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = stand;
                }
            }
        }

        if (nearest != null) {
            nearest.remove();
            player.sendMessage(ChatUtil.format("§aTraining Dummy removed."));
        } else {
            player.sendMessage(ChatUtil.format("§cNo Training Dummy found within 10 blocks."));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("remove");
        }
        return List.of();
    }
}
