package com.cavetale.skills.skill.combat;

import com.cavetale.skills.SkillsPlugin;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Skill;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.util.Players;
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
    public final SearingTalent searingTalent = new SearingTalent();
    public final PyromaniacTalent pyromaniacTalent = new PyromaniacTalent();
    public final DenialTalent denialTalent = new DenialTalent();
    public final GodModeTalent godModeTalent = new GodModeTalent();
    public final ArcherZoneTalent archerZoneTalent = new ArcherZoneTalent();
    public final IronAgeTalent ironAgeTalent = new IronAgeTalent();
    public final ExecutionerTalent executionerTalent = new ExecutionerTalent();
    public final ImpalerTalent impalerTalent = new ImpalerTalent();
    public final ToxicistTalent toxicistTalent = new ToxicistTalent();
    public final ToxicFurorTalent toxicFurorTalent = new ToxicFurorTalent();

    protected static final long CHUNK_KILL_DECAY_TIME = Duration.ofMinutes(5).toMillis();

    public CombatSkill(@NonNull final SkillsPlugin plugin) {
        super(plugin, SkillType.COMBAT);
        this.killsKey = new NamespacedKey(plugin, "kills");
        this.lastKillKey = new NamespacedKey(plugin, "last_kill");
    }

    @Override
    protected void enable() {
        MobStatusEffect.enable(plugin);
        combatRewards.enable();
        Bukkit.getPluginManager().registerEvents(combatListener, plugin);
    }

    protected void onMobDamagePlayer(Player player, Mob mob, Projectile projectile, EntityDamageByEntityEvent event) {
        searingTalent.onMobDamagePlayer(player, mob, projectile, event);
        denialTalent.onMobDamagePlayer(player, mob, projectile, event);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, Projectile proj, EntityDamageByEntityEvent event) {
        final ItemStack item = player.getInventory().getItemInMainHand();
        pyromaniacTalent.onPlayerDamageMob(player, mob, item, proj, event);
        denialTalent.onPlayerDamageMob(player, mob, item, proj, event);
        denialTalent.onPlayerDamageMob(player, mob, item, proj, event);
        archerZoneTalent.onPlayerDamageMob(player, mob, item, proj, event);
        ironAgeTalent.onPlayerDamageMob(player, mob, item, proj, event);
        executionerTalent.onPlayerDamageMob(player, mob, item, proj, event);
        impalerTalent.onPlayerDamageMob(player, mob, item, proj, event);
        toxicistTalent.onPlayerDamageMob(player, mob, item, proj, event);
        toxicFurorTalent.onPlayerDamageMob(player, mob, item, proj, event);
    }

    /**
     * Give skill points when a player kills a mob.
     */
    protected void onPlayerKillMob(Player player, Mob mob, EntityDeathEvent event) {
        if (mob.fromMobSpawner()) return;
        CombatReward reward = combatRewards.rewards.get(mob.getType());
        if (reward == null) return;
        if (!Players.playMode(player)) return;
        Session session = plugin.sessions.of(player);
        if (!session.isEnabled()) return;
        if (mob instanceof Ageable && !((Ageable) mob).isAdult()) return;
        final PersistentDataContainer pdc = mob.getLocation().getChunk().getPersistentDataContainer();
        final long now = System.currentTimeMillis();
        Integer oldKills = Tags.getInt(pdc, killsKey);
        if (oldKills == null) oldKills = 0;
        Long lastKill = Tags.getLong(pdc, lastKillKey);
        if (lastKill == null) lastKill = 0L;
        long subtraction = (now - lastKill) / CHUNK_KILL_DECAY_TIME;
        final int kills = Math.max(0, oldKills - (int) subtraction) + 1;
        Tags.set(pdc, killsKey, kills);
        Tags.set(pdc, lastKillKey, now);
        if (kills > 10) return;
        session.addSkillPoints(SkillType.COMBAT, reward.sp);
        if (reward.money > 0) {
            int bonus = session.getMoneyBonus(SkillType.COMBAT);
            double factor = 1.0 + 0.01 * SkillsPlugin.moneyBonusPercentage(bonus);
            dropMoney(player, mob.getLocation(), reward.money * factor);
        }
        giveExpBonus(player, session);
        event.setDroppedExp(event.getDroppedExp() + session.getExpBonus(SkillType.COMBAT));
    }

    protected void onMeleeKill(Player player, Mob mob) {
        godModeTalent.onMeleeKill(player, mob);
    }

    protected void onArcherKill(Player player, Mob mob, Projectile projectile, EntityDamageByEntityEvent event) {
        archerZoneTalent.onArcherKill(player, mob, projectile, event);
    }
}
