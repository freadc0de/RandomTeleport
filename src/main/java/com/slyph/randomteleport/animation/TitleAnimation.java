package com.slyph.randomteleport.animation;

import com.slyph.randomteleport.RandomTeleportPlugin;
import com.slyph.randomteleport.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TitleAnimation {

    private final RandomTeleportPlugin plugin;
    private List<String> titles;
    private List<String> subtitles;
    private int interval;
    private Sound tickSound;
    private float tickVol, tickPitch;
    private final Map<UUID, BukkitTask> running = new ConcurrentHashMap<>();

    public TitleAnimation(RandomTeleportPlugin plugin) {
        this.plugin = plugin;
        loadFromConfig();
    }

    public void loadFromConfig() {
        var cfg = plugin.getConfig().getConfigurationSection("title-animation");
        this.interval   = Math.max(1, cfg.getInt("interval-ticks", 20));
        this.titles     = new ArrayList<>(cfg.getStringList("titles"));
        this.subtitles  = new ArrayList<>(cfg.getStringList("subtitles"));
        if (titles.isEmpty())    titles.add("&aТелепортация");
        if (subtitles.isEmpty()) subtitles.add("&7Ожидайте...");

        var snd = plugin.getConfig().getConfigurationSection("sounds.animation-tick");
        try {
            tickSound  = Sound.valueOf(snd.getString("sound", "BLOCK_LEVER_CLICK"));
            tickVol    = (float) snd.getDouble("volume", 0.8);
            tickPitch  = (float) snd.getDouble("pitch", 1.2);
        } catch (Exception ex) { tickSound = null; }
    }

    public void start(Player p) {
        stop(p);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int frame = 0;
            @Override public void run() {
                if (!p.isOnline()) { stop(p); return; }
                String t  = ColorUtil.color(titles.get(frame % titles.size()));
                String st = ColorUtil.color(subtitles.get(frame % subtitles.size()));
                p.sendTitle(t, st, 0, interval, 0);

                if (tickSound != null) {
                    p.playSound(p.getLocation(), tickSound, tickVol, tickPitch);
                }
                frame++;
            }
        }, 0L, interval);
        running.put(p.getUniqueId(), task);
    }

    public void stop(Player p) {
        BukkitTask task = running.remove(p.getUniqueId());
        if (task != null) task.cancel();
    }
}
