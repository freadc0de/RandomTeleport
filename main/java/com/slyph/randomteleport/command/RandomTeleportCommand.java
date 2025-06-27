package com.slyph.randomteleport.command;

import com.slyph.randomteleport.gui.TeleportMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RandomTeleportCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!player.hasPermission("randomteleport.use")) {
            player.sendMessage("§cНет прав!");
            return true;
        }
        new TeleportMenu(player).open();
        return true;
    }
}
