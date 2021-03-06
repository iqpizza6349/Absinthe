package com.tistory.workshop6349.unitcontrollers.zerg;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.observation.AvailableAbility;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.tistory.workshop6349.Constants;
import com.tistory.workshop6349.Vector2d;
import com.tistory.workshop6349.economy.Base;
import com.tistory.workshop6349.economy.BaseManager;
import com.tistory.workshop6349.game.AbsintheUnit;
import com.tistory.workshop6349.game.Game;
import com.tistory.workshop6349.game.GameInfoCache;
import com.tistory.workshop6349.knowledge.Scouting;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Creep {
	private static final Map<ImmutablePair<Integer, Integer>, Integer> reserved = new HashMap<>();
	private static int[][] terrain = new int[1000][1000];
	private static final int[][] bases = new int[1000][1000];
	private static final Map<Tag, Integer> used = new HashMap<>();
	private static List<Vector2d> creep_points = new ArrayList<>();
	static {
		Vector2d min = Game.min_point();
		Vector2d max = Game.max_point();
		for (int x = (int) min.getX(); x < max.getX(); x += Constants.CREEP_RESOLUTION) {
			for (int y = (int) min.getY(); y < max.getY(); y += Constants.CREEP_RESOLUTION) {
				for (Base b : BaseManager.bases) {
					if (b.location.distance(Vector2d.of(x, y)) < 6) {
						bases[x][y] = 1;
					}
				}
			}
		}
	}
	
	public static void start_frame() {
		calculate();
	}
	
	private static long last_update = -999;
	private static void calculate() {
		if (Game.get_frame() - last_update < 1.0 * Constants.FPS) return;
		last_update = Game.get_frame();
		ArrayList<ImmutablePair<Integer, Integer>> to_erase = new ArrayList<>();
		for (ImmutablePair<Integer, Integer> item : reserved.keySet()) {
			if (reserved.get(item) < Game.get_frame() - Constants.FPS * 10) {
				to_erase.add(item);
			}
		}
		for (ImmutablePair<Integer, Integer> item : to_erase) reserved.remove(item);
		terrain = new int[1000][1000];
		creep_points = new ArrayList<>();
		List<Vector2d> alt = new ArrayList<>();
		
		Vector2d min = Game.min_point();
		Vector2d max = Game.max_point();
		for (int x = (int) min.getX(); x <= max.getX(); x += Constants.CREEP_RESOLUTION) {
			for (int y = (int) min.getY(); y <= max.getY(); y += Constants.CREEP_RESOLUTION) {
				if (Game.pathable(Vector2d.of(x, y)) && bases[x][y] == 0) {
					if (Game.on_creep(Vector2d.of(x, y)) && Game.is_visible(Vector2d.of(x, y))) {
						terrain[x][y] = 1;
					}
				} else {
					terrain[x][y] = -1;
				}
			}
		}
		for (int x = (int) min.getX(); x <= max.getX(); x += Constants.CREEP_RESOLUTION) {
			for (int y = (int) min.getY(); y <= max.getY(); y += Constants.CREEP_RESOLUTION) {
				if (terrain[x][y] == 1) {
					for (Vector2d p : around(Vector2d.of(x, y))) {
						if (terrain[(int) p.getX()][(int) p.getY()] == 0) {
							Base best = BaseManager.bases.get(0);
							for (Base b: BaseManager.bases) {
								if (b.location.distance(p) < best.location.distance(p)) {
									best = b;
								}
							}
							boolean first3 = false;
							for (int i = 0; i < 3; i++) {
								if (best.location.distance(BaseManager.get_base(i)) < 1) {
									first3 = true;
									break;
								}
							}
							if (!first3 || Scouting.closest_enemy_spawn().distance(best.location) > Scouting.closest_enemy_spawn().distance(p)) {
								if (Game.height(Vector2d.of(x,  y)) == Game.height(BaseManager.main_base().location)) {
									alt.add(Vector2d.of(x, y));
								} else {
									Game.draw_box(Vector2d.of(x, y), Color.PURPLE);
									creep_points.add(Vector2d.of(x, y));
								}
							}
						}
					}
				}
			}
		}
		if (creep_points.size() == 0) creep_points.addAll(alt);
		
		for (AbsintheUnit u : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_CREEP_TUMOR_BURROWED)) {
			for (int i = creep_points.size() - 1; i > 0; i--) {
				if (u.distance(creep_points.get(i)) <= 4) {
					creep_points.remove(i);
				}
			}
		}
		
		for (AbsintheUnit u : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_CREEP_TUMOR_QUEEN)) {
			for (int i = creep_points.size() - 1; i > 0; i--) {
				if (u.distance(creep_points.get(i)) <= 4) {
					creep_points.remove(i);
				}
			}
		}
		
		for (AbsintheUnit u : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_CREEP_TUMOR)) {
			for (int i = creep_points.size() - 1; i > 0; i--) {
				if (u.distance(creep_points.get(i)) <= 4) {
					creep_points.remove(i);
				}
			}
		}
		
	}

	public static void on_frame(AbsintheUnit u) {
		if (used.getOrDefault(u.tag(), 0) < 300 && u.done()) {
			Game.write_text("Attemping to spread", u.location());
			boolean found = false;
			for (AvailableAbility x : Game.availible_abilities(u).getAbilities()) {
				if (x.getAbility() == Abilities.BUILD_CREEP_TUMOR) {
					found = true;
					spread_towards(u, Scouting.closest_enemy_spawn());
					break;
				}
			}
			if (!found) {
				used.put(u.tag(), used.getOrDefault(u.tag(), 0) + 1);
			}
		}
	}
	
	private static void spread_towards(AbsintheUnit u, Vector2d p) {
		
		Vector2d best = null;
		for (Vector2d point: creep_points) {
			if (point.distance(u.location()) < 10) {
				if (best == null || best.distance(p) > point.distance(p)) {
					if (Game.can_place(Abilities.BUILD_CREEP_TUMOR_QUEEN, point)) {
						best = point;
					}
				}
			}
		}
		if (best != null) {
			Game.draw_line(u.location(), best, Color.PURPLE);
			u.use_ability(Abilities.BUILD_CREEP_TUMOR, best);
			used.put(u.tag(), used.getOrDefault(u.tag(), 0) + 1);
		}

	}
	
	private static double score(Vector2d p) {
		return p.distance(BaseManager.main_base().location);
	}
	
	public static Vector2d get_creep_point() {
		Vector2d best = null;
		for (Vector2d p : creep_points) {
			if (!reserved.containsKey(new ImmutablePair<>((int) p.getX(), (int) p.getY()))) {
				if (best == null || score(best) > score(p)) {
					best = p;
				}
			}
		}
		if (best != null) {
			reserved.put(new ImmutablePair<>((int) best.getX(), (int) best.getY()), (int) Game.get_frame());
		}
		return best;
	}
	
	private static Vector2d[] around(Vector2d p) {
		Vector2d[] result = new Vector2d[4];
		result[0] = Vector2d.of(p.getX() + Constants.CREEP_RESOLUTION, p.getY());
		result[1] = Vector2d.of(p.getX() - Constants.CREEP_RESOLUTION, p.getY());
		result[2] = Vector2d.of(p.getX(), p.getY() + Constants.CREEP_RESOLUTION);
		result[3] = Vector2d.of(p.getX(), p.getY() - Constants.CREEP_RESOLUTION);
		return result;
	}
}
