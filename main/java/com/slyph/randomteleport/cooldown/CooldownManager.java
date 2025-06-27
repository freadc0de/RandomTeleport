package com.slyph.randomteleport.cooldown;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    private final Map<UUID, Long> lastUse = new ConcurrentHashMap<>();
    private volatile int cooldownSeconds;

    public CooldownManager(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public boolean onCooldown(Player p) {
        return getRemaining(p) > 0;
    }

    public long getRemaining(Player p) {
        long last = lastUse.getOrDefault(p.getUniqueId(), 0L);
        long elapsed = (System.currentTimeMillis() / 1000L) - last;
        long remain = cooldownSeconds - elapsed;
        return Math.max(0, remain);
    }

    public void record(Player p) {
        lastUse.put(p.getUniqueId(), System.currentTimeMillis() / 1000L);
    }

    public void setCooldownSeconds(int seconds) {
        this.cooldownSeconds = seconds;
    }
}
