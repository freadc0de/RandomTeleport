package com.slyph.randomteleport.effect;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.plugin.Plugin;
import java.util.Random;

public class SpiralEffect extends BukkitRunnable {

    private final World world;
    private final Location origin;
    private int step = 0;
    private final Random rnd = new Random();

    public SpiralEffect(Location origin, Plugin plugin) {
        this.world  = origin.getWorld();
        this.origin = origin.clone();
        runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public void run() {
        if (step > 40) { cancel(); return; }
        double radius = 0.2 + 0.05 * step;
        double angle  = 0.3 * step;
        Vector v = new Vector(
                Math.cos(angle) * radius,
                0.1 * step / 10.0,
                Math.sin(angle) * radius
        );
        spawnDust(origin.clone().add(v), true);
        spawnDust(origin.clone().add(v.clone().multiply(-1)), false);
        step++;
    }

    private void spawnDust(Location loc, boolean white) {
        Particle.DustOptions opts = new Particle.DustOptions(
                white ? Color.WHITE : Color.LIME, 1.5f);
        world.spawnParticle(Particle.REDSTONE, loc, 0, opts);
    }
}
