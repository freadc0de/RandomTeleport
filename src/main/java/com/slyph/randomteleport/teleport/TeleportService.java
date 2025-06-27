package com.slyph.randomteleport.teleport;

import com.slyph.randomteleport.RandomTeleportPlugin;
import com.slyph.randomteleport.effect.SpiralEffect;
import com.slyph.randomteleport.util.ColorUtil;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class TeleportService {

    private final RandomTeleportPlugin plugin;
    private final Random random = new Random();
    private final Set<UUID> waiting = Collections.synchronizedSet(new HashSet<>());

    public TeleportService(RandomTeleportPlugin plugin) { this.plugin = plugin; }

    public void randomTeleport(Player p)  { startProcess(p, false); }
    public void nearestTeleport(Player p) { startProcess(p, true); }

    public boolean isWaiting(Player p) { return waiting.contains(p.getUniqueId()); }

    public void cancelWaiting(Player p) {
        if (!waiting.remove(p.getUniqueId())) return;

        plugin.titleAnim().stop(p);
        sendCancel(p);
    }

    private void startProcess(Player p, boolean avoidPlayers) {

        World world = Bukkit.getWorld(plugin.getConfig().getString("settings.world"));

        if (avoidPlayers && world.getPlayers().stream().allMatch(pl -> pl.equals(p))) {
            msg(p, plugin.getConfig().getStringList("messages.no_players_online"));
            playSound(p, "error");
            return;
        }

        waiting.add(p.getUniqueId());

        plugin.titleAnim().start(p);
        msg(p, plugin.getConfig().getStringList("messages.teleporting"));

        int attempts = plugin.getConfig().getInt("settings.attempts");
        int range    = plugin.getConfig().getInt("settings.max-range", 10000);

        LocationUtils.findSafeLocationAsync(world, range, attempts, random)
                .thenAccept(loc -> Bukkit.getScheduler().runTask(plugin, () ->
                        handleFoundLocation(p, loc, avoidPlayers)));
    }

    private void handleFoundLocation(Player p, Location loc, boolean avoidPlayers) {

        if (!waiting.contains(p.getUniqueId())) return;

        if (avoidPlayers && tooClose(loc, p)) {
            startProcess(p, true);
            return;
        }

        int warmup = plugin.getConfig().getInt("settings.warmup-ticks", 40);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!waiting.remove(p.getUniqueId())) return;
            finishTeleport(p, loc);
        }, warmup);
    }

    private void finishTeleport(Player p, Location loc) {

        p.teleportAsync(loc).thenRun(() -> {
            plugin.titleAnim().stop(p);

            msg(p, plugin.getConfig().getStringList("messages.teleported"));

            var sec = plugin.getConfig().getConfigurationSection("titles.success");
            p.sendTitle(ColorUtil.color(sec.getString("title", "&aТелепортация успешна")),
                    ColorUtil.color(sec.getString("subtitle", "&7Приятной игры!")),
                    0, 40, 10);

            playSound(p, "success");
            new SpiralEffect(loc, plugin);

            plugin.cooldown().record(p);
        });
    }

    private void sendCancel(Player p) {
        var cfg = plugin.getConfig();

        cfg.getStringList("messages.cancelled")
                .forEach(m -> p.sendMessage(ColorUtil.color(m)));

        String title    = ColorUtil.color(cfg.getString("titles.cancel.title",
                "&cТелепортация прервана"));
        String subtitle = ColorUtil.color(cfg.getString("titles.cancel.subtitle",
                "&7Вы пошевелились!"));
        p.sendTitle(title, subtitle, 0, 40, 10);      // 2 с показа
        playSound(p, "error");
    }

    private boolean tooClose(Location loc, Player me) {
        return loc.getWorld().getPlayers().stream()
                .filter(pl -> !pl.equals(me))
                .anyMatch(pl -> pl.getLocation().distanceSquared(loc) < 250 * 250);
    }

    private void msg(Player p, List<String> list) {
        list.forEach(m -> p.sendMessage(ColorUtil.color(m)));
    }

    private void playSound(Player p, String key) {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("sounds." + key);
        try {
            Sound s = Sound.valueOf(sec.getString("sound"));
            float v = (float) sec.getDouble("volume", 1.0);
            float pi= (float) sec.getDouble("pitch", 1.0);
            p.playSound(p.getLocation(), s, v, pi);
        } catch (Exception ignored) {}
    }
}
