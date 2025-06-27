package com.slyph.randomteleport;

import com.slyph.randomteleport.animation.TitleAnimation;
import com.slyph.randomteleport.command.RandomTeleportCommand;
import com.slyph.randomteleport.command.ReloadCommand;
import com.slyph.randomteleport.cooldown.CooldownManager;
import com.slyph.randomteleport.gui.TeleportMenuListener;
import com.slyph.randomteleport.teleport.TeleportService;
import com.slyph.randomteleport.update.UpdateChecker;
import com.slyph.randomteleport.util.ColorUtil;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

public final class RandomTeleportPlugin extends JavaPlugin {

    private static RandomTeleportPlugin instance;
    private BukkitAudiences adventure;
    private TeleportService teleportService;
    private CooldownManager cooldownManager;
    private TitleAnimation  titleAnimation;
    private UpdateChecker   updateChecker;

    public static RandomTeleportPlugin getInstance() { return instance; }
    public BukkitAudiences adventure()        { return adventure; }
    public TeleportService teleportService()  { return teleportService; }
    public CooldownManager cooldown()         { return cooldownManager; }
    public TitleAnimation  titleAnim()        { return titleAnimation; }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        ColorUtil.init();

        adventure       = BukkitAudiences.create(this);
        teleportService = new TeleportService(this);
        titleAnimation  = new TitleAnimation(this);

        int cd = getConfig().getInt("settings.cooldown-seconds", 3600);
        cooldownManager = new CooldownManager(cd);

        updateChecker   = new UpdateChecker(this);

        getCommand("rtp").setExecutor(new RandomTeleportCommand());
        getCommand("rtpreload").setExecutor(new ReloadCommand());

        getServer().getPluginManager().registerEvents(new TeleportMenuListener(), this);

        updateChecker.checkAsync();
        getLogger().info("RandomTeleport enabled.");
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
        getLogger().info("RandomTeleport config reloaded.");
    }
}
