package com.slyph.randomteleport.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static Pattern hexPattern;

    public static void init() {
        hexPattern = Pattern.compile("&([A-Fa-f0-9]{6})");
    }

    public static String color(String text) {
        if (text == null) return "";
        Matcher matcher = hexPattern.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static String stripColor(String s) {
        return ChatColor.stripColor(color(s));
    }
}
