package com.tistory.workshop6349.unitcontrollers.protoss;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.tistory.workshop6349.Constants;
import com.tistory.workshop6349.army.EnemySquadManager;
import com.tistory.workshop6349.game.AbsintheUnit;
import com.tistory.workshop6349.game.Game;
import com.tistory.workshop6349.game.GameInfoCache;
import com.tistory.workshop6349.unitcontrollers.GenericUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stalker {

    private static final Map<Tag, Long> last_blink_frame = new HashMap<>();

    public static void on_frame(AbsintheUnit u) {
        if (Game.has_upgrade(Upgrades.BLINK_TECH) && Game.get_frame() - last_blink_frame.getOrDefault(u.tag(), (long) 0) > 10 * Constants.FPS) {
            if (u.shields() < 1) {
                List<AbsintheUnit> result = new ArrayList<>();
                for (AbsintheUnit enemy : GameInfoCache.get_units(Alliance.ENEMY)) {
                    if (Game.is_combat(enemy.type()) && enemy.distance(u) < 12) {
                        result.add(enemy);
                    }
                }
                if (result.size() > 0) {
                    last_blink_frame.put(u.tag(), Game.get_frame());
                    u.use_ability(Abilities.EFFECT_BLINK, u.location().add(u.location().directionTo(EnemySquadManager.average_point(result)).scale(-8)));
                    return;
                }
            }
        }

        GenericUnit.on_frame(u, true);
    }
}
