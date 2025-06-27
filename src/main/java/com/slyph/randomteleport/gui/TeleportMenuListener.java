package com.slyph.randomteleport.gui;

import com.slyph.randomteleport.RandomTeleportPlugin;
import com.slyph.randomteleport.teleport.TeleportService;
import com.slyph.randomteleport.util.ColorUtil;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class TeleportMenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getInventory().getHolder() != null) return;

        var plugin = RandomTeleportPlugin.getInstance();
        var cfg    = plugin.getConfig();

        String guiTitle = ColorUtil.stripColor(cfg.getString("gui.title"));
        if (!ColorUtil.stripColor(e.getView().getTitle()).equals(guiTitle)) return;

        e.setCancelled(true);
        player.closeInventory();

        var cdManager = plugin.cooldown();
        if (cdManager.onCooldown(player)) {
            long remain = cdManager.getRemaining(player);
            cfg.getStringList("messages.cooldown").forEach(msg ->
                    player.sendMessage(ColorUtil.color(msg.replace("%time%", String.valueOf(remain))))
            );
            playErrorSound(player, cfg.getConfigurationSection("sounds.error"));
            return;
        }

        TeleportService service = plugin.teleportService();
        int slot   = e.getSlot();
        int normal = cfg.getInt("gui.buttons.normal.slot");
        int nearest= cfg.getInt("gui.buttons.nearest.slot");

        if (slot == normal) {
            service.randomTeleport(player);
        } else if (slot == nearest) {
            service.nearestTeleport(player);
        }
        cdManager.record(player);
    }

    private void playErrorSound(Player p, ConfigurationSection sec) {
        try {
            Sound s = Sound.valueOf(sec.getString("sound", "BLOCK_NOTE_BLOCK_BASS"));
            float v = (float) sec.getDouble("volume", 1.0);
            float pi= (float) sec.getDouble("pitch", 0.8);
            p.playSound(p.getLocation(), s, v, pi);
        } catch (Exception ignored) {}
    }
}
