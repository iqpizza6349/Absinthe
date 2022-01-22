package com.tistory.workshop6349.unitcontrollers.zerg;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.tistory.workshop6349.game.AbsintheUnit;
import com.tistory.workshop6349.game.Game;
import com.tistory.workshop6349.game.GameInfoCache;

import java.util.ArrayList;
import java.util.List;

public class Larva {
	private static final List<AbsintheUnit> larva = new ArrayList<>();
	private static int larva_index = 0;
	public static void start_frame() {
		larva_index = 0;
		larva.clear();
		larva.addAll(GameInfoCache.get_units(Alliance.SELF, Units.ZERG_LARVA));
	}
	public static boolean has_larva() {
		return larva_index < larva.size();
	}

    public static void produce_unit(UnitType type) {
    	if (has_larva() && type != Units.INVALID) {
    		larva.get(larva_index).use_ability(Game.get_unit_type_data().get(type).getAbility().get());
    		larva_index++;
    	}
	}
}
