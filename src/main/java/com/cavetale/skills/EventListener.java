package com.cavetale.skills;

import com.cavetale.worldmarker.BlockMarker;
import com.cavetale.worldmarker.EntityMarker;
import com.cavetale.worldmarker.MarkChunkTickEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
final class EventListener implements Listener {
    final SkillsPlugin plugin;
    boolean creatureSpawnLock;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!Util.playMode(player)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.hasItem()) return;
        // Item
        final ItemStack item = Util.getHand(player, event.getHand());
        final Block block = event.getClickedBlock();
        if (item.getType() == Material.STICK) {
            if (plugin.farming.useStick(player, block)) {
                event.setCancelled(true);
            }
            return;
        } else if (event.getHand() == EquipmentSlot.HAND && Mining.isPickaxe(item)) {
            if (plugin.mining.usePickaxe(player, block, event.getBlockFace(), item)) {
                event.setCancelled(true);
            }
            return;
        }
        Farming.Crop crop = Farming.Crop.ofSeed(item);
        if (crop != null) {
            // Cancelling with edibles may trigger infinite eating animation.
            plugin.farming.useSeed(player, block, crop, item);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    void onMarkChunkTick(MarkChunkTickEvent event) {
        event.getBlocksWithId(Farming.WATERED_CROP)
            .forEach(plugin.farming::tickWateredCrop);
        event.getBlocksWithId(Farming.GROWN_CROP)
            .forEach(plugin.farming::tickGrownCrop);
        plugin.combat.onTick(event.getChunk());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!Util.playMode(player)) return;
        Block block = event.getBlock();
        String bid = BlockMarker.getId(block);
        if (bid != null) {
            switch (bid) {
            case Farming.WATERED_CROP:
                BlockMarker.resetId(block);
                break;
            case Farming.GROWN_CROP:
                plugin.farming.harvest(player, block);
                break;
            default: break;
            }
        }
        plugin.mining.mine(player, block);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    void onBlockGrow(BlockGrowEvent event) {
        if (BlockMarker.hasId(event.getBlock(), Farming.WATERED_CROP)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    void onPlayerJoin(PlayerJoinEvent event) {
        plugin.loadSession(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerQuit(PlayerQuitEvent event) {
        plugin.removeSession(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Mob) {
            Mob mob = (Mob) entity;
            if (!mob.isDead()) return;
            Boss boss = plugin.bossOf(mob);
            if (boss != null) {
                plugin.bosses.remove(boss);
                Player hero = plugin.getServer().getPlayer(boss.hero);
                if (hero != null && Util.playMode(hero)) {
                    plugin.addSkillPoints(hero, SkillType.COMBAT, 10);
                    Session session = plugin.sessionOf(hero);
                    session.bossLevel = Math.max(session.bossLevel, boss.level);
                    if (!plugin.rollTalentPoint(hero, boss.level)) {
                        hero.sendTitle(ChatColor.RED + boss.type.displayName,
                                       ChatColor.WHITE + "Level " + boss.level + " Defeated!");
                    }
                    mob.getWorld().dropItem(mob.getEyeLocation(),
                                            new ItemStack(Material.DIAMOND, boss.level));
                    return;
                }
            }
            Player killer = entity.getKiller();
            if (killer != null) {
                plugin.combat.playerKillMob(killer, mob);
            }
        } else if (entity instanceof Player) {
            Player player = (Player) entity;
            if (!Util.playMode(player)) return;
            Session session = plugin.sessionOf(player);
            session.bossLevel = 0;
            session.bossProgress = 0;
        }
    }

    /**
     * Replace spawned mobs with bosses if conditions are met.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    void onCreatureSpawn(CreatureSpawnEvent event) {
        if (creatureSpawnLock) return;
        switch (event.getSpawnReason()) {
        case NATURAL:
        case NETHER_PORTAL:
        case REINFORCEMENTS:
        case TRAP:
        case VILLAGE_INVASION:
            break;
        default: return;
        }
        LivingEntity entity = event.getEntity();
        Location loc = entity.getLocation();
        World world = loc.getWorld();
        for (Player player : world.getPlayers()) {
            if (!Util.playMode(player)) continue;
            Session session = plugin.sessionOf(player);
            if (session.bossProgress < 10 + session.bossLevel * 10) continue;
            if (Util.dst(player.getLocation(), loc) > 128) continue;
            if (!player.hasLineOfSight(entity)) continue;
            creatureSpawnLock = true;
            Boss boss;
            try {
                boss = Boss.spawn(plugin, player, session.bossLevel + 1, entity);
            } catch (Exception e) {
                e.printStackTrace();
                boss = null;
            }
            creatureSpawnLock = false;
            if (boss == null) continue;
            session.bossProgress = 0;
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Bosses may not be set on fire.
     */
    @EventHandler(ignoreCancelled = true)
    void onEntityCombust(EntityCombustEvent event) {
        Entity entity = event.getEntity();
        Boss boss = plugin.bossOf(entity);
        if (boss != null) {
            event.setCancelled(true);
            return;
        }
        if (EntityMarker.hasId(entity, Boss.ADD)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        Boss boss = plugin.bossOf(entity);
        if (boss != null) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    void onExplosionPrime(ExplosionPrimeEvent event) {
        Entity entity = event.getEntity();
        Boss boss = plugin.bossOf(entity);
        if (boss != null && boss.type == Boss.Type.FART_GOBLIN) {
            event.setCancelled(true);
            boss.fart();
            return;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            // Mob attacks player
            final Player player = (Player) event.getEntity();
            if (!Util.playMode(player)) return;
            Mob mob = null;
            Projectile proj = null;
            switch (event.getCause()) {
            case ENTITY_ATTACK:
                if (event.getDamager() instanceof Mob) {
                    mob = (Mob) event.getDamager();
                }
                break;
            case PROJECTILE:
                if (event.getDamager() instanceof Projectile) {
                    proj = (Projectile) event.getDamager();
                    if (proj.getShooter() instanceof Mob) {
                        mob = (Mob) proj.getShooter();
                    }
                }
                break;
            default:
                break;
            }
            if (mob != null) {
                plugin.combat.mobDamagePlayer(player, mob, proj, event);
                Boss boss = plugin.bossOf(mob);
                if (boss != null) boss.damagePlayer(player, proj);
            }
        } else if (event.getEntity() instanceof Mob) {
            // Player attacks mob
            final Mob mob = (Mob) event.getEntity();
            Player player = null;
            Projectile proj = null;
            switch (event.getCause()) {
            case ENTITY_ATTACK:
                if (event.getDamager() instanceof Player) {
                    player = (Player) event.getDamager();
                }
                break;
            case PROJECTILE:
                if (event.getDamager() instanceof Projectile) {
                    proj = (Projectile) event.getDamager();
                    if (proj.getShooter() instanceof Player) {
                        player = (Player) proj.getShooter();
                    }
                }
                break;
            default:
                break;
            }
            if (player != null && Util.playMode(player)) {
                plugin.combat.playerDamageMob(player, mob, proj, event);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Session session = plugin.sessionOf(player);
            double health = player.getHealth();
            if (session.hasTalent(Talent.COMBAT_GOD_MODE)
                && session.immortal > 0
                && health <= event.getFinalDamage()) {
                event.setDamage(Math.max(0.0, health - 1.0));
                Effects.godMode(player);
                player.sendActionBar(ChatColor.GOLD + "God Mode Save!");
            }
        } else {
            Entity entity = event.getEntity();
            Boss boss = plugin.bossOf(entity);
            if (boss != null) {
                if (event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
                    event.setCancelled(true);
                    boss.stuck = true;
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
        if (EntityMarker.hasId(event.getEntity(), Boss.AOE)) {
            event.getAffectedEntities().removeIf(e -> plugin.bossOf(e) != null);
        }
    }

    @EventHandler(ignoreCancelled = true)
    void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile proj = event.getEntity();
        if (proj.getShooter() instanceof Mob) {
            Mob mob = (Mob) proj.getShooter();
            if (plugin.combat.statusEffectOf(mob).hasSilence()) {
                Effects.denyLaunch(mob);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    void onEntityPotionEffect(EntityPotionEffectEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Session session = plugin.sessionOf(player);
            if (session.hasTalent(Talent.COMBAT_SPIDERS)
                && session.poisonFreebie
                && event.getCause() == EntityPotionEffectEvent.Cause.ATTACK) {
                session.poisonFreebie = false;
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        ItemStack item = event.getItemInHand();
        if (plugin.farming.isHoe(item)) {
            if (block.getType() == Material.FARMLAND) {
                Effects.hoe(block, event.getBlockReplacedState().getBlockData());
            }
        }
    }
}

