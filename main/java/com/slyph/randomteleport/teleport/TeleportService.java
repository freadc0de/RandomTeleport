package com.slyph.randomteleport.teleport;

import com.slyph.randomteleport.RandomTeleportPlugin;
import com.slyph.randomteleport.effect.SpiralEffect;
import com.slyph.randomteleport.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TeleportService {

    private final RandomTeleportPlugin plugin;
    private final Random random = new Random();

    private final Map<UUID, CompletableFuture<Location>> waiting = new HashMap<>();

    public TeleportService(RandomTeleportPlugin plugin) {
        this.plugin = plugin;
    }

    public void randomTeleport(Player p)  { startProcess(p, false); }

    public void nearestTeleport(Player p) {
        World world = Bukkit.getWorld(plugin.getConfig().getString("settings.world"));
        boolean othersOnline = world.getPlayers().stream().anyMatch(pl -> !pl.equals(p));

        if (!othersOnline) {
            plugin.getConfig().getStringList("messages.no_players_online")
                    .forEach(m -> p.sendMessage(ColorUtil.color(m)));
            playSound(p, "error");
            return;
        }
        startProcess(p, true);
    }

    public boolean isWaiting(Player p) { return waiting.containsKey(p.getUniqueId()); }

    public void cancelWaiting(Player p) {
        CompletableFuture<Location> fut = waiting.remove(p.getUniqueId());
        if (fut != null) fut.cancel(true);
        plugin.titleAnim().stop(p);
        sendCancel(p);
    }

    private void startProcess(Player p, boolean avoidPlayers) {
        int attempts = plugin.getConfig().getInt("settings.attempts");
        int range    = plugin.getConfig().getInt("settings.max-range", 10000);
        World world  = Bukkit.getWorld(plugin.getConfig().getString("settings.world"));

        plugin.titleAnim().start(p);
        plugin.getConfig().getStringList("messages.teleporting")
                .forEach(m -> p.sendMessage(ColorUtil.color(m)));

        CompletableFuture<Location> future =
                LocationUtils.findSafeLocationAsync(world, range, attempts, random);

        waiting.put(p.getUniqueId(), future);

        future.thenAccept(loc -> Bukkit.getScheduler().runTask(plugin, () -> {
            if (!waiting.containsKey(p.getUniqueId())) return;
            if (avoidPlayers && tooClose(loc, p)) {
                waiting.remove(p.getUniqueId());
                startProcess(p, true);
                return;
            }
            waiting.remove(p.getUniqueId());
            finishTeleport(p, loc);
        }));
    }

    private boolean tooClose(Location loc, Player me) {
        return loc.getWorld().getPlayers().stream()
                .filter(pl -> !pl.equals(me))
                .anyMatch(pl -> pl.getLocation().distanceSquared(loc) < 250 * 250);
    }

    private void finishTeleport(Player p, Location loc) {
        p.teleportAsync(loc).thenRun(() -> {
            plugin.titleAnim().stop(p);

            plugin.getConfig().getStringList("messages.teleported")
                    .forEach(m -> p.sendMessage(ColorUtil.color(m)));

            var sec = plugin.getConfig().getConfigurationSection("titles.success");
            String title    = ColorUtil.color(sec.getString("title", "&aТелепортация успешна"));
            String subtitle = ColorUtil.color(sec.getString("subtitle", "&7Приятной игры!"));
            p.sendTitle(title, subtitle, 0, 40, 10);

            playSound(p, "success");
            new SpiralEffect(loc, plugin);
        });
    }

    private void sendCancel(Player p) {
        var cfg = plugin.getConfig();
        cfg.getStringList("messages.cancelled")
                .forEach(m -> p.sendMessage(ColorUtil.color(m)));

        String t  = ColorUtil.color(cfg.getString("titles.cancel.title"));
        String st = ColorUtil.color(cfg.getString("titles.cancel.subtitle"));
        p.sendTitle(t, st, 0, 40, 10);

        playSound(p, "error");
    }

    private void playSound(Player p, String key) {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("sounds." + key);
        try {
            Sound s  = Sound.valueOf(sec.getString("sound"));
            float v  = (float) sec.getDouble("volume", 1.0);
            float pi = (float) sec.getDouble("pitch", 1.0);
            p.playSound(p.getLocation(), s, v, pi);
        } catch (Exception ignored) {}
    }
}
