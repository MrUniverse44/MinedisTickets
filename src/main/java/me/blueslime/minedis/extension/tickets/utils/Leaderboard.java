package me.blueslime.minedis.extension.tickets.utils;

import java.util.*;

public class Leaderboard {
    public static Set<Map.Entry<String, Integer>> sort(Map<String, Integer> map) {
        return descent(map);
    }

    public static Set<Map.Entry<String, Integer>> descent(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(map.entrySet());

        sortedEntries.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        Map<String, Integer> sortedMap = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : sortedEntries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap.entrySet();
    }
}
