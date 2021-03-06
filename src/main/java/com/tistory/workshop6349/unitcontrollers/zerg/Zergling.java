package com.tistory.workshop6349.unitcontrollers.zerg;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.data.Upgrades;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.tistory.workshop6349.Vector2d;
import com.tistory.workshop6349.army.BanelingAvoidance;
import com.tistory.workshop6349.army.UnitMovementManager;
import com.tistory.workshop6349.economy.Base;
import com.tistory.workshop6349.economy.BaseManager;
import com.tistory.workshop6349.game.AbsintheUnit;
import com.tistory.workshop6349.game.Game;
import com.tistory.workshop6349.game.GameInfoCache;
import com.tistory.workshop6349.unitcontrollers.GenericUnit;

public class Zergling {

	public static void on_frame(AbsintheUnit u) {
		
		if (BanelingAvoidance.banelingAssignments.containsKey(u.tag())) {
			if (GameInfoCache.get_unit(BanelingAvoidance.banelingAssignments.get(u.tag())) != null) {
				u.attack(GameInfoCache.get_unit(BanelingAvoidance.banelingAssignments.get(u.tag())));
				return;
			}
		}
		
		if (Game.has_upgrade(Upgrades.BURROW)) {
			for (Base b : BaseManager.bases) {
				if (!b.has_command_structure() && b != BaseManager.get_next_base()) {
					if (!b.has_ling() || b.ling == u) {
						if (u.distance(b) > 2) {
							u.move(b.location); 
							b.ling = u;
						} else {
							u.use_ability(Abilities.BURROW_DOWN);
							b.ling = u;
						}
						return;
					}
				}
			}
		}
		
		for (AbsintheUnit enemyBane: GameInfoCache.get_units(Alliance.ENEMY, Units.ZERG_BANELING)) {
			if (u.location().distance(enemyBane.location()) < 4) {
				Vector2d offset = new Vector2d(u.location().getX() - enemyBane.location().getX(), u.location().getY() - enemyBane.location().getY());
				offset = offset.normalized().scale(3f);
				u.move(offset.add(u.location()));
				return;
			}
		}
		
		if (u.burrowed()) {
			for (AbsintheUnit drone : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
				if (drone.distance(u) < 5) {
					u.use_ability(Abilities.BURROW_UP);
				}
			}
			if (u.distance(BaseManager.get_next_base()) < 5) {
				u.use_ability(Abilities.BURROW_UP);
			}
		}


		if (UnitMovementManager.assignments.containsKey(u.tag()) && UnitMovementManager.surroundCenter.containsKey(u.tag())) {
			if (UnitMovementManager.surroundCenter.get(u.tag()).distance(UnitMovementManager.assignments.get(u.tag())) < 1.5) {
				u.attack(UnitMovementManager.assignments.get(u.tag()));
			} else {
				Vector2d offset = UnitMovementManager.surroundCenter.get(u.tag()).directionTo(UnitMovementManager.assignments.get(u.tag()));
				Vector2d result = UnitMovementManager.assignments.get(u.tag()).add(offset.scale(6));
				if (Game.pathable(result) && Math.abs(Game.height(result) - Game.height(u.location())) < 0.5) {
					u.move(result);
				} else {
					u.attack(UnitMovementManager.assignments.get(u.tag()));
				}
			}
		} else {
			GenericUnit.on_frame(u, true);
		}
	}
}
