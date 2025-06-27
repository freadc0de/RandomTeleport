package com.slyph.randomteleport.command;

import com.slyph.randomteleport.RandomTeleportPlugin;
import com.slyph.randomteleport.gui.TeleportMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RandomTeleportCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        RandomTeleportPlugin plugin = RandomTeleportPlugin.getInstance();

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("randomteleport.reload")) {
                sender.sendMessage("§cНет прав!");
                return true;
            }
            plugin.reload();
            sender.sendMessage("§aRandomTeleport перезагружен.");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cТолько для игроков.");
            return true;
        }
        if (!player.hasPermission("randomteleport.use")) {
            player.sendMessage("§cНет прав!");
            return true;
        }
        new TeleportMenu(player).open();
        return true;
    }
}
