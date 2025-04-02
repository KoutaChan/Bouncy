package me.koutachan.bouncy.utils;

public class FormatUtils {
    public static String formatTick(int tick) {
        return String.format("%.2f", tick / 20.0);
    }
}