package com.slyph.randomteleport.gui;

import com.slyph.randomteleport.RandomTeleportPlugin;
import com.slyph.randomteleport.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class TeleportMenu {

    private final Player player;
    private final Inventory inv;

    public TeleportMenu(Player player) {
        this.player = player;

        var cfg   = RandomTeleportPlugin.getInstance().getConfig();
        int size  = cfg.getInt("gui.size", 45);
        String ttl = ColorUtil.color(cfg.getString("gui.title", "&a&lRTP"));

        inv = Bukkit.createInventory(null, size, ttl);

        drawChessBackground(cfg.getConfigurationSection("gui.background"));

        placeButton("normal");
        placeButton("pattern");
        placeButton("nearest");
    }

    private void drawChessBackground(ConfigurationSection sec) {
        ItemStack even = pane(sec.getString("primary",   "GRAY_STAINED_GLASS_PANE"));
        ItemStack odd  = pane(sec.getString("secondary", "BLACK_STAINED_GLASS_PANE"));

        for (int slot = 0; slot < inv.getSize(); slot++) {
            int row = slot / 9, col = slot % 9;
            inv.setItem(slot, ((row + col) & 1) == 0 ? even : odd);
        }
    }

    private void placeButton(String id) {
        ConfigurationSection btn = RandomTeleportPlugin.getInstance()
                .getConfig().getConfigurationSection("gui.buttons." + id);

        if (btn.contains("highlight")) {
            var hl = btn.getConfigurationSection("highlight");
            ItemStack glass = pane(hl.getString("material", "LIGHT_BLUE_STAINED_GLASS_PANE"));
            hl.getIntegerList("slots").forEach(s -> inv.setItem(s, glass));
        }

        ItemStack item = new ItemStack(Material.valueOf(btn.getString("material", "PAPER")));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.color(btn.getString("name", "")));

        List<String> lore = btn.getStringList("lore").stream()
                .map(ColorUtil::color).collect(Collectors.toList());
        meta.setLore(lore);

        if (id.equals("pattern")) {
            meta.setLocalizedName("");
        }

        item.setItemMeta(meta);
        inv.setItem(btn.getInt("slot", 0), item);
    }

    private ItemStack pane(String material) {
        ItemStack it = new ItemStack(Material.valueOf(material));
        ItemMeta im = it.getItemMeta(); im.setDisplayName(" "); it.setItemMeta(im);
        return it;
    }

    public void open() { player.openInventory(inv); }
}
