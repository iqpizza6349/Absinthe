package com.tistory.workshop6349.unitcontrollers;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.tistory.workshop6349.Vector2d;
import com.tistory.workshop6349.army.ThreatManager;
import com.tistory.workshop6349.economy.Base;
import com.tistory.workshop6349.economy.BaseManager;
import com.tistory.workshop6349.game.AbsintheUnit;
import com.tistory.workshop6349.game.GameInfoCache;
import com.tistory.workshop6349.game.RaceInterface;

public class Extractor {

    public static void on_frame(AbsintheUnit unit) {
        if (unit.assigned_workers() > 3) {
            for (AbsintheUnit u: GameInfoCache.get_units(Alliance.SELF, RaceInterface.get_race_worker())) {
                if (u.ability() == Abilities.HARVEST_GATHER) {
                    if (u.orders().get(0).getTargetedUnitTag().get().equals(unit.tag())) {
                        u.stop();
                        return;
                    }
                }
            }
        }
        if (!ThreatManager.is_safe(unit.location())) return;
        if (unit.done()) {
            if (is_near_base(unit.location())) {
                if (unit.assigned_workers() < unit.ideal_workers()) {
                    AbsintheUnit best = BaseManager.get_free_worker(unit.location());
                    if (best != null) {
                        best.use_ability(Abilities.SMART, unit);
                    }
                }
            }
        }
    }

    private static boolean is_near_base(Vector2d p) {
        for (Base b: BaseManager.bases) {
            if (b.has_friendly_command_structure() && b.location.distance(p) < 10) return true;
        }
        return false;
    }
}
