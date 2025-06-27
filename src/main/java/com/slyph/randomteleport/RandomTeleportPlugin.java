package com.slyph.randomteleport;

import com.slyph.randomteleport.animation.TitleAnimation;
import com.slyph.randomteleport.command.RandomTeleportCommand;
import com.slyph.randomteleport.cooldown.CooldownManager;
import com.slyph.randomteleport.gui.TeleportMenuListener;
import com.slyph.randomteleport.teleport.TeleportService;
import com.slyph.randomteleport.update.UpdateChecker;
import com.slyph.randomteleport.util.ColorUtil;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class RandomTeleportPlugin extends JavaPlugin {

    private static RandomTeleportPlugin instance;
    public static RandomTeleportPlugin getInstance() { return instance; }

    private BukkitAudiences adventure;
    private TeleportService teleportService;
    private CooldownManager cooldownManager;
    private TitleAnimation  titleAnimation;
    private UpdateChecker   updateChecker;

    public BukkitAudiences adventure()       { return adventure; }
    public TeleportService teleportService() { return teleportService; }
    public CooldownManager cooldown()        { return cooldownManager; }
    public TitleAnimation  titleAnim()       { return titleAnimation; }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        ColorUtil.init();

        adventure       = BukkitAudiences.create(this);
        teleportService = new TeleportService(this);
        titleAnimation  = new TitleAnimation(this);
        cooldownManager = new CooldownManager(getConfig().getInt("settings.cooldown-seconds", 3600));
        updateChecker   = new UpdateChecker(this);

        getCommand("rtp").setExecutor(new RandomTeleportCommand());

        getServer().getPluginManager().registerEvents(new TeleportMenuListener(), this);

        updateChecker.checkAsync();
        printBanner();
    }

    @Override
    public void onDisable() {
        if (adventure != null) adventure.close();
    }

    public void reload() {
        reloadConfig();
        ColorUtil.init();
        cooldownManager.setCooldownSeconds(getConfig().getInt("settings.cooldown-seconds", 3600));
        titleAnimation.loadFromConfig();
        updateChecker.checkAsync();
        getLogger().info("Config reloaded.");
    }

    private void printBanner() {
        String version = getDescription().getVersion();
        String storage = "Local";
        String cd      = String.valueOf(getConfig().getInt("settings.cooldown-seconds", 3600));

        List<String> lines = List.of(
                "",
                "RandomTeleport v" + version,
                "Cooldown     : " + cd + " s",
                ""
        );
        int max = lines.stream().mapToInt(String::length).max().orElse(0);
        String border = "═".repeat(max + 4);

        getLogger().info(border);
        lines.forEach(l -> getLogger().info("  " + l));
        getLogger().info(border);
    }
}
