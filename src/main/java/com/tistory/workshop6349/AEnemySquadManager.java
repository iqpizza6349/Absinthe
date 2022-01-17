package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AEnemySquadManager {

    public static ArrayList<Set<UnitInPool>> enemy_squads = new ArrayList<>();
    public static void onFrame() {
        Set<Tag> parsed = new HashSet<>();
        enemy_squads.clear();
        for (UnitInPool a: AGameInfoCache.getUnits(Alliance.ENEMY)) {
            if (AGame.isStructure(a.unit().getType())) continue;
            if (!parsed.contains(a.getTag())) {
                List<UnitInPool> open = new ArrayList<>();
                Set<UnitInPool> squad = new HashSet<>();
                open.add(a);
                squad.add(a);
                while (open.size() > 0) {
                    UnitInPool current = open.remove(0);
                    for (UnitInPool b: AGameInfoCache.getUnits(Alliance.ENEMY)) {
                        if (AGame.isStructure(b.unit().getType())) continue;
                        if (b.getTag() != current.getTag() && !parsed.contains(b.getTag())) {
                            if (b.unit().getPosition().toPoint2d().distance(current.unit().getPosition().toPoint2d()) < AConstants.ENEMY_SQUAD_DISTANCE) {
                                open.add(b);
                                parsed.add(b.getTag());
                                squad.add(b);
                            }
                        }
                    }
                }
                enemy_squads.add(squad);
            }
        }
    }

    public static Point2d averagePoint(List<UnitInPool> l) {
        float x = 0;
        float y = 0;
        int n = 0;
        for (UnitInPool u : l) {
            x += u.unit().getPosition().getX();
            y += u.unit().getPosition().getY();
            n++;
        }
        return Point2d.of(x / n, y / n);
    }
}
