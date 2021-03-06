package com.tistory.workshop6349.unitcontrollers.zerg;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.tistory.workshop6349.Vector2d;
import com.tistory.workshop6349.army.ArmyManager;
import com.tistory.workshop6349.game.AbsintheUnit;
import com.tistory.workshop6349.game.GameInfoCache;

import java.util.HashSet;
import java.util.Set;

public class Viper {
	
	private static final Set<UnitType> yoink_targets = new HashSet<>();
	static {
		yoink_targets.add(Units.PROTOSS_CARRIER);
		yoink_targets.add(Units.PROTOSS_TEMPEST);
		yoink_targets.add(Units.PROTOSS_COLOSSUS);
		yoink_targets.add(Units.PROTOSS_IMMORTAL);
		yoink_targets.add(Units.PROTOSS_DISRUPTOR);
		
		yoink_targets.add(Units.TERRAN_SIEGE_TANK);
		yoink_targets.add(Units.TERRAN_SIEGE_TANK_SIEGED);
		yoink_targets.add(Units.TERRAN_THOR);
		yoink_targets.add(Units.TERRAN_THOR_AP);
		yoink_targets.add(Units.TERRAN_LIBERATOR_AG);
		yoink_targets.add(Units.TERRAN_BATTLECRUISER);
		
	}
	
	public static void on_frame(AbsintheUnit u) {
		if (u.ability() == Abilities.EFFECT_VIPER_CONSUME) return;
		
		if (u.energy() < 100) {
			for (AbsintheUnit structure : GameInfoCache.get_units(Alliance.SELF)) {
				if (structure.is_structure() && structure.health() > 600) {
					u.use_ability(Abilities.EFFECT_VIPER_CONSUME, structure);
					return;
				}
			}
		}
		
		for (AbsintheUnit enemy : GameInfoCache.get_units(Alliance.ENEMY)) {
			if (yoink_targets.contains(enemy.type())) {
				if (enemy.distance(u) < 14 && enemy.distance(ArmyManager.army_center) > 5) {
					u.use_ability(Abilities.EFFECT_ABDUCT, enemy);
					return;
				}
			}
		}
		
		if (ArmyManager.army_center.distance(Vector2d.of(0, 0)) > 1) {
			if (u.distance(ArmyManager.army_center) > 5) {
				u.move(ArmyManager.army_center);
			}
		} else {
			if (u.distance(ArmyManager.army_center) > 5) {
				u.move(ArmyManager.army_center);
			}
		}
	}
}
