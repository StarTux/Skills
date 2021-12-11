package com.cavetale.skills.skill.combat;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.Util;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Skill;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.worldmarker.util.Tags;
import java.time.Duration;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

public final class CombatSkill extends Skill {
    protected final CombatListener combatListener = new CombatListener(this);;
    protected final CombatRewards combatRewards = new CombatRewards();
    protected final NamespacedKey killsKey;
    protected final NamespacedKey lastKillKey;
    public final PyromaniacTalent pyromaniacTalent;
    public final SilenceTalent silenceTalent;
    public final VamonosTalent vamonosTalent;
    public final GodModeTalent godModeTalent;
    public final ArcherZoneTalent archerZoneTalent;

    protected static final long CHUNK_KILL_COOLDOWN = Duration.ofMinutes(5).toMillis();

    public CombatSkill(@NonNull final SkillsPlugin plugin) {
        super(plugin, SkillType.COMBAT);
        this.killsKey = new NamespacedKey(plugin, "kills");
        this.lastKillKey = new NamespacedKey(plugin, "last_kill");
        this.pyromaniacTalent = new PyromaniacTalent(plugin, this);
        this.silenceTalent = new SilenceTalent(plugin, this);
        this.vamonosTalent = new VamonosTalent(plugin, this);
        this.godModeTalent = new GodModeTalent(plugin, this);
        this.archerZoneTalent = new ArcherZoneTalent(plugin, this);
    }

    @Override
    protected void enable() {
        MobStatusEffect.enable(plugin);
        combatRewards.enable();
        Bukkit.getPluginManager().registerEvents(combatListener, plugin);
    }

    protected void onMobDamagePlayer(Player player, Mob mob, Projectile projectile, EntityDamageByEntityEvent event) {
        pyromaniacTalent.onMobDamagePlayer(player, mob, projectile, event);
        vamonosTalent.onMobDamagePlayer(player, mob, projectile, event);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, Projectile proj, EntityDamageByEntityEvent event) {
        final ItemStack item = player.getInventory().getItemInMainHand();
        pyromaniacTalent.onPlayerDamageMob(player, mob, item, proj, event);
        silenceTalent.onPlayerDamageMob(player, mob, item, proj, event);
        vamonosTalent.onPlayerDamageMob(player, mob, item, proj, event);
        archerZoneTalent.onPlayerDamageMob(player, mob, item, proj, event);
    }

    protected void onPlayerKillMob(Player player, Mob mob, EntityDeathEvent event) {
        CombatReward reward = combatRewards.rewards.get(mob.getType());
        if (reward == null) return;
        if (!Util.playMode(player)) return;
        Session session = plugin.sessions.of(player);
        if (!session.isEnabled()) return;
        if (mob instanceof Ageable && !((Ageable) mob).isAdult()) return;
        final PersistentDataContainer pdc = mob.getLocation().getChunk().getPersistentDataContainer();
        final long now = System.currentTimeMillis();
        final Integer oldKills = Tags.getInt(pdc, killsKey);
        final Long oldLastKill = Tags.getLong(pdc, lastKillKey);
        int kills = oldKills != null ? oldKills : 0;
        long lastKill = oldLastKill != null ? oldLastKill : 0L;
        kills = now - lastKill < CHUNK_KILL_COOLDOWN ? kills + 1 : 0;
        Tags.set(pdc, killsKey, kills);
        Tags.set(pdc, lastKillKey, now);
        if (kills > 50) return;
        session.addSkillPoints(SkillType.COMBAT, reward.sp);
        event.setDroppedExp(event.getDroppedExp() + session.getExpBonus(SkillType.COMBAT));
    }

    protected void onMeleeKill(Player player, Mob mob) {
        godModeTalent.onMeleeKill(player, mob);
    }

    protected void onArcherKill(Player player, Mob mob, Projectile projectile, EntityDamageByEntityEvent event) {
        archerZoneTalent.onArcherKill(player, mob, projectile, event);
    }
}
