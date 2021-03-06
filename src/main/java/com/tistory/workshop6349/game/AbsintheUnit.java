package com.tistory.workshop6349.game;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.*;
import com.github.ocraft.s2client.protocol.unit.*;
import com.tistory.workshop6349.Constants;
import com.tistory.workshop6349.Vector2d;
import com.tistory.workshop6349.economy.Base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbsintheUnit {

    private final UnitInPool contained;
    private static final Map<Tag, AbsintheUnit> cache = new HashMap<>();

    private AbsintheUnit(UnitInPool unit) {
        contained = unit;
    }

    public static AbsintheUnit getInstance(UnitInPool unit) {
        if (!cache.containsKey(unit.getTag())) {
            cache.put(unit.getTag(), new AbsintheUnit(unit));
        }
        return cache.get(unit.getTag());
    }

    public Vector2d location() {
        return Vector2d.of(get_unit().getPosition().toPoint2d());
    }

    public double distance(AbsintheUnit other) {
        return location().distance(other.location());
    }

    public double distance(Base other) {
        return location().distance(other.location);
    }

    public double distance(Vector2d other) {
        return location().distance(other);
    }

    public Tag tag() {
        return contained.getTag();
    }

    private long type_frame = -1;
    private UnitType type = Units.INVALID;
    public UnitType type() {
        if (type_frame != Game.get_frame()) {
            type_frame = Game.get_frame();
            type = get_unit().getType();
        }
        return type;
    }

    public boolean alive() {
        return contained.isAlive();
    }

    public Alliance alliance() {
        return get_unit().getAlliance();
    }

    public boolean friendly() {
        return get_unit().getAlliance() == Alliance.SELF;
    }

    public boolean done() {
        return get_unit().getBuildProgress() > Constants.DONE;
    }

    public double progress() {
        return get_unit().getBuildProgress();
    }

    private long unit_frame = -1;
    private Unit unit = null;
    private Unit get_unit() {
        if (unit_frame != Game.get_frame()) {
            unit_frame = Game.get_frame();
            unit = contained.unit();
        }
        return unit;
    }

    public List<UnitOrder> orders() {
        return get_unit().getOrders();
    }

    public long last_seen() {
        return contained.getLastSeenGameLoop();
    }

    public boolean flying() {
        return get_unit().getFlying().orElse(false);
    }

    public boolean cloaked() {
        return get_unit().getCloakState().orElse(CloakState.NOT_CLOAKED) != CloakState.NOT_CLOAKED;
    }

    public boolean idle() {
        return orders().size() == 0;
    }

    public double health() {
        return get_unit().getHealth().orElse((float) 0.0);
    }

    public double health_max() {
        return get_unit().getHealthMax().orElse((float) 0.0);
    }

    public void attack(Vector2d point) {
        Game.unit_command(get_unit(), Abilities.ATTACK, point);
    }

    public void attack(AbsintheUnit unit) {
        Game.unit_command(get_unit(), Abilities.ATTACK, unit.unit());
    }

    public void move(Vector2d point) {
        Game.unit_command(get_unit(), Abilities.MOVE, point);
        //Game.unit_command(get_unit(), Abilities.MOVE, GameInfoCache.get_enemy_spawn().toVector2d(), true);
    }

    public void use_ability(Ability ability) {
        Game.unit_command(get_unit(), ability);
    }

    public void use_ability_queued(Ability ability) {
        Game.unit_command(get_unit(), ability, true);
    }

    public void use_ability(Ability ability, Vector2d point) {
        Game.unit_command(get_unit(), ability, point);
    }

    public void use_ability(Ability ability, AbsintheUnit unit) {
        Game.unit_command(get_unit(), ability, unit.unit());
    }

    public boolean is_worker() {
        return Game.is_worker(type());
    }

    public boolean is_structure() {
        return Game.is_structure(type());
    }

    public boolean is_combat() {
        return Game.is_combat(type());
    }

    public boolean is_command() {
        return Game.is_town_hall(type());
    }

    public boolean is_hallucination() {
        return get_unit().getHallucination().orElse(false);
    }

    public boolean is_not_snapshot() {
        return get_unit().getDisplayType() != DisplayType.SNAPSHOT;
    }

    public void stop() {
        use_ability(Abilities.STOP);
    }

    public void cancel() {
        use_ability(Abilities.CANCEL);
    }

    public Ability ability() {
        if (orders().size() == 0) {
            return Abilities.INVALID;
        }
        return orders().get(0).getAbility();
    }

    public int minerals() {
        return get_unit().getMineralContents().orElse(0);
    }

    public int gas() {
        return get_unit().getVespeneContents().orElse(0);
    }

    public int assigned_workers() {
        return get_unit().getAssignedHarvesters().orElse(0);
    }

    public int ideal_workers() {
        return get_unit().getIdealHarvesters().orElse(0);
    }

    Unit unit() {
        return get_unit();
    }

    public boolean is_gas() {
        return type() == Units.PROTOSS_ASSIMILATOR || type() == Units.TERRAN_REFINERY || type() == Units.ZERG_EXTRACTOR;
    }

    public double cooldown() {
        return get_unit().getWeaponCooldown().orElse(0.0f);
    }

    public boolean burrowed() {
        return get_unit().getBurrowed().orElse(false);
    }

    public double energy() {
        return get_unit().getEnergy().orElse(0.0f);
    }

    public double shields() {
        return get_unit().getShield().orElse(0.0f);
    }

    public boolean is_chronoed() {
        return get_unit().getBuffs().contains(Buffs.CHRONOBOOST_ENERGY_COST);
    }

    public boolean is_neuraled() {
        return get_unit().getBuffs().contains(Buffs.NEURAL_PARASITE);
    }

    public boolean is_burrowed() {
        return get_unit().getBurrowed().orElse(false);
    }

    public boolean is_melee() {
        for (Weapon w : Game.get_unit_type_data().get(get_unit().getType()).getWeapons()) {
            if (w.getRange() > 4) {
                return false;
            }
        }
        return true;
    }

    public double supply() {
        if (type() == Units.PROTOSS_ADEPT_PHASE_SHIFT) return 1;
        return Game.get_unit_type_data().get(type()).getFoodRequired().orElse(0f);
    }

    public double cargo() {
        return get_unit().getCargoSpaceTaken().orElse(0);
    }

    public double cargo_max() {
        return get_unit().getCargoSpaceMax().orElse(0);
    }
}
