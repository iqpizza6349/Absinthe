package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Tag;

import java.util.*;

public class ABaseDefense {

    public static Set<Tag> used = new HashSet<>();
    public static Map<Tag, Point2d> assignments = new HashMap<>();

    public static void onFrame() {
        used.clear();
        assignments.clear();

        for (Set<UnitInPool> enemySquad : AEnemySquadManager.enemy_squads) {
            int supply = 0;
            boolean flyers = false;
            Point2d average = AEnemySquadManager.averagePoint(new ArrayList<>(enemySquad));
            for (ABase b : ABaseManager.bases) {
                if (b.hasFriendlyCommandStructure() && b.location.distance(average) < 15) {
                    for (UnitInPool enemy : enemySquad) {
                        supply += AGame.getUnitTypeData().get(enemy.unit().getType()).getFoodRequired().orElse((float) 0);
                        flyers = enemy.unit().getFlying().orElse(false) || flyers;
                    }

                    float assignedSupply = 0;
                    while (assignedSupply < supply * 1.5 || supply > 30) {
                        UnitInPool current = closestFree(average);
                        if  (current == null) {
                            break;
                        }

                        assignedSupply += AGame.getUnitTypeData().get(current.unit().getType()).getFoodRequired().orElse((float) 0);
                        assignments.put(current.getTag(), average);
                    }
                    break;
                }
            }
        }
    }

    public static UnitInPool closestFree(Point2d p) {
        UnitInPool best = null;
        for (UnitInPool ally : AControlGroups.get(0)) {
            if (!AGame.isStructure(ally.unit().getType()) && AGame.isCombat(ally.unit().getType())) {
                if (!used.contains(ally.getTag())) {
                    if (best == null || (best.unit().getPosition().toPoint2d().distance(p) / AGame.getUnitTypeData().get(best.unit().getType())
                            .getMovementSpeed().orElse((float) 1)) > (ally.unit().getPosition().toPoint2d().distance(p)) / AGame.getUnitTypeData().get(ally.unit().getType())
                            .getMovementSpeed().orElse((float) 1)) {
                        best = ally;
                    }
                }
            }
        }

        if (best != null) {
            used.add(best.getTag());
        }
        return best;
    }

}
