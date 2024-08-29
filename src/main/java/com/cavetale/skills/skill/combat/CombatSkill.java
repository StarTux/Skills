package com.cavetale.skills.skill.combat;

import com.cavetale.core.connect.NetworkServer;
import com.cavetale.core.event.skills.SkillsMobKillRewardEvent;
import com.cavetale.skills.session.Session;
import com.cavetale.skills.skill.Skill;
import com.cavetale.skills.skill.SkillType;
import com.cavetale.skills.util.Players;
import com.cavetale.worldmarker.util.Tags;
import java.time.Duration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataContainer;
import static com.cavetale.skills.SkillsPlugin.skillsPlugin;
import static com.cavetale.skills.skill.combat.CombatReward.combatReward;

public final class CombatSkill extends Skill {
    protected final CombatListener combatListener = new CombatListener(this);;
    public final PyromaniacTalent pyromaniacTalent = new PyromaniacTalent();
    public final DenialTalent denialTalent = new DenialTalent();
    public final GodModeTalent godModeTalent = new GodModeTalent();
    public final ExecutionerTalent executionerTalent = new ExecutionerTalent();
    public final ToxicistTalent toxicistTalent = new ToxicistTalent();
    public final BerserkerTalent berserkerTalent = new BerserkerTalent();

    protected static final long CHUNK_KILL_DECAY_TIME = Duration.ofMinutes(5).toMillis();

    public CombatSkill() {
        super(SkillType.COMBAT);
    }

    @Override
    protected void enable() {
        MobStatusEffect.enable();
        Bukkit.getPluginManager().registerEvents(combatListener, skillsPlugin());
    }

    /**
     * Give skill points when a player kills a mob.
     */
    protected void onMeleeKill(Player player, Mob mob, EntityDeathEvent event) {
        godModeTalent.onMeleeKill(player, mob);
        CombatReward reward = combatReward(mob);
        if (reward == null) return;
        if (!Players.playMode(player)) return;
        final Session session = Session.of(player);
        if (!session.isEnabled()) return;
        if (mob instanceof Ageable && !((Ageable) mob).isAdult()) return;
        if (addKillAndCheckCooldown(mob.getLocation())) return;
        final var rewardEvent = new SkillsMobKillRewardEvent(player, mob,
                                                             (reward.sp * 5 / 2),
                                                             session.computeMoneyDrop(skillType, reward.money * 1.5),
                                                             3 * event.getDroppedExp() + session.getExpBonus(skillType));
        rewardEvent.callEvent();
        if (rewardEvent.isCancelled()) return;
        if (rewardEvent.getPostMultiplyFactor() != 1.0) {
            skillsPlugin().getLogger().info("[Combat] [" + mob.getType() + "] " + player.getName() + " " + rewardEvent.debugString());
        }
        session.addSkillPoints(skillType, rewardEvent.getFinalSkillPoints());
        dropMoney(player, mob.getLocation(), rewardEvent.getFinalMoney());
        event.setDroppedExp(rewardEvent.getFinalExp());
    }

    private static final NamespacedKey KEY_KILLS = NamespacedKey.fromString("skills:kills");
    private static final NamespacedKey KEY_LAST_KILL = NamespacedKey.fromString("skills:last_kill");

    /**
     * Add kill and check cooldown.
     * @return true if location (chunk) is on cooldown
     */
    public static boolean addKillAndCheckCooldown(Location location) {
        if (NetworkServer.current() == NetworkServer.MOB_ARENA) return false;
        final PersistentDataContainer pdc = location.getChunk().getPersistentDataContainer();
        final long now = System.currentTimeMillis();
        Integer oldKills = Tags.getInt(pdc, KEY_KILLS);
        if (oldKills == null) oldKills = 0;
        Long lastKill = Tags.getLong(pdc, KEY_LAST_KILL);
        if (lastKill == null) lastKill = 0L;
        long subtraction = (now - lastKill) / CHUNK_KILL_DECAY_TIME;
        final int kills = Math.max(0, oldKills - (int) subtraction) + 1;
        Tags.set(pdc, KEY_KILLS, kills);
        Tags.set(pdc, KEY_LAST_KILL, now);
        return kills > 5;
    }
}
