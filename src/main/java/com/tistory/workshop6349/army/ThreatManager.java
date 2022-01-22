package com.tistory.workshop6349.army;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.tistory.workshop6349.Vector2d;
import com.tistory.workshop6349.economy.Base;
import com.tistory.workshop6349.economy.BaseManager;
import com.tistory.workshop6349.enemy.EnemyModel;
import com.tistory.workshop6349.game.AbsintheUnit;
import com.tistory.workshop6349.game.Game;
import com.tistory.workshop6349.game.GameInfoCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ThreatManager {

    public static double threat = 0;
    public static double attacking_threat = 0;

    public static void on_frame() {
        Vector2d center;

        threat = 0;
        attacking_threat = 0;

        if (GameInfoCache.get_opponent_race() == Race.ZERG) {
            threat = 10;
        }

        for (Set<AbsintheUnit> squad : EnemySquadManager.enemy_squads) {
            center = EnemySquadManager.average_point(new ArrayList<>(squad));
            Base closest = null;
            for (Base b : BaseManager.bases) {
                if (closest == null || b.location.distance(center) < closest.location.distance(center)) {
                    closest = b;
                }
            }

            double supply = total_supply(new ArrayList<>(squad));

            boolean only_medivacs = true;

            for (AbsintheUnit u: squad) {
                if (u.type() != Units.TERRAN_MEDIVAC) {
                    only_medivacs = false;
                    break;
                }
            }

            if (only_medivacs) supply *= 10;

            if (closest.has_friendly_command_structure()) {
                threat += supply * 1.3;
                attacking_threat += supply;
            } else if (closest.has_enemy_command_structure()) {
                if (GameInfoCache.get_opponent_race() == Race.ZERG) {
                    threat += supply * 1.3;
                } else {
                    threat += supply * 0.8;
                }

            } else {
                threat += supply;
            }
        }

        int[] res = EnemyModel.resourceEstimate();
        threat += (Math.max(res[0], 0) / 100) * 0.4;

        threat -= Math.min(5 * (EnemyModel.enemyBaseCount() - BaseManager.base_count()), 12);

        for (AbsintheUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
            if (Game.is_worker(enemy.type())) {
                for (Base b : BaseManager.bases) {
                    if (b.has_friendly_command_structure() && enemy.distance(b) < 15) {
                        attacking_threat += 1;
                    }
                }
            }
        }

    }

    public static double total_supply(List<AbsintheUnit> list) {
        double result = 0;
        for (AbsintheUnit u : list) {
            result += u.supply();
        }
        return result;
    }

    public static double threat() {
        return threat;
    }

    public static double attacking_supply() {
        return attacking_threat;
    }

    public static boolean is_safe(Vector2d p) {
        for (AbsintheUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
            if (enemy.is_combat()) {
                if (enemy.distance(p) < 10) {
                    return false;
                }
            }
        }
        return true;
    }
}
