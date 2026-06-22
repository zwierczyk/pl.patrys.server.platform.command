package dev.patrys.custommenu.utils;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ColorUtil {

    public static String color(String text) {
        if (text == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> color(List<String> text) {
        return text.stream().map(ColorUtil::color).collect(Collectors.toList());
    }

    public static List<String> color(String... text) {
        return Arrays.stream(text).map(ColorUtil::color).collect(Collectors.toList());
    }
}