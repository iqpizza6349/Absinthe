package com.tistory.workshop6349.army;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.tistory.workshop6349.Constants;
import com.tistory.workshop6349.Vector2d;
import com.tistory.workshop6349.game.AbsintheUnit;
import com.tistory.workshop6349.game.Game;
import com.tistory.workshop6349.game.GameInfoCache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnemySquadManager {

    public static final ArrayList<Set<AbsintheUnit>> enemy_squads = new ArrayList<>();
    public static void on_frame() {
        Set<Tag> parsed = new HashSet<>();
        enemy_squads.clear();
        for (AbsintheUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
            if (Game.is_structure(enemy.type()) || (!Game.is_combat(enemy.type()) && enemy.type() != Units.PROTOSS_ADEPT_PHASE_SHIFT)) continue;
            if (!parsed.contains(enemy.tag())) {
                List<AbsintheUnit> open = new ArrayList<>();
                Set<AbsintheUnit> squad = new HashSet<>();
                open.add(enemy);
                squad.add(enemy);
                while (open.size() > 0) {
                    AbsintheUnit current = open.remove(0);
                    for (AbsintheUnit enemy2: GameInfoCache.get_units(Alliance.ENEMY)) {
                        if (Game.is_structure(enemy2.type()) || !Game.is_combat(enemy2.type())) continue;
                        if (enemy2.tag() != current.tag() && !parsed.contains(enemy2.tag())) {
                            if (enemy2.distance(current) < Constants.ENEMY_SQUAD_DISTANCE) {
                                open.add(enemy2);
                                parsed.add(enemy2.tag());
                                squad.add(enemy2);
                            }
                        }
                    }
                }
                enemy_squads.add(squad);
                Game.write_text(String.valueOf(ThreatManager.total_supply(new ArrayList<>(squad))), average_point(new ArrayList<>(squad)));
            }
        }
    }

    public static Vector2d average_point(List<AbsintheUnit> l) {
        if (l.size() == 0) return Vector2d.of(0, 0);

        float x = 0;
        float y = 0;
        int n = 0;
        for (AbsintheUnit u : l) {
            x += u.location().getX();
            y += u.location().getY();
            n++;
        }
        return Vector2d.of(x / n, y / n);
    }
}
