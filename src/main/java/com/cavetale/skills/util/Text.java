package com.cavetale.skills.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.ChatColor;

public final class Text {
    private Text() { }

    public static List<String> wrapLine(String what, int maxLineLength) {
        String[] words = what.split("\\s+");
        List<String> lines = new ArrayList<>();
        if (words.length == 0) return lines;
        StringBuilder line = new StringBuilder(words[0]);
        int lineLength = ChatColor.stripColor(words[0]).length();
        String lastColors = "";
        for (int i = 1; i < words.length; ++i) {
            String word = words[i];
            int wordLength = ChatColor.stripColor(word).length();
            if (lineLength + wordLength + 1 > maxLineLength) {
                String lineStr = lastColors + line.toString();
                lines.add(lineStr);
                lastColors = ChatColor.getLastColors(lineStr);
                line = new StringBuilder(word);
                lineLength = wordLength;
            } else {
                line.append(" ");
                line.append(word);
                lineLength += wordLength + 1;
            }
        }
        if (line.length() > 0) lines.add(lastColors + line.toString());
        return lines;
    }

    public static List<String> wrapLines(String what, int maxLineLength) {
        return Stream.of(what.split("\n"))
            .map(line -> wrapLine(line, maxLineLength))
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    public static List<String> wrapMultiline(String input, int maxLineLength) {
        return flatten(Stream.of(input.split("\n\n"))
                       .map(par -> wrapLines(par, maxLineLength))
                       .collect(Collectors.toList()));
    }

    public static List<String> flatten(List<List<String>> list) {
        List<String> result = new ArrayList<>();
        if (list.isEmpty()) return result;
        result.addAll(list.get(0));
        for (int i = 1; i < list.size(); i += 1) {
            result.add("");
            result.addAll(list.get(i));
        }
        return result;
    }
}
