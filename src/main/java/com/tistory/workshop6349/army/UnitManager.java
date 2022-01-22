package com.tistory.workshop6349.army;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.tistory.workshop6349.game.AbsintheUnit;
import com.tistory.workshop6349.game.Game;
import com.tistory.workshop6349.game.GameInfoCache;
import com.tistory.workshop6349.unitcontrollers.Extractor;
import com.tistory.workshop6349.unitcontrollers.GenericUnit;
import com.tistory.workshop6349.unitcontrollers.Worker;
import com.tistory.workshop6349.unitcontrollers.protoss.Nexus;
import com.tistory.workshop6349.unitcontrollers.protoss.Stalker;

public class UnitManager {

    public static void onFrame() {
        for (AbsintheUnit unit : GameInfoCache.get_units(Alliance.SELF)) {
            if (Game.is_free_unit(unit.type())) {
                continue;
            }

            if (unit.is_worker()) {
                Worker.on_frame(unit);
            }
            else if (unit.is_gas()) {
                Extractor.on_frame(unit);
            }
            else if (unit.type() == Units.PROTOSS_STALKER) {
                Stalker.on_frame(unit);
            }
            else if (unit.type() == Units.PROTOSS_NEXUS) {
                Nexus.on_frame(unit);
            }
            else if (!Game.is_structure(unit.type())) {
                GenericUnit.on_frame(unit, true);
            }
        }
    }

}
