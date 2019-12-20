package com.cavetale.skills;

import com.cavetale.worldmarker.BlockMarker;
import com.cavetale.worldmarker.MarkChunkTickEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
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
            Player killer = entity.getKiller();
            if (killer != null) {
                plugin.combat.playerKillMob(killer, mob);
            }
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

