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
        var cfg = RandomTeleportPlugin.getInstance().getConfig();

        int size   = cfg.getInt("gui.size", 45);
        String raw = cfg.getString("gui.title", "&a&lRTP");
        inv = Bukkit.createInventory(null, size, ColorUtil.color(raw));

        ConfigurationSection bg = cfg.getConfigurationSection("gui.background-item");
        ItemStack bgStack = plainItem(bg);

        for (int i = 0; i < size; i++) inv.setItem(i, bgStack);

        placeButton("normal");
        placeButton("nearest");
    }

    private ItemStack plainItem(ConfigurationSection sec) {
        ItemStack item = new ItemStack(Material.valueOf(sec.getString("material", "GRAY_STAINED_GLASS_PANE")));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.color(sec.getString("name", " ")));
        item.setItemMeta(meta);
        return item;
    }

    private void placeButton(String id) {
        ConfigurationSection root = RandomTeleportPlugin.getInstance()
                .getConfig().getConfigurationSection("gui.buttons." + id);

        ConfigurationSection hl = root.getConfigurationSection("highlight");
        ItemStack highlight = new ItemStack(Material.valueOf(hl.getString("material", "LIGHT_BLUE_STAINED_GLASS_PANE")));
        ItemMeta hlMeta = highlight.getItemMeta();
        hlMeta.setDisplayName(" ");
        highlight.setItemMeta(hlMeta);

        for (int slot : hl.getIntegerList("slots")) {
            if (slot >= 0 && slot < inv.getSize()) inv.setItem(slot, highlight);
        }

        int slot = root.getInt("slot");
        ItemStack item = new ItemStack(Material.valueOf(root.getString("material", "ENDER_PEARL")));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.color(root.getString("name")));
        List<String> lore = root.getStringList("lore").stream()
                .map(ColorUtil::color).collect(Collectors.toList());
        meta.setLore(lore);
        item.setItemMeta(meta);

        inv.setItem(slot, item);
    }

    public void open() { player.openInventory(inv); }
    public Inventory inventory() { return inv; }
}
