package com.cavetale.skills;

import com.cavetale.worldmarker.EntityMarker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Flying;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Spider;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

final class Boss {
    final SkillsPlugin plugin;
    final Type type;
    final int level;
    Mob entity;
    int ticksLived = 0;
    UUID hero;
    static final String BOSS = "skills:boss";
    static final String AOE = "skills:aoe";
    static final String ADD = "skills:add";
    EnumSet<Ability> abilities = EnumSet.noneOf(Ability.class);
    boolean flying;
    // state
    int warpCooldown = 200;
    int warpTicks;
    int makeFireCooldown = 100;
    int makeFireTicks;
    int shootArrowTicks;
    int shootArrowCooldown = 40;
    int throwPotionTicks;
    int throwPotionCooldown = 100;
    int throwProjectileTicks;
    int throwProjectileCooldown = 200;
    int throwEnderPearlTicks;
    int throwEnderPearlCooldown = 200;
    int lightningTicks = 0;
    int lightningCooldown = 200;
    Class<? extends Projectile> projectileClass;
    PotionEffect throwPotion;
    boolean stuck = false;
    int addTicks = 0;
    int addCooldown = 20;

    enum Type {
        ZAMBIE(EntityType.ZOMBIE),
        SKELLINGTON(EntityType.SKELETON),
        FART_GOBLIN(EntityType.CREEPER),
        TERRIBLE_TIMMY(EntityType.ZOMBIE),
        QUEEN_SPIDER(EntityType.SPIDER),
        MINERA(EntityType.PHANTOM);

        final EntityType mobType;
        final String displayName;

        Type(@NonNull final EntityType mobType) {
            this.mobType = mobType;
            this.displayName = Util.niceEnumName(this);
        }
    }

    enum Ability {
        WARP,
        MAKE_FIRE,
        SHOOT_ARROW,
        THROW_POTION,
        THROW_PROJECTILE,
        THROW_ENDER_PEARL,
        LIGHTNING;
    }

    Boss(@NonNull final SkillsPlugin plugin,
         @NonNull final Type type, final int level) {
        this.plugin = plugin;
        this.type = type;
        this.level = level;
    }

    private Mob spawnEntity(@NonNull Location loc) {
        World w = loc.getWorld();
        switch (type) {
        case ZAMBIE:
            return w.spawn(loc, Zombie.class, e -> {
                    EntityEquipment eq = e.getEquipment();
                    eq.setHelmet(new ItemStack(Material.GOLDEN_HELMET));
                    eq.setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
                    eq.setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
                    eq.setBoots(new ItemStack(Material.GOLDEN_BOOTS));
                    eq.setItemInMainHand(item(Material.GOLDEN_SWORD, Enchantment.DAMAGE_ALL,
                                              Enchantment.FIRE_ASPECT));
                    eq.setItemInOffHand(new ItemStack(Material.SHIELD));
                    noDrop(eq);
                    potion(e, PotionEffectType.FIRE_RESISTANCE, 1);
                });
        case SKELLINGTON:
            return w.spawn(loc, Skeleton.class, e -> {
                    EntityEquipment eq = e.getEquipment();
                    eq.setHelmet(new ItemStack(Material.IRON_HELMET));
                    eq.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                    eq.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                    eq.setBoots(new ItemStack(Material.IRON_BOOTS));
                    eq.setItemInMainHand(item(Material.BOW, Enchantment.ARROW_DAMAGE,
                                              Enchantment.ARROW_FIRE,
                                              Enchantment.ARROW_KNOCKBACK));
                    eq.setItemInOffHand(new ItemStack(Material.SHIELD));
                    noDrop(eq);
                    potion(e, PotionEffectType.FIRE_RESISTANCE, 1);
                });
        case FART_GOBLIN:
            return w.spawn(loc, Creeper.class, e -> {
                    e.setPowered(true);
                    potion(e, PotionEffectType.SPEED, 2);
                });
        case TERRIBLE_TIMMY:
            return w.spawn(loc, Zombie.class, e -> {
                    e.setBaby(true);
                    EntityEquipment eq = e.getEquipment();
                    eq.setItemInMainHand(new ItemStack(Material.BONE));
                    eq.setHelmet(new ItemStack(Material.TURTLE_HELMET));
                    noDrop(eq);
                    potion(e, PotionEffectType.FIRE_RESISTANCE, 1);
                    potion(e, PotionEffectType.SPEED, 1);
                });
        case QUEEN_SPIDER:
            return w.spawn(loc, Spider.class, e -> {
                    potion(e, PotionEffectType.SPEED, 2);
                    potion(e, PotionEffectType.REGENERATION, 2);
                });
        case MINERA:
            return w.spawn(loc, Phantom.class, e -> {
                    e.setSize(20);
                });
        default: return null;
        }
    }

    static ItemStack item(Material mat, Enchantment... enchs) {
        ItemStack item = new ItemStack(mat);
        for (Enchantment ench : enchs) {
            item.addEnchantment(ench, ench.getMaxLevel());
        }
        ItemMeta meta = item.getItemMeta();
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }

    static void noDrop(EntityEquipment eq) {
        eq.setHelmetDropChance(0.0f);
        eq.setChestplateDropChance(0.0f);
        eq.setLeggingsDropChance(0.0f);
        eq.setBootsDropChance(0.0f);
        eq.setItemInMainHandDropChance(0.0f);
        eq.setItemInOffHandDropChance(0.0f);
    }

    static void potion(Mob e, PotionEffectType type, int level) {
        e.addPotionEffect(new PotionEffect(type, 72000, level - 1, true, false));
    }

    boolean spawn(@NonNull Location loc) {
        entity = spawnEntity(loc);
        flying = entity instanceof Flying;
        if (entity == null) return false;
        plugin.meta.set(entity, BOSS, this);
        plugin.bosses.add(this);
        // Name
        entity.setCustomName(ChatColor.RED + type.displayName);
        EntityMarker.setId(entity, BOSS);
        // Health
        double health = (double) level * 50.0 + 100.0;
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        entity.setHealth(health);
        // Knockback Resistance
        entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0);
        // Strength
        double strength = 10;
        strength += (double) (level - 1) * 5.0;
        entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(strength);
        // Transient
        entity.setPersistent(false);
        List<Ability> abs = new ArrayList<>(Arrays.asList(Ability.values()));
        if (type == Type.MINERA) {
            abs.remove(Ability.THROW_ENDER_PEARL);
            abs.remove(Ability.MAKE_FIRE);
        }
        Collections.shuffle(abs, plugin.random);
        for (int i = 0; i < level; i += 1) {
            if (i >= abs.size()) break;
            abilities.add(abs.get(i));
        }
        return true;
    }

    void remove() {
        if (entity == null || !entity.isValid()) return;
        entity.remove();
    }

    boolean isValid() {
        return entity != null && entity.isValid();
    }

    Player nearbyTarget() {
        int min = 80;
        Player result = null;
        Location loc = entity.getLocation();
        for (Player p : entity.getWorld().getPlayers()) {
            if (!Util.playMode(p)) continue;
            if (!p.isValid() || p.isDead()) continue;
            int d = Util.dst(loc, p.getLocation());
            if (d < min) {
                result = p;
                min = d;
            }
        }
        return result;
    }

    void onTick() {
        ticksLived += 1;
        Effects.bossSwirl(entity, ticksLived);
        Player target = null;
        if (hero != null) plugin.getServer().getPlayer(hero);
        if (target != null) {
            if (!Util.playMode(target)) {
                target = null;
            } else if (!target.getWorld().equals(entity.getWorld())) {
                target = null;
            }
            if (Util.dst(target.getLocation(), entity.getLocation()) > 48) {
                if (warp(target)) return;
            }
        }
        if (target == null) {
            LivingEntity t = entity.getTarget();
            if (t instanceof Player && Util.playMode((Player) t)) {
                target = (Player) t;
            }
        }
        if (target == null) target = nearbyTarget();
        if (target == null) {
            remove();
            return;
        }
        entity.setTarget(target);
        if (stuck && warp(target)) {
            stuck = false;
            return;
        }
        if (abilities.contains(Ability.WARP) && warpTicks++ >= warpCooldown) {
            warp(target);
        }
        if (abilities.contains(Ability.MAKE_FIRE) && makeFireTicks++ >= makeFireCooldown) {
            Block block = entity.getLocation().getBlock();
            if (block.isEmpty()) {
                block.setType(Material.FIRE);
                makeFireTicks = plugin.random.nextInt(makeFireCooldown / 2);
            }
        }
        if (abilities.contains(Ability.SHOOT_ARROW)
            && shootArrowTicks++ >= shootArrowCooldown) {
            shootArrow(target);
        }
        if (abilities.contains(Ability.THROW_POTION)
            && throwPotionTicks++ >= throwPotionCooldown) {
            throwPotion(target);
        }
        if (abilities.contains(Ability.THROW_PROJECTILE)
            && throwProjectileTicks++ >= throwProjectileCooldown) {
            throwProjectile(target);
        }
        if (abilities.contains(Ability.THROW_ENDER_PEARL)
            && throwEnderPearlTicks++ >= throwEnderPearlCooldown) {
            throwEnderPearl(target);
        }
        if (type == Type.QUEEN_SPIDER && addTicks++ >= addCooldown) {
            if (2 > entity.getNearbyEntities(8.0, 8.0, 8.0).stream()
                .filter(e -> e.getType() == EntityType.CAVE_SPIDER)
                .count()) {
                CaveSpider add = entity.getWorld().spawn(entity.getLocation(), CaveSpider.class);
                add.setTarget(target);
                EntityMarker.setId(add, ADD);
                addTicks = 0;
            }
        }
        if (type == Type.MINERA && addTicks++ >= addCooldown) {
            if (1 > entity.getNearbyEntities(16.0, 16.0, 16.0).stream()
                .filter(e -> e.getType() == EntityType.PHANTOM)
                .count()) {
                Phantom add = entity.getWorld().spawn(entity.getLocation(), Phantom.class);
                add.setTarget(target);
                EntityMarker.setId(add, ADD);
                addTicks = 0;
            }
        }
        if (abilities.contains(Ability.LIGHTNING) && lightningTicks++ >= lightningCooldown) {
            lightningTicks = 0;
            for (Player player : entity.getWorld().getPlayers()) {
                if (Util.dst(entity.getLocation(), player.getLocation()) > 20) continue;
                if (!Util.playMode(player)) continue;
                player.getWorld().strikeLightning(player.getLocation());
            }
        }
    }

    double rnd(double f) {
        double r = plugin.random.nextDouble();
        return plugin.random.nextBoolean() ? r * f : -r * f;
    }

    boolean warp(Player target) {
        final double radius = 20.0;
        Location to = target.getLocation().add(rnd(radius), rnd(radius), rnd(radius));
        if (to.getY() < 5.0 && to.getY() > 250.0) return false;
        if (!flying && !to.getBlock().getRelative(0, -1, 0).getType().isSolid()) return false;
        BoundingBox bb = entity.getBoundingBox().shift(to.clone().subtract(entity.getLocation()));
        World w = to.getWorld();
        final int ax = (int) Math.floor(bb.getMinX());
        final int ay = (int) Math.floor(bb.getMinY());
        final int az = (int) Math.floor(bb.getMinZ());
        final int bx = (int) Math.floor(bb.getMaxX());
        final int by = (int) Math.floor(bb.getMaxY());
        final int bz = (int) Math.floor(bb.getMaxZ());
        for (int y = ay; y <= by; y += 1) {
            for (int z = az; z <= bz; z += 1) {
                for (int x = ax; x <= bx; x += 1) {
                    if (!w.getBlockAt(x, y, z).isEmpty()) return false;
                }
            }
        }
        warpTicks = plugin.random.nextInt(warpCooldown / 2);
        Effects.warp(entity);
        entity.teleport(to);
        Effects.warp(entity);
        return true;
    }

    void shootArrow(Player target) {
        if (plugin.combat.statusEffectOf(entity).hasSilence()) return;
        if (!entity.hasLineOfSight(target)) return;
        Vector v = target.getLocation().toVector().subtract(entity.getLocation().toVector())
            .normalize().multiply(3.0);
        Projectile proj = entity.launchProjectile(Arrow.class, v);
        proj.setShooter(entity);
        shootArrowTicks = plugin.random.nextInt(shootArrowCooldown / 2);
    }

    void throwPotion(Player target) {
        if (plugin.combat.statusEffectOf(entity).hasSilence()) return;
        if (!entity.hasLineOfSight(target)) return;
        if (throwPotion == null) {
            List<PotionEffect> opts = Arrays
                .asList(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0),
                        new PotionEffect(PotionEffectType.SLOW, 200, 0),
                        new PotionEffect(PotionEffectType.CONFUSION, 200, 0),
                        new PotionEffect(PotionEffectType.POISON, 400, 0),
                        new PotionEffect(PotionEffectType.POISON, 400, 1),
                        new PotionEffect(PotionEffectType.WITHER, 200, 0),
                        new PotionEffect(PotionEffectType.HARM, 1, 2),
                        new PotionEffect(PotionEffectType.HARM, 1, 1),
                        new PotionEffect(PotionEffectType.SLOW_DIGGING, 400, 0),
                        new PotionEffect(PotionEffectType.LEVITATION, 200, 0));
            throwPotion = opts.get(plugin.random.nextInt(opts.size()));
        }
        Vector v = target.getLocation().toVector().subtract(entity.getLocation().toVector())
            .normalize().multiply(1.8);
        Projectile proj = entity.launchProjectile(ThrownPotion.class, v);
        proj.setShooter(entity);
        ThrownPotion potion = (ThrownPotion) proj;
        ItemStack item = potion.getItem();
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.addCustomEffect(throwPotion, true);
        item.setItemMeta(meta);
        potion.setItem(item);
        throwPotionTicks = plugin.random.nextInt(throwPotionCooldown / 2);
    }

    void throwProjectile(Player target) {
        if (plugin.combat.statusEffectOf(entity).hasSilence()) return;
        if (!entity.hasLineOfSight(target)) return;
        if (projectileClass == null) {
            List<Class<? extends Projectile>> opts = Arrays
                .asList(DragonFireball.class,
                        LargeFireball.class,
                        SmallFireball.class,
                        WitherSkull.class);
            projectileClass = opts.get(plugin.random.nextInt(opts.size()));
        }
        Vector v = target.getLocation().toVector().subtract(entity.getLocation().toVector())
            .normalize().multiply(1.5);
        Projectile proj = entity.launchProjectile(projectileClass, v);
        proj.setShooter(entity);
        throwProjectileTicks = plugin.random.nextInt(throwProjectileCooldown / 2);
    }

    void throwEnderPearl(Player target) {
        if (plugin.combat.statusEffectOf(entity).hasSilence()) return;
        if (!entity.hasLineOfSight(target)) return;
        Vector v = target.getLocation().toVector().subtract(entity.getLocation().toVector())
            .normalize().multiply(1.5);
        Projectile proj = entity.launchProjectile(EnderPearl.class, v);
        proj.setShooter(entity);
        throwEnderPearlTicks = plugin.random.nextInt(throwEnderPearlCooldown / 2);
    }

    static Boss spawn(@NonNull SkillsPlugin plugin,
                      @NonNull Player player, final int level,
                      @NonNull LivingEntity entity) {
        int i = 1;
        Type type = null;
        for (Type it : Type.values()) {
            if (it.mobType == entity.getType() && plugin.random.nextInt(i++) == 0) {
                type = it;
            }
        }
        if (type == null) return null;
        Boss boss = new Boss(plugin, type, level);
        if (!boss.spawn(entity.getLocation())) return null;
        boss.hero = player.getUniqueId();
        boss.entity.setTarget(player);
        player.sendActionBar(ChatColor.RED + type.displayName
                             + ChatColor.WHITE + " Level " + level);
        return boss;
    }

    void fart() {
        Location loc = entity.getLocation();
        AreaEffectCloud aoe = entity.getWorld()
            .spawn(loc, AreaEffectCloud.class, e -> {
                    e.setBasePotionData(new PotionData(PotionType.POISON));
                    e.setColor(Color.GREEN);
                    e.setDuration(200);
                    e.setRadius(4.0f);
                    e.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, 200, 0), true);
                    e.addCustomEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 1), true);
                });
        AreaEffectCloud aoe2 = entity.getWorld()
            .spawn(loc.add(0.0, 2.0, 0.0), AreaEffectCloud.class, e -> {
                    e.setBasePotionData(new PotionData(PotionType.POISON));
                    e.setColor(Color.GREEN);
                    e.setDuration(200);
                    e.setRadius(2.0f);
                    e.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, 200, 0), true);
                    e.addCustomEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 1), true);
                });
        EntityMarker.setId(aoe, AOE);
        EntityMarker.setId(aoe2, AOE);
    }

    void damagePlayer(@NonNull Player player, Projectile proj) {
        if (type == Type.QUEEN_SPIDER) {
            if (plugin.combat.statusEffectOf(entity).hasNoPoison()) return;
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 600, 1));
        }
    }
}
