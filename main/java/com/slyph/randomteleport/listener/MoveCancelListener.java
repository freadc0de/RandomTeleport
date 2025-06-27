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
        if (e.getFrom().distanceSquared(e.getTo()) < 0.01) return;      // фактическое движение?
        var service = plugin.teleportService();
        if (!service.isWaiting(e.getPlayer())) return;                  // не в ожидании
        service.cancelWaiting(e.getPlayer());                           // отмена
    }
}
