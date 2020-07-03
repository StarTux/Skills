package com.cavetale.skills;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class Rnd {
    private Rnd() { }

    public static Random random() {
        return ThreadLocalRandom.current();
    }

    public static <T> T getEntry(List<T> list) {
        if (Objects.requireNonNull(list).isEmpty()) {
            throw new IllegalStateException("list is empty");
        }
        if (list.size() == 1) return list.get(0);
        int index = getInt(list.size());
        return list.get(index);
    }

    public static <T> T getEntry(T... ts) {
        int index = getInt(ts.length);
        return ts[index];
    }

    public static <T> List<T> shuffle(List<T> list) {
        Collections.shuffle(list, random());
        return list;
    }

    public static int getInt(int max) {
        return random().nextInt(max);
    }

    public static int getInt(int min, int max) {
        return min + random().nextInt(max - min);
    }

    public static double getDouble() {
        return random().nextDouble();
    }
}
