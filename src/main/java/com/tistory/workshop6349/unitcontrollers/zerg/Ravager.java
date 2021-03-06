package com.tistory.workshop6349.unitcontrollers.zerg;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.observation.AvailableAbility;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.tistory.workshop6349.Constants;
import com.tistory.workshop6349.Vector2d;
import com.tistory.workshop6349.economy.BaseManager;
import com.tistory.workshop6349.game.AbsintheUnit;
import com.tistory.workshop6349.game.Game;
import com.tistory.workshop6349.game.GameInfoCache;
import com.tistory.workshop6349.knowledge.Scouting;
import com.tistory.workshop6349.unitcontrollers.GenericUnit;

import java.util.HashMap;
import java.util.Map;

public class Ravager {
	
	public static Map<Vector2d, Long> ff_biles = new HashMap<>();
	
	static UnitType[] bile_targets = {	Units.NEUTRAL_FORCE_FIELD, Units.PROTOSS_WARP_PRISM_PHASING, Units.TERRAN_SIEGE_TANK_SIEGED, Units.PROTOSS_PHOTON_CANNON, Units.ZERG_SPINE_CRAWLER, Units.TERRAN_BUNKER};
	
	public static void on_frame(AbsintheUnit u2) {
		
		AbsintheUnit best = null;
		
		for (UnitType target_type : bile_targets) {
			for (AbsintheUnit u : GameInfoCache.get_units(Alliance.ENEMY, target_type)) {
				if (u.distance(u2) < 9) {
					if (target_type != Units.NEUTRAL_FORCE_FIELD || (!ff_biles.containsKey(u.location()) || ff_biles.get(u.location()) < Game.get_frame() - (3 * Constants.FPS))) {
						best = u;
					}
				}
			}
			if (best != null) break;
		}
		
		if (best == null) {
			for (AbsintheUnit u : GameInfoCache.get_units(Alliance.ENEMY)) {
				if (u.distance(u2) < 9 && u.is_structure()) {
					best = u;
				}
				if (best != null) break;
			}
		}
		
		if (best == null) {
			for (AbsintheUnit u : GameInfoCache.get_units(Alliance.ENEMY)) {
				if (u.distance(u2) < 9) {
					best = u;
				}
				if (best != null) break;
			}
		}
		
		if (best != null) {
			for (AvailableAbility ab : Game.availible_abilities(u2).getAbilities()) {
				if (ab.getAbility() == Abilities.EFFECT_CORROSIVE_BILE) {
					
					if (best.type() == Units.NEUTRAL_FORCE_FIELD) {
						ff_biles.put(best.location(), Game.get_frame());
					}
					
					u2.use_ability(Abilities.EFFECT_CORROSIVE_BILE, best.location());
					return;
				}
			}
			if (best.type() == Units.PROTOSS_PHOTON_CANNON || best.type() == Units.TERRAN_BUNKER || best.type() == Units.ZERG_SPINE_CRAWLER) {
				if (best.distance(BaseManager.main_base().location) < best.distance(Scouting.closest_enemy_spawn())) {
					Vector2d diff = best.location().directionTo(u2.location());
					u2.move(Vector2d.of(best.location().getX() + diff.getX() * 15, best.location().getY() + diff.getY() * 15));
					return;
				}
			}
		}
		if (u2.ability() != Abilities.EFFECT_CORROSIVE_BILE) GenericUnit.on_frame(u2, true);
	}
}
