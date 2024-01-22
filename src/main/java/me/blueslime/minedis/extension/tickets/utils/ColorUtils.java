package me.blueslime.minedis.extension.tickets.utils;

import java.awt.*;
import java.util.Locale;

public class ColorUtils {
    private static boolean isNumber(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Converts a hex string to a color. If it can't be converted null is returned.
     * @param hex (i.e. #CCCCCCFF #CCCCCC)
     * @return Color
     */
    private static Color hexadecimal(String hex) {
        hex = hex.replace("#", "");
        switch (hex.length()) {
            case 6:
                return new Color(
                        Integer.valueOf(hex.substring(0, 2), 16),
                        Integer.valueOf(hex.substring(2, 4), 16),
                        Integer.valueOf(hex.substring(4, 6), 16));
            case 8:
                return new Color(
                        Integer.valueOf(hex.substring(0, 2), 16),
                        Integer.valueOf(hex.substring(2, 4), 16),
                        Integer.valueOf(hex.substring(4, 6), 16),
                        Integer.valueOf(hex.substring(6, 8), 16));
        }
        return Color.WHITE;
    }

    public static Color getColor(String color) {
        if (isNumber(color)) {
            return new Color(Integer.parseInt(color));
        }
        if (color.contains("#")) {
            return hexadecimal(color);
        }
        if (color.contains(",")) {
            String[] split = color.replace(" ", "").split(",");

            if (split.length == 3) {
                return new Color(
                        Integer.parseInt(split[0]),
                        Integer.parseInt(split[1]),
                        Integer.parseInt(split[2])
                );
            } else if (split.length == 2) {
                return new Color(
                        Integer.parseInt(split[0]),
                        Integer.parseInt(split[1]),
                        0
                );
            } else if (split.length >= 4) {
                return new Color(
                        Integer.parseInt(split[0]),
                        Integer.parseInt(split[1]),
                        Integer.parseInt(split[2]),
                        Integer.parseInt(split[3])
                );
            }else {
                return new Color(
                        Integer.parseInt(split[0]),
                        0,
                        0
                );
            }
        }

        switch (color.toLowerCase(Locale.ENGLISH)) {
            default:
            case "&e":
            case "e":
            case "yellow":
                return Color.YELLOW;
            case "white":
            case "&r":
            case "&f":
            case "r":
            case "f":
                return Color.WHITE;
            case "light-gray":
            case "light_gray":
            case "light gray":
            case "7":
            case "&7":
                return Color.LIGHT_GRAY;
            case "gray":
                return Color.GRAY;
            case "dark_gray":
            case "dark gray":
            case "dark-gray":
            case "&8":
            case "8":
                return Color.DARK_GRAY;
            case "black":
            case "0":
            case "&0":
                return Color.BLACK;
            case "red":
            case "&4":
            case "4":
                return Color.RED;
            case "&d":
            case "d":
            case "pink":
                return Color.PINK;
            case "&6":
            case "6":
            case "orange":
                return Color.ORANGE;
            case "green":
            case "dark green":
            case "dark-green":
            case "dark_green":
            case "&2":
            case "2":
            case "a":
            case "&a":
            case "lime":
                return Color.GREEN;
            case "magenta":
            case "&5":
            case "5":
                return Color.MAGENTA;
            case "cyan":
            case "&b":
            case "b":
                return Color.CYAN;
            case "blue":
            case "&1":
            case "&9":
            case "&3":
            case "1":
            case "9":
            case "3":
                return Color.BLUE;
        }
    }
}
