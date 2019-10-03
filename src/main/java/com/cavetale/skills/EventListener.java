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
import org.bukkit.event.entity.ExplosionPrimeEvent;
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
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.hasItem()) return;
        final ItemStack item = event.getItem();
        final Block block = event.getClickedBlock();
        if (item.getType() == Material.STICK) {
            plugin.growstick.use(event.getPlayer(), block);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    void onMarkChunkTick(MarkChunkTickEvent event) {
        event.getBlocksWithId(Growstick.WATERED_CROP)
            .forEach(plugin.growstick::tickWateredCrop);
        event.getBlocksWithId(Growstick.GROWN_CROP)
            .forEach(plugin.growstick::tickGrownCrop);
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
            case Growstick.WATERED_CROP:
                BlockMarker.resetId(block);
                break;
            case Growstick.GROWN_CROP:
                plugin.growstick.harvest(player, block);
                break;
            default: break;
            }
        }
        plugin.mining.mine(player, block);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!Util.playMode(player)) return;
        Block block = event.getBlock();
        Growstick.Crop crop = Growstick.Crop.of(event.getBlock());
        if (crop != null) {
            Session session = plugin.sessionOf(player);
            EquipmentSlot slot = event.getHand();
            if (session.hasTalent(Talent.FARM_PLANT_RADIUS) && !player.isSneaking()) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                        if (!player.isValid()) return;
                        plugin.growstick.plantRadius(player, block, crop, slot);
                    });
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    void onBlockGrow(BlockGrowEvent event) {
        if (BlockMarker.hasId(event.getBlock(), Growstick.WATERED_CROP)) {
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
        if (!entity.isDead()) return;
        Boss boss = plugin.bossOf(entity);
        if (boss != null) {
            plugin.bosses.remove(boss);
            Player hero = plugin.getServer().getPlayer(boss.hero);
            if (hero != null) {
                plugin.addSkillPoints(hero, SkillType.COMBAT, 10);
                Session session = plugin.sessionOf(hero);
                session.bossLevel = Math.max(session.bossLevel, boss.level);
                if (!plugin.rollTalentPoint(hero, 1)) {
                    hero.sendTitle(ChatColor.RED + boss.type.displayName,
                                   ChatColor.WHITE + "Level " + boss.level + " Defeated!");
                }
                return;
            }
        }
        Player killer = entity.getKiller();
        if (killer != null) {
            plugin.combat.kill(killer, entity);
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
            if (Util.dst(player.getLocation(), loc) > 32) continue;
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
        if (boss != null) {
            event.setCancelled(true);
            boss.explosionPrime();
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player damagee = (Player) event.getEntity();
            Boss boss = null;
            if (event.getDamager() instanceof Mob) {
                boss = plugin.bossOf(event.getDamager());
            } else if (event.getDamager() instanceof Projectile) {
                Projectile proj = (Projectile) event.getDamager();
                if (proj.getShooter() instanceof Mob) {
                    boss = plugin.bossOf((Mob) proj.getShooter());
                }
            }
            if (boss != null) {
                boss.dealDamage(damagee);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    void onEntityDamage(EntityDamageEvent event) {
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

    @EventHandler(ignoreCancelled = true)
    void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event) {
        if (EntityMarker.hasId(event.getEntity(), Boss.AOE)) {
            event.getAffectedEntities().removeIf(e -> plugin.bossOf(e) != null);
        }
    }
}
