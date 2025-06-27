package com.slyph.randomteleport.listener;

import com.slyph.randomteleport.RandomTeleportPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveCancelListener implements Listener {

    private final RandomTeleportPlugin plugin;

    public MoveCancelListener(RandomTeleportPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!plugin.teleportService().isWaiting(e.getPlayer())) return;

        var f = e.getFrom();
        var t = e.getTo();
        if (f.getX() != t.getX() || f.getY() != t.getY() || f.getZ() != t.getZ()) {
            plugin.teleportService().cancelWaiting(e.getPlayer());
        }
    }
}
