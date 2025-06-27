package com.slyph.randomteleport.teleport;

import com.slyph.randomteleport.RandomTeleportPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.Random;

public class TeleportService {

    private final RandomTeleportPlugin plugin;
    private final Random random = new Random();

    public TeleportService(RandomTeleportPlugin plugin) {
        this.plugin = plugin;
    }

    public void randomTeleport(Player p) {
        World world = targetWorld();
        findAndTeleport(p, world, false);
    }

    public void nearestTeleport(Player p) {
        World world = targetWorld();
        if (world.getPlayers().stream().filter(pl -> !pl.equals(p)).toList().isEmpty()) {
            plugin.getConfig().getStringList("messages.no_players_online")
                    .forEach(s -> p.sendMessage(com.slyph.randomteleport.util.ColorUtil.color(s)));
            playErrorSound(p);      // ← звук ошибки
            return;
        }
        findAndTeleport(p, world, true);
    }

    private World targetWorld() {
        return Bukkit.getWorld(plugin.getConfig().getString("settings.world"));
    }

    private void findAndTeleport(Player p, World world, boolean avoidPlayers) {
        int attempts = plugin.getConfig().getInt("settings.attempts");
        int range    = plugin.getConfig().getInt("settings.max-range", 10000);

        plugin.titleAnim().start(p);
        plugin.getConfig().getStringList("messages.teleporting")
                .forEach(msg -> p.sendMessage(com.slyph.randomteleport.util.ColorUtil.color(msg)));

        LocationUtils.findSafeLocationAsync(world, range, attempts, random).thenAccept(loc -> {
            if (avoidPlayers && distanceToNearest(loc) < 250) {
                findAndTeleport(p, world, true);
                return;
            }
            p.teleportAsync(loc).thenRun(() -> {
                plugin.titleAnim().stop(p);

                plugin.getConfig().getStringList("messages.teleported")
                        .forEach(msg -> p.sendMessage(com.slyph.randomteleport.util.ColorUtil.color(msg)));
                playSuccessSound(p);
            });
        });
    }

    private double distanceToNearest(Location loc) {
        return loc.getWorld().getPlayers().stream()
                .map(Player::getLocation)
                .min(Comparator.comparingDouble(loc::distance))
                .map(loc::distance)
                .orElse(Double.MAX_VALUE);
    }

    private void playSuccessSound(Player p) {
        playFromSection(p, plugin.getConfig().getConfigurationSection("sounds.success"));
    }
    private void playErrorSound(Player p) {
        playFromSection(p, plugin.getConfig().getConfigurationSection("sounds.error"));
    }
    private void playFromSection(Player p, ConfigurationSection sec) {
        try {
            Sound s  = Sound.valueOf(sec.getString("sound"));
            float v  = (float) sec.getDouble("volume", 1.0);
            float pi = (float) sec.getDouble("pitch", 1.0);
            p.playSound(p.getLocation(), s, v, pi);
        } catch (Exception ignored) {}
    }
}
