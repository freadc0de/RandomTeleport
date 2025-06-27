package com.slyph.randomteleport.command;

import com.slyph.randomteleport.RandomTeleportPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("randomteleport.reload")) {
            sender.sendMessage("§cНет прав!");
            return true;
        }

        RandomTeleportPlugin.getInstance().reload();
        sender.sendMessage("§aПлагин RandomTeleport перезагружен!");
        return true;
    }
}
