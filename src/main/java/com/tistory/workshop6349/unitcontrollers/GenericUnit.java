package com.tistory.workshop6349.unitcontrollers;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Weapon;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.tistory.workshop6349.Vector2d;
import com.tistory.workshop6349.army.ArmyManager;
import com.tistory.workshop6349.army.UnitMovementManager;
import com.tistory.workshop6349.economy.Base;
import com.tistory.workshop6349.economy.BaseManager;
import com.tistory.workshop6349.game.AbsintheUnit;
import com.tistory.workshop6349.game.Game;
import com.tistory.workshop6349.game.GameInfoCache;
import com.tistory.workshop6349.knowledge.Wisdom;

import java.util.ArrayList;

public class GenericUnit {

    public static void on_frame(AbsintheUnit u, boolean moveOut) {

        for (AbsintheUnit disruptor_shot : GameInfoCache.get_units(Units.PROTOSS_DISRUPTOR_PHASED)) {
            if (disruptor_shot.distance(u) < 4) {
                u.move(disruptor_shot.location().directionTo(u.location()).scale(2).add(u.location()));
                return;
            }
        }

        if (!u.is_melee() && u.ability() == Abilities.ATTACK && u.orders().get(0).getTargetedUnitTag().isPresent() && Game.army_supply() < 100) {
            AbsintheUnit target = GameInfoCache.get_unit(u.orders().get(0).getTargetedUnitTag().get());
            if (target != null && (Game.is_town_hall(target.type()) || target.type() == Units.TERRAN_BUNKER)) {
                for (AbsintheUnit scv: GameInfoCache.get_units(Alliance.ENEMY, Units.TERRAN_SCV)) {
                    if (scv.distance(target) < 5) {
                        u.attack(scv);
                        return;
                    }
                }
            }
        }

        if (u.cooldown() > 0.1 & u.ability() == Abilities.ATTACK) {
            for (AbsintheUnit e: GameInfoCache.get_units(Alliance.ENEMY)) {
                if (e.type() == Units.PROTOSS_INTERCEPTOR) continue;
                if (Game.is_changeling(e.type())) continue;
                if (Game.get_unit_type_data().get(e.type()).getWeapons().size() > 0) {
                    Weapon best = null;
                    for (Weapon w: Game.get_unit_type_data().get(u.type()).getWeapons()) {
                        if (w.getTargetType() == Weapon.TargetType.ANY || (w.getTargetType() == Weapon.TargetType.AIR && e.flying()) || ( (w.getTargetType() == Weapon.TargetType.GROUND && !e.flying()))) {
                            best = w;
                        }
                    }
                    if (best != null) {
                        if (u.location().distance(e.location()) < best.getRange()) {
                            if (new ArrayList<>(Game.get_unit_type_data().get(e.type()).getWeapons()).get(0).getRange() < best.getRange()) {
                                Vector2d offset = u.location().directionTo(e.location());
                                Vector2d target = Vector2d.of(u.location().getX() - offset.getX(), u.location().getY() - offset.getY());
                                u.move(target);
                                return;
                            }
                        }
                    }
                }
            }
            return;
        }
        else if (UnitMovementManager.assignments.containsKey(u.tag())) {
            u.attack(UnitMovementManager.assignments.get(u.tag()));
            return;
        }

        if (Wisdom.cannon_rush()) return;

        if ((Wisdom.shouldAttack() && moveOut) || UnitMovementManager.has_defense_point()) {
            if (ArmyManager.has_target) {
                if (UnitMovementManager.has_defense_point()) {
                    u.attack(UnitMovementManager.defense_point());
                } else if (u.location().distance(ArmyManager.army_center) > 15) {
                    u.attack(ArmyManager.army_center);
                } else {
                    u.attack(ArmyManager.get_target());
                }
            } else {
                if (u.idle()) {
                    u.attack(Vector2d.of(Game.get_game_info().findRandomLocation()));
                    return;
                }
            }
            return;

        }
        if (moveOut && !Wisdom.shouldAttack()) {
            Base front = BaseManager.get_forward_base();
            if (u.location().distance(front.location) > 8) {
                u.move(front.location);
            }
        }
    }
}
