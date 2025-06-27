package com.slyph.randomteleport.teleport;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public final class LocationUtils {

    private LocationUtils() {}

    public static CompletableFuture<Location> findSafeLocationAsync(
            World world, int range, int attempts, Random random) {

        return tryFind(world, range, attempts, random)
                .thenApply(loc -> loc == null ? world.getSpawnLocation() : loc);
    }

    private static CompletableFuture<Location> tryFind(
            World world, int range, int attempts, Random random) {

        if (attempts <= 0) return CompletableFuture.completedFuture(null);

        int x = random.nextInt(range * 2 + 1) - range;
        int z = random.nextInt(range * 2 + 1) - range;

        return world.getChunkAtAsync(x >> 4, z >> 4, true).thenCompose(chunk -> {
            Location loc = highestSafe(chunk, x, z, world);
            if (loc == null) {
                return tryFind(world, range, attempts - 1, random);
            }
            return CompletableFuture.completedFuture(loc);
        });
    }

    private static Location highestSafe(Chunk chunk, int x, int z, World world) {
        int y = world.getHighestBlockYAt(x, z);
        Location loc = new Location(world, x + 0.5, y, z + 0.5);

        Material block = world.getBlockAt(x, y - 1, z).getType();
        if (!block.isSolid() || block.name().contains("WATER") || block.name().contains("LAVA")) {
            return null;
        }
        return loc.add(0, 1, 0);
    }
}
