package com.cavetale.skills;

import com.winthier.exploits.Exploits;
import com.winthier.generic_events.GenericEvents;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import lombok.NonNull;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

final class Mining {
    final SkillsPlugin plugin;
    final EnumMap<Material, Reward> rewards = new EnumMap<>(Material.class);

    @Value
    static class Reward {
        final Material material;
        final int sp;
        final int exp;
        final Material item;
        final int drops;

        boolean dropSelf() {
            switch (material) {
            case IRON_ORE:
            case GOLD_ORE:
                return true;
            default: return false;
            }
        }
    }

    private void reward(@NonNull Material material, final int sp, final int exp,
                        @NonNull Material item, int drops) {
        rewards.put(material, new Reward(material, sp, exp, item, drops));
    }

    Mining(@NonNull final SkillsPlugin plugin) {
        this.plugin = plugin;
        // exp values are maxima according to the wiki
        reward(Material.DIAMOND_ORE, 10, 7, Material.DIAMOND, 1);
        reward(Material.EMERALD_ORE, 10, 7, Material.EMERALD, 1);
        reward(Material.IRON_ORE, 3, 3, Material.IRON_NUGGET, 9);
        reward(Material.GOLD_ORE, 5, 3, Material.GOLD_NUGGET, 9);
        reward(Material.COAL_ORE, 1, 2, Material.COAL, 1);
        reward(Material.LAPIS_ORE, 1, 5, Material.LAPIS_LAZULI, 6); // 4-8
        reward(Material.NETHER_QUARTZ_ORE, 1, 5, Material.QUARTZ, 1);
        reward(Material.REDSTONE_ORE, 1, 5, Material.REDSTONE, 5); // 4-5
    }

    static boolean stone(@NonNull Block block) {
        switch (block.getType()) {
        case STONE:
        case DIORITE:
        case ANDESITE:
        case GRANITE:
            return true;
        default:
            return false;
        }
    }

    static boolean dirt(@NonNull Block block) {
        switch (block.getType()) {
        case GRAVEL:
        case DIRT:
            return true;
        default:
            return false;
        }
    }

    static boolean isPickaxe(@NonNull ItemStack item) {
        switch (item.getType()) {
        case DIAMOND_PICKAXE:
        case IRON_PICKAXE:
        case STONE_PICKAXE:
        case WOODEN_PICKAXE:
        case GOLDEN_PICKAXE:
            return true;
        default:
            return false;
        }
    }

    /**
     * Called via scheduler.
     */
    private int stripMine(@NonNull Player player, @NonNull Block block) {
        // Check item
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return 0;
        if (!isPickaxe(item)) return 0;
        int efficiency = item.getEnchantmentLevel(Enchantment.DIG_SPEED);
        if (efficiency <= 0) return 0;
        // Figure out direction
        Block head = player.getEyeLocation().getBlock();
        // Require straight mining
        if (head.getX() != block.getX()
            && head.getZ() != block.getZ()) return 0;
        int dx = block.getX() - head.getX();
        int dz = block.getZ() - head.getZ();
        if (dx == 0 && dz == 0) return 0;
        if (Math.abs(dx) > Math.abs(dz)) {
            dx /= Math.abs(dx);
            dz = 0;
        } else {
            dx = 0;
            dz /= Math.abs(dz);
        }
        // Figure out item
        Damageable dmg = null;
        ItemMeta meta = item.getItemMeta();
        int unbreaking = item.getEnchantmentLevel(Enchantment.DURABILITY);
        if (!meta.isUnbreakable() && meta instanceof Damageable) {
            dmg = (Damageable) meta;
        }
        // Start breaking
        Block nbor = block.getRelative(0, 0, 0); // clone
        int result = 0;
        int total = efficiency / 2 + 1;
        for (int i = 0; i < total; i += 1) {
            nbor = nbor.getRelative(dx, 0, dz);
            if (!stone(nbor)) break;
            if (!GenericEvents.playerCanBuild(player, nbor)) break;
            // Damage the pickaxe and cancel if it is used up.
            if (dmg != null) {
                if (dmg.getDamage() >= item.getType().getMaxDurability()) break;
                if (unbreaking == 0 || plugin.random.nextInt(unbreaking) == 0) {
                    dmg.setDamage(dmg.getDamage() + 1);
                    item.setItemMeta(meta);
                }
            }
            Effects.mineBlockMagic(nbor);
            nbor.breakNaturally(item);
            result += 1;
        }
        return result;
    }

    /**
     * Called by scheduler.
     *
     * @bugs Does NOT deal damage to the pickaxe.
     */
    private int mineVein(@NonNull Player player,
                         @NonNull Block block,
                         @NonNull ItemStack item,
                         @NonNull Reward reward,
                         final int efficiency) {
        Material mat = reward.material;
        HashSet<Block> done = new HashSet<>();
        HashSet<Block> todo = new HashSet<>();
        ArrayList<Block> vein = new ArrayList<>();
        todo.add(block);
        done.add(block);
        int total = efficiency * 4;
        while (!todo.isEmpty() && vein.size() < total) {
            Block pivot = todo.iterator().next();
            todo.remove(pivot);
            for (int y = -1; y <= 1; y += 1) {
                for (int z = -1; z <= 1; z += 1) {
                    for (int x = -1; x <= 1; x += 1) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Block nbor = pivot.getRelative(x, y, z);
                        if (done.contains(nbor)) continue;
                        done.add(nbor);
                        if (nbor.getType() != mat) continue;
                        if (!GenericEvents.playerCanBuild(player, nbor)) continue;
                        todo.add(nbor);
                        vein.add(nbor);
                    }
                }
            }
        }
        for (Block v : vein) {
            if (!Exploits.isPlayerPlaced(v)) {
                giveReward(player, v, reward);
                if (reward.dropSelf() && reward.exp > 0) {
                    // If reward drops self, vanilla gives no exp, so we do it.
                    Util.exp(v.getLocation().add(0.5, 0.5, 0.5), reward.exp);
                }
            }
            Effects.mineBlockMagic(v);
            v.breakNaturally(item);
        }
        return vein.size();
    }

    private boolean oreAlert(@NonNull Player player, @NonNull Block block) {
        final int radius = 3;
        ArrayList<Block> bs = new ArrayList<>();
        for (int y = -radius; y <= radius; y += 1) {
            for (int z = -radius; z <= radius; z += 1) {
                for (int x = -radius; x <= radius; x += 1) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block nbor = block.getRelative(x, y, z);
                    if (nbor.getY() < 0) continue;
                    Material mat = nbor.getType();
                    if (mat == Material.DIAMOND_ORE || mat == Material.EMERALD_ORE) {
                        bs.add(nbor);
                    }
                }
            }
        }
        if (bs.isEmpty()) return false;
        Block ore = bs.get(plugin.random.nextInt(bs.size()));
        Effects.oreAlert(player, ore);
        return true;
    }

    /**
     * Called by scheduler.
     */
    private int xray(@NonNull Player player, @NonNull Block block) {
        if (!player.isValid()) return 0;
        if (!player.getWorld().equals(block.getWorld())) return 0;
        Session session = plugin.sessions.of(player);
        // Night Vision
        final int potionDuration = 45 * 20; // ticks
        PotionEffect nightVision = player.getPotionEffect(PotionEffectType.NIGHT_VISION);
        if (nightVision == null || nightVision.getDuration() < potionDuration) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,
                                                    potionDuration,
                                                    0, // amplifier
                                                    true, // ambient
                                                    false, // particles
                                                    true), // icon
                                   true);
        }
        // Actual XRay
        if (session.xrayActive) return 0;
        session.xrayActive = true;
        final int radius = 3;
        final int realRadius = 2;
        final ArrayList<Block> bs = new ArrayList<>();
        final ArrayList<Block> br = new ArrayList<>();
        Location loc = player.getLocation();
        int px = loc.getBlockX();
        int pz = loc.getBlockZ();
        for (int y = -radius; y <= radius; y += 1) {
            for (int z = -radius; z <= radius; z += 1) {
                for (int x = -radius; x <= radius; x += 1) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block nbor = block.getRelative(x, y, z);
                    if (nbor.getY() < 0) continue;
                    if (nbor.isEmpty() || nbor.isLiquid()) continue;
                    int d = Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z)));
                    if ((!stone(nbor) && !dirt(nbor)) || d > realRadius) {
                        br.add(nbor);
                    } else {
                        bs.add(nbor);
                    }
                }
            }
        }
        if (bs.isEmpty()) return 0;
        BlockData fakeBlockData = Material.BLACK_STAINED_GLASS.createBlockData();
        BlockData fakeDirtData = Material.WHITE_STAINED_GLASS.createBlockData();
        for (Block b : bs) {
            Location bl = b.getLocation();
            if (dirt(b)) {
                Spectators.apply(player, p ->
                                 p.sendBlockChange(bl, fakeDirtData));
            } else {
                Spectators.apply(player, p ->
                                 p.sendBlockChange(bl, fakeBlockData));
            }
        }
        for (Block b : br) {
            Location bl = b.getLocation();
            BlockData data = b.getBlockData();
            Spectators.apply(player, p ->
                             p.sendBlockChange(bl, data));
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.isValid()) return;
                plugin.sessions.of(player).xrayActive = false;
                if (!player.getWorld().equals(block.getWorld())) return;
                for (Block b : bs) {
                    if (!player.isValid()) return;
                    if (!player.getWorld().equals(block.getWorld())) return;
                    player.sendBlockChange(b.getLocation(), b.getBlockData());
                }
            }, 60L); // 3 seconds
        return bs.size();
    }

    void mine(@NonNull Player player, @NonNull Block block) {
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) return;
        if (!isPickaxe(item)) return;
        final int efficiency = item.getEnchantmentLevel(Enchantment.DIG_SPEED);
        final int fortune = item.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
        Session session = plugin.sessions.of(player);
        final boolean sneak = player.isSneaking();
        final boolean stone = stone(block);
        // Strip Mining
        final boolean miningLevel = block.getY() < 32;
        if (session.hasTalent(Talent.MINE_STRIP) && !sneak && stone && efficiency > 0
            && miningLevel) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (!player.isValid()) return;
                    if (!player.getWorld().equals(block.getWorld())) return;
                    stripMine(player, block);
                });
        }
        Reward reward = rewards.get(block.getType());
        // Vein Mining
        if (session.hasTalent(Talent.MINE_STRIP)
            && !sneak && reward != null && efficiency > 0 && miningLevel) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (!player.isValid()) return;
                    if (!player.getWorld().equals(block.getWorld())) return;
                    mineVein(player, block, item, reward, efficiency);
                });
        }
        // Ore Alert
        if (session.hasTalent(Talent.MINE_ORE_ALERT) && miningLevel && stone) {
            oreAlert(player, block);
        }
        // Xray
        if (session.hasTalent(Talent.MINE_XRAY) && !session.xrayActive && stone
            && fortune > 0 && !sneak && miningLevel) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                    xray(player, block);
                });
        }
        if (reward == null) return;
        giveReward(player, block, reward);
        if (reward.dropSelf() && reward.exp > 0 && !Exploits.isPlayerPlaced(block)) {
            // If reward drops self, vanilla gives no exp, so we do it.
            Util.exp(block.getLocation().add(0.5, 0.5, 0.5), reward.exp);
        }
    }

    boolean usePickaxe(@NonNull Player player, @NonNull Block block,
                       @NonNull BlockFace face, @NonNull ItemStack item) {
        Reward reward = rewards.get(block.getType());
        if (reward == null) return false;
        Session session = plugin.sessions.of(player);
        if (!session.hasTalent(Talent.MINE_SILK_STRIP)) return false;
        if (item == null || item.getType() == Material.AIR) return false;
        if (!GenericEvents.playerCanBuild(player, block)) return false;
        int silk = item.getEnchantmentLevel(Enchantment.SILK_TOUCH);
        if (silk == 0) return false;
        // Damage the pickaxe
        ItemMeta meta = item.getItemMeta();
        if (!meta.isUnbreakable() && meta instanceof Damageable) {
            Damageable dmg = (Damageable) meta;
            if (dmg.getDamage() >= item.getType().getMaxDurability()) return false;
            int unbreaking = item.getEnchantmentLevel(Enchantment.DURABILITY);
            if (unbreaking == 0 || plugin.random.nextInt(unbreaking) == 0) {
                dmg.setDamage(dmg.getDamage() + 1);
                item.setItemMeta(meta);
            }
        }
        // Drop an item (point of no return)
        ItemStack drop = new ItemStack(reward.item);
        double off = 0.7;
        Location dropLocation = block
            .getLocation().add(0.5 + (double) face.getModX() * off,
                               0.5 + (double) face.getModY() * off,
                               0.5 + (double) face.getModZ() * off);
        if (face.getModY() == -1) {
            dropLocation = dropLocation.add(0, -0.5, 0);
        } else if (face.getModY() != 1) {
            dropLocation = dropLocation.add(0, -0.25, 0);
        }
        double spd = 0.125;
        Vector vel = new Vector(face.getModX() * spd,
                                face.getModY() * spd,
                                face.getModZ() * spd);
        player.getWorld().dropItem(dropLocation, drop).setVelocity(vel);
        // (Maybe) change the Block
        double factor = 2.20; // Fortune 3
        if (session.hasTalent(Talent.MINE_SILK_MULTI)) factor = 2.60;
        final double amount; // Expected value of additionally dropped items.
        amount = (double) reward.drops * factor;
        final double chance; // Chance at NOT getting another drop.
        chance = 1.0 / amount;
        final double roll = plugin.random.nextDouble();
        Effects.useSilk(player, block, dropLocation);
        if (roll < chance) {
            giveReward(player, block, reward);
            if (reward.exp > 0) {
                Util.exp(dropLocation, reward.exp);
            }
            Effects.failSilk(player, block);
            if (reward.material == Material.NETHER_QUARTZ_ORE) {
                block.setType(Material.NETHERRACK);
            } else {
                block.setType(Material.STONE);
            }
        }
        return true;
    }

    /**
     * Give the SP reward for the broken block and roll talent points
     * where it applies.
     *
     * Do NOT give exp rewards as their spawning location is
     * situational.
     *
     * Do NOT drop any items because they only drop when silk
     * stripping.
     */
    private boolean giveReward(@NonNull Player player,
                       @NonNull Block block,
                       @NonNull Reward reward) {
        if (Exploits.isPlayerPlaced(block)) return false;
        plugin.points.give(player, SkillType.MINING, reward.sp);
        Material mat = block.getType();
        if (mat == Material.DIAMOND_ORE || mat == Material.EMERALD_ORE) {
            plugin.talents.rollPoint(player, 1);
        }
        return true;
    }
}
