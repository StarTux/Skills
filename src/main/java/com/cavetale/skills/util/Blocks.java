package com.cavetale.skills.util;

import org.bukkit.block.Block;

public final class Blocks {
    private Blocks() { }

    public static String toString(Block block) {
        return block.getWorld().getName() + ":" + block.getX() + "," + block.getY() + "," + block.getZ();
    }
}
