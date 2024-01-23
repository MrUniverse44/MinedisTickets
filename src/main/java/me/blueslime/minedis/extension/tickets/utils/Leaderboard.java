package me.blueslime.minedis.extension.tickets.utils;

import java.util.*;

public class Leaderboard {
    public static Set<Map.Entry<String, Integer>> sort(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(map.entrySet());

        sortedEntries.sort(Map.Entry.comparingByValue());

        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap.entrySet();
    }
}
