package com.tistory.workshop6349.unitcontrollers.zerg;

import com.tistory.workshop6349.army.ArmyManager;
import com.tistory.workshop6349.army.UnitMovementManager;
import com.tistory.workshop6349.game.AbsintheUnit;

public class Overseer {

	public static void on_frame(AbsintheUnit u) {
		if (UnitMovementManager.assignments.containsKey(u.tag())) {
			u.move(UnitMovementManager.assignments.get(u.tag()));
		} else if (u.distance(ArmyManager.army_center) > 5) {
			u.move(ArmyManager.army_center);
		}
	}
}
