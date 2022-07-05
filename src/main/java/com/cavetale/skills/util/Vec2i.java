package com.cavetale.skills.util;

public final record Vec2i(int x, int y) {
    public static Vec2i v(int x, int y) {
        return new Vec2i(x, y);
    }
}
