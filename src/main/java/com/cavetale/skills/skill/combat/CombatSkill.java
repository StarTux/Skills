package com.cavetale.skills.skill.combat;

import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Skill;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.util.Players;
import com.cavetale.worldmarker.util.Tags;
import java.time.Duration;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import static com.cavetale.skills.SkillsPlugin.moneyBonusPercentage;
import static com.cavetale.skills.SkillsPlugin.sessionOf;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;
import static com.cavetale.skills.skill.combat.CombatReward.combatReward;

public final class CombatSkill extends Skill {
    protected final CombatListener combatListener = new CombatListener(this);;
    protected final NamespacedKey killsKey;
    protected final NamespacedKey lastKillKey;
    public final SearingTalent searingTalent = new SearingTalent();
    public final PyromaniacTalent pyromaniacTalent = new PyromaniacTalent();
    public final DenialTalent denialTalent = new DenialTalent();
    public final GodModeTalent godModeTalent = new GodModeTalent();
    public final IronAgeTalent ironAgeTalent = new IronAgeTalent();
    public final ExecutionerTalent executionerTalent = new ExecutionerTalent();
    public final ImpalerTalent impalerTalent = new ImpalerTalent();
    public final ToxicistTalent toxicistTalent = new ToxicistTalent();
    public final ToxicFurorTalent toxicFurorTalent = new ToxicFurorTalent();

    protected static final long CHUNK_KILL_DECAY_TIME = Duration.ofMinutes(5).toMillis();

    public CombatSkill() {
        super(SkillType.COMBAT);
        this.killsKey = new NamespacedKey(skillsPlugin(), "kills");
        this.lastKillKey = new NamespacedKey(skillsPlugin(), "last_kill");
    }

    @Override
    protected void enable() {
        MobStatusEffect.enable();
        Bukkit.getPluginManager().registerEvents(combatListener, skillsPlugin());
    }

    protected void onMobDamagePlayer(Player player, Mob mob, EntityDamageByEntityEvent event) {
        searingTalent.onMobDamagePlayer(player, mob, event);
        denialTalent.onMobDamagePlayer(player, mob, event);
    }

    protected void onPlayerDamageMob(Player player, Mob mob, EntityDamageByEntityEvent event) {
        final ItemStack item = player.getInventory().getItemInMainHand();
        pyromaniacTalent.onPlayerDamageMob(player, mob, item, event);
        denialTalent.onPlayerDamageMob(player, mob, item, event);
        denialTalent.onPlayerDamageMob(player, mob, item, event);
        ironAgeTalent.onPlayerDamageMob(player, mob, item, event);
        executionerTalent.onPlayerDamageMob(player, mob, item, event);
        impalerTalent.onPlayerDamageMob(player, mob, item, event);
        toxicistTalent.onPlayerDamageMob(player, mob, item, event);
        toxicFurorTalent.onPlayerDamageMob(player, mob, item, event);
    }

    /**
     * Give skill points when a player kills a mob.
     */
    protected void onMeleeKill(Player player, Mob mob, EntityDeathEvent event) {
        godModeTalent.onMeleeKill(player, mob);
        if (mob.fromMobSpawner()) return;
        CombatReward reward = combatReward(mob);
        if (reward == null) return;
        if (!Players.playMode(player)) return;
        Session session = sessionOf(player);
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
        session.addSkillPoints(SkillType.COMBAT, reward.sp * 2);
        if (reward.money > 0) {
            int bonus = session.getMoneyBonus(SkillType.COMBAT);
            double factor = 1.0 + 0.01 * moneyBonusPercentage(bonus);
            dropMoney(player, mob.getLocation(), reward.money * factor);
        }
        event.setDroppedExp(2 * event.getDroppedExp() + session.getExpBonus(SkillType.COMBAT));
    }
}
