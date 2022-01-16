package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AThreatManager {

    public static Map<Tag, Integer> seen = new HashMap<>();

    static void onFrame() {
        Set<Tag> to_remove = new HashSet<>();
        for (Tag t: seen.keySet()) {
            seen.put(t, seen.get(t) + 1);
            if (AGame.getUnit(t) == null || !AGame.getUnit(t).isAlive() || seen.get(t) > (AConstants.FPS * 20) / AConstants.FRAME_SKIP) {
                to_remove.add(t);
            }
        }
        for (Tag t: to_remove) {
            seen.remove(t);
        }
        for (UnitInPool u : AGameInfoCache.getUnits(Alliance.ENEMY)) {
            for (ABase b: ABaseManager.bases) {
                if (!b.hasFriendlyCommandStructure()) continue;
                if (b.location.distance(u.unit().getPosition().toPoint2d()) < 30 && u.unit().getType() != Units.PROTOSS_ADEPT_PHASE_SHIFT) {
                    seen.put(u.getTag(), 0);
                }
            }
        }
    }

    public static boolean underAttack() {
        return seen.size() >= 2;
    }

    public static boolean isSafe(Point2d p) {
        for (UnitInPool e: AGameInfoCache.getUnits(Alliance.ENEMY)) {
            if (!AGame.isWorker(e.unit().getType())) {
                if (e.unit().getPosition().toPoint2d().distance(p) < 10) {
                    return false;
                }
            }
        }
        return true;
    }
}
