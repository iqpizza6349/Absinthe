package com.tistory.workshop6349.unitcontrollers.protoss;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.tistory.workshop6349.game.AbsintheUnit;
import com.tistory.workshop6349.game.GameInfoCache;

public class Nexus {

    public static void on_frame(AbsintheUnit u) {
        if (u.energy() > 50) {
            for (AbsintheUnit struct : GameInfoCache.get_units(Alliance.SELF)) {
                if (struct.is_structure() && !struct.idle() && struct.done() && !struct.is_chronoed()) {
                    u.use_ability(Abilities.EFFECT_CHRONO_BOOST_ENERGY_COST, struct);
                }
            }
        }
    }
}
