package com.tistory.workshop6349.army;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.tistory.workshop6349.game.AbsintheUnit;
import com.tistory.workshop6349.game.GameInfoCache;

import java.util.*;

public class BanelingAvoidance {

    public static final Map<Tag, Tag> banelingAssignments = new HashMap<>();
    public static void on_frame() {

        List<Tag> to_remove = new ArrayList<>();
        for (Tag t: banelingAssignments.keySet()) {
            if (GameInfoCache.get_unit(t) == null) {
                to_remove.add(t);
            }
        }
        for (Tag t: banelingAssignments.keySet()) {
            if (GameInfoCache.get_unit(banelingAssignments.get(t)) == null) {
                to_remove.add(t);
            }
        }
        for (Tag t: to_remove) {
            banelingAssignments.remove(t);
        }

        for (AbsintheUnit enemyBane: GameInfoCache.get_units(Alliance.ENEMY, Units.ZERG_BANELING)) {
            if (Collections.frequency(banelingAssignments.values(), enemyBane.tag()) < 2) {
                for (AbsintheUnit allyLing : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_ZERGLING)) {
                    if (banelingAssignments.containsKey(allyLing.tag())) continue;
                    if (allyLing.distance(enemyBane) < 10) {
                        banelingAssignments.put(allyLing.tag(), enemyBane.tag());
                        break;
                    }
                }
            }
        }
    }
}
