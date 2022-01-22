package com.tistory.workshop6349.unitcontrollers.zerg;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.tistory.workshop6349.game.AbsintheUnit;
import com.tistory.workshop6349.game.GameInfoCache;
import com.tistory.workshop6349.unitcontrollers.GenericUnit;

public class Lurker {
	public static void on_frame(AbsintheUnit u) {
		boolean near = false;
		for (AbsintheUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
			if (enemy.distance(u) < 10 && !enemy.flying()) {
				near = true;
			}
		} 
		if (near && !u.burrowed()) {
			u.use_ability(Abilities.BURROW_DOWN);
			return;
		} else if (!near && u.burrowed()) {
			u.use_ability(Abilities.BURROW_UP);
			return;
		} else if (!u.burrowed()){
			GenericUnit.on_frame(u, true);
			return;
		}
	}
}
