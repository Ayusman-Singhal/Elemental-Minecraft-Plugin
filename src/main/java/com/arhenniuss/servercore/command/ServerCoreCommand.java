package com.arhenniuss.servercore.command;

import com.arhenniuss.servercore.combat.AbilityDebugManager;
import com.arhenniuss.servercore.config.AbilityBalanceConfig;
import com.arhenniuss.servercore.npc.ElementAssignerNPC;
import com.arhenniuss.servercore.player.PlayerData;
import com.arhenniuss.servercore.player.PlayerDataManager;
import com.arhenniuss.servercore.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ServerCoreCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "setassigner", "reset", "debug", "reload");

    private final ElementAssignerNPC assignerNPC;
    private final PlayerDataManager playerDataManager;
    private final AbilityDebugManager debugManager;
    private final AbilityBalanceConfig balanceConfig;

    public ServerCoreCommand(ElementAssignerNPC assignerNPC, PlayerDataManager playerDataManager,
            AbilityDebugManager debugManager, AbilityBalanceConfig balanceConfig) {
        this.assignerNPC = assignerNPC;
        this.playerDataManager = playerDataManager;
        this.debugManager = debugManager;
        this.balanceConfig = balanceConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatUtil.format("§eUsage: /servercore <setassigner|reset|debug|reload>"));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "setassigner" -> handleSetAssigner(sender);
            case "reset" -> handleReset(sender, args);
            case "debug" -> handleDebug(sender);
            case "reload" -> handleReload(sender);
            default -> sender.sendMessage(ChatUtil.format(
                    "§cUnknown subcommand. Use: setassigner, reset, debug, reload"));
        }

        return true;
    }

    private void handleSetAssigner(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatUtil.format("§cOnly players can use this command."));
            return;
        }

        assignerNPC.spawn(player.getLocation());
        player.sendMessage(ChatUtil.format("§aElement Assigner NPC spawned at your location."));
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatUtil.format("§cUsage: /servercore reset <player>"));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatUtil.format("§cPlayer §e" + args[1] + " §cis not online."));
            return;
        }

        PlayerData data = playerDataManager.getPlayerData(target.getUniqueId());
        if (data == null) {
            sender.sendMessage(ChatUtil.format("§cNo data found for §e" + target.getName() + "§c."));
            return;
        }

        data.setElement(null);
        playerDataManager.savePlayer(target.getUniqueId());

        sender.sendMessage(ChatUtil.format("§aReset element for §e" + target.getName() + "§a."));
        target.sendMessage(ChatUtil.format("§eYour element has been reset by an admin."));
    }

    private void handleDebug(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatUtil.format("§cOnly players can use this command."));
            return;
        }

        boolean enabled = debugManager.toggle(player);
        player.sendMessage(ChatUtil.format(
                enabled ? "§aDebug mode §eenabled§a." : "§cDebug mode §edisabled§c."));
    }

    private void handleReload(CommandSender sender) {
        balanceConfig.reload();
        sender.sendMessage(ChatUtil.format("§aAbility balance config reloaded."));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            String prefix = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
