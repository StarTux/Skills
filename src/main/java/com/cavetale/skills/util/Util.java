package com.cavetale.skills.util;

import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Misc utility class.
 */
public final class Util {
    private Util() { }

    /**
     * Figure out if player is in any valid GameMode to play for skill
     * points.
     */
    public static boolean playMode(@NonNull Player player) {
        switch (player.getGameMode()) {
        case SURVIVAL:
        case ADVENTURE:
            return true;
        default:
            return false;
        }
    }

    /**
     * Spawn in some exp.
     */
    public static ExperienceOrb exp(Location location, int amount) {
        if (amount <= 0) return null;
        return location.getWorld().spawn(location,
                                         ExperienceOrb.class,
                                         orb -> orb.setExperience(amount));
    }

    /**
     * Get current time in seconds.
     */
    public static long now() {
        return System.nanoTime() / 1000000000L;
    }

    /**
     * Get correct item in hand for EquipmentSlot.
     */
    public static ItemStack getHand(@NonNull Player player,
                             @NonNull EquipmentSlot slot) {
        switch (slot) {
        case HAND:
            return player.getInventory().getItemInMainHand();
        case OFF_HAND:
            return player.getInventory().getItemInOffHand();
        default:
            return null;
        }
    }
}
