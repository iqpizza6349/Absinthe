package com.tistory.workshop6349.army;

import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.tistory.workshop6349.Constants;
import com.tistory.workshop6349.Vector2d;
import com.tistory.workshop6349.army.UnitRoleManager.UnitRole;
import com.tistory.workshop6349.economy.Base;
import com.tistory.workshop6349.economy.BaseManager;
import com.tistory.workshop6349.enemy.EnemyBaseDefense;
import com.tistory.workshop6349.enemy.EnemyModel;
import com.tistory.workshop6349.game.AbsintheUnit;
import com.tistory.workshop6349.game.Game;
import com.tistory.workshop6349.game.GameInfoCache;
import com.tistory.workshop6349.knowledge.Scouting;
import com.tistory.workshop6349.knowledge.Wisdom;
import com.tistory.workshop6349.knowledge.ZergWisdom;
import com.tistory.workshop6349.unitcontrollers.zerg.Queen;

import java.util.*;

public class UnitMovementManager {

    private static final Set<Tag> used = new HashSet<>();
    public static final Map<Tag, Vector2d> assignments = new HashMap<>();
    public static final Map<Tag, Vector2d> surroundCenter = new HashMap<>();

    private static Map<Set<AbsintheUnit>, Boolean> assigned = new HashMap<>();

    private static Vector2d defense_point;
    public static int detection_points;
    public static int unassigned_ground = 0;
    public static int unassigned_air = 0;

    public static double attack_threshold = 1.1;

    public static boolean chase_aggressively = false;

    public static void on_frame() {

        boolean new_chase_aggressively = false;

        if (GameInfoCache.attacking_army_supply() >= EnemyModel.enemyArmy() * 1.9) {
            attack_threshold = 1.1;
        }

        if (GameInfoCache.attacking_army_supply() <= EnemyModel.enemyArmy() * 1.1) {
            attack_threshold = 1.9;
        }

        defense_point = null;
        used.clear();
        assignments.clear();
        surroundCenter.clear();
        detection_points = 0;

        assigned.clear();

        for (AbsintheUnit ally : UnitRoleManager.get(UnitRole.ARMY)) {
            if (Game.hits_air(ally.type())) {
                unassigned_air += ally.supply();
            } else {
                unassigned_ground += ally.supply();
            }
        }


        for (Set<AbsintheUnit> enemy_squad : EnemySquadManager.enemy_squads) {

            assigned.put(enemy_squad, false);

            Vector2d average = EnemySquadManager.average_point(new ArrayList<>(enemy_squad));

            for (Base b : BaseManager.bases) {
                try {

                    int engage_distance = Constants.THREAT_DISTANCE;

                    if (ZergWisdom.needed_spine_count() > 0) {
                        engage_distance = 10;
                    }

                    if (chase_aggressively && !(Wisdom.all_in_detected() && GameInfoCache.get_opponent_race() == Race.ZERG && Game.army_supply() < 30 && EnemyModel.counts.getOrDefault(Units.ZERG_ROACH, 0) == 0)) {
                        if (ZergWisdom.needed_spine_count() == 0) {
                            engage_distance *= 2.5;
                        }
                    }

                    if (((b.has_friendly_command_structure() || b.equals(BaseManager.get_next_base())) &&
                            b.location.distance(average) < engage_distance) && !BaseManager.closest_base(average).has_enemy_command_structure()) {
                        assign_defense(enemy_squad);
                        assigned.put(enemy_squad, true);
                        Game.write_text("Chasing away", EnemySquadManager.average_point(new ArrayList<>(enemy_squad)));
                        new_chase_aggressively = true;

                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                    Game.write_text("Unable to defend 1", EnemySquadManager.average_point(new ArrayList<>(enemy_squad)));
                }
            }
        }

        chase_aggressively = new_chase_aggressively;

        for (Set<AbsintheUnit> enemy_squad : EnemySquadManager.enemy_squads) {
            if (assigned.get(enemy_squad)) continue;

            double enemy_strength = ThreatManager.total_supply(new ArrayList<>(enemy_squad));
            double my_strength = ThreatManager.total_supply(UnitRoleManager.get(UnitRole.ARMY));

            Vector2d average = EnemySquadManager.average_point(new ArrayList<>(enemy_squad));
            try {
                if (BaseManager.closest_base(average).has_friendly_command_structure() &&
                        my_strength > enemy_strength * attack_threshold) {


                    Game.write_text("Defending " + my_strength + " " + enemy_strength, EnemySquadManager.average_point(new ArrayList<>(enemy_squad)));
                    assign_defense(enemy_squad);
                    assigned.put(enemy_squad, true);

                }
            } catch (Exception e) {
                e.printStackTrace();
                Game.write_text("Unable to defend 2", EnemySquadManager.average_point(new ArrayList<>(enemy_squad)));
            }
        }

        for (Set<AbsintheUnit> enemy_squad : EnemySquadManager.enemy_squads) {
            if (assigned.get(enemy_squad)) continue;

            Vector2d average = EnemySquadManager.average_point(new ArrayList<>(enemy_squad));

            double enemy_strength = ThreatManager.total_supply(new ArrayList<>(enemy_squad));
            double my_strength = ThreatManager.total_supply(UnitRoleManager.get(UnitRole.ARMY)) - GameInfoCache.count_friendly(Units.ZERG_QUEEN) * 2;

            try {
                if ((Game.closest_invisible(average).distance(average) > 7 &&
                        my_strength > enemy_strength * attack_threshold &&
                        !BaseManager.closest_base(average).has_enemy_command_structure())) {

                    Game.write_text("Crushing " + my_strength + " " + enemy_strength, EnemySquadManager.average_point(new ArrayList<>(enemy_squad)));
                    assign_defense(enemy_squad);
                    assigned.put(enemy_squad, true);

                }
            } catch (Exception e) {
                e.printStackTrace();
                Game.write_text("Unable to defend 3", EnemySquadManager.average_point(new ArrayList<>(enemy_squad)));
            }

        }

        int dist = 30;
        if (GameInfoCache.get_opponent_race() == Race.ZERG) {
            dist = 45;
        }
        for (Base b : BaseManager.bases) {
            if (Wisdom.army_ratio() > 1.0 && b.has_enemy_command_structure() && EnemyBaseDefense.get_defense(b) < GameInfoCache.attacking_army_supply() && EnemyBaseDefense.get_defense(b) < 10 && Scouting.closest_enemy_spawn(b.location).distance(b.location) > dist) {
                assign_runby(b.location, Math.max(EnemyBaseDefense.get_defense(b) + 2, 6));
            }
        }

        if (Wisdom.proxy_detected() && GameInfoCache.attacking_army_supply() > EnemyModel.enemySupply() * attack_threshold) {
            AbsintheUnit best = null;
            for (AbsintheUnit enemy: GameInfoCache.get_units(Alliance.ENEMY)) {
                if (Game.is_structure(enemy.type())) {
                    if (best == null || best.distance(BaseManager.main_base()) > enemy.distance(BaseManager.main_base())) {
                        best = enemy;
                    }
                }
            }
            if (best != null) {
                for (AbsintheUnit u : UnitRoleManager.get(UnitRole.ARMY)) {
                    assignments.put(u.tag(), best.location());
                }
                assign_runby(best.location(), 50);
            }
        }
    }


    private static void assign_defense(Set<AbsintheUnit> enemy_squad) {
        float ground_supply = 0;
        float flyer_supply = 0;
        boolean needs_detection = false;

        Vector2d average = EnemySquadManager.average_point(new ArrayList<>(enemy_squad));

        boolean lib = false;

        for (AbsintheUnit enemy : enemy_squad) {
            if (enemy.type() == Units.TERRAN_LIBERATOR) {
                lib = true;
                flyer_supply += 1;
            }
            if (enemy.type() == Units.PROTOSS_ADEPT_PHASE_SHIFT) ground_supply += 1;
            if (enemy.flying()) {
                flyer_supply += Game.supply(enemy.type());
            } else {
                ground_supply += Game.supply(enemy.type());
            }
            needs_detection = needs_detection || enemy.cloaked() || enemy.type() == Units.TERRAN_BANSHEE || enemy.type() == Units.TERRAN_GHOST || enemy.type() == Units.PROTOSS_MOTHERSHIP;
        }

        if (needs_detection) detection_points++;

        ArrayList<AbsintheUnit> assigned = new ArrayList<>();
        if (ground_supply > 70 || flyer_supply > 70) {
            defense_point = average;
            return;
        }

        float assigned_supply = 0;
        while (assigned_supply < ground_supply * 1.3) {
            AbsintheUnit current = closest_free(average, false, false);
            if (current == null) current = closest_free(average, true, false);
            if (current == null) break;
            assigned_supply += Game.supply(current.type()) * (current.health() / current.health_max());
            if (current.type() == Units.ZERG_MUTALISK) assigned_supply--;
            if (current.type() == Units.ZERG_QUEEN) assigned_supply -= 1;
            assigned.add(current);
        }
        assigned_supply = 0;
        while (assigned_supply < flyer_supply) {
            AbsintheUnit current = closest_free(average, true, lib);
            if (current == null) break;
            assigned_supply += Game.supply(current.type()) * (current.health() / current.health_max());
            if (current.type() == Units.ZERG_MUTALISK) assigned_supply--;
            if (current.type() == Units.ZERG_QUEEN) assigned_supply -= 1;
            assigned.add(current);
        }
        if (needs_detection) {
            AbsintheUnit current = closest_free_detection(average);
            if (current != null) {
                assigned.add(current);
            }
        }
        Vector2d center = average_point_zergling(assigned, average);
        for (AbsintheUnit u: assigned) {
            assignments.put(u.tag(), average);
            if (center.distance(Vector2d.of(0, 0)) > 1 && enemy_squad.size() * 2.5 <= assigned.size()) {
                surroundCenter.put(u.tag(), center);
            }
            Game.draw_line(average, u.location(), Color.GREEN);
        }
    }

    private static void assign_runby(Vector2d average, double supply) {
        ArrayList<AbsintheUnit> assigned = new ArrayList<>();
        float assigned_supply = 0;
        while (assigned_supply < supply) {
            AbsintheUnit current = closest_free_aggressor(average);
            if (current == null) break;
            assigned_supply += Game.supply(current.type()) * (current.health() / current.health_max());
            assigned.add(current);
        }

        for (AbsintheUnit u: assigned) {
            assignments.put(u.tag(), average);
            Game.draw_line(average, u.location(), Color.RED);
        }
    }

    private static AbsintheUnit closest_free_detection(Vector2d p) {
        AbsintheUnit best = null;
        for (AbsintheUnit ally : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_OVERSEER)) {
            if (!assignments.containsKey(ally.tag())) {
                if (best == null || best.distance(p) > ally.distance(p)) {
                    best = ally;
                }
            }
        }
        return best;
    }

    private static AbsintheUnit closest_free(Vector2d p, boolean aa, boolean lib) {
        AbsintheUnit best = null;
        for (AbsintheUnit ally : UnitRoleManager.get(UnitRole.ARMY)) {
            if (aa && !Game.hits_air(ally.type())) continue;
            if (!aa && !Game.hits_ground(ally.type())) continue;
            if (Game.is_spellcaster(ally.type())) continue;
            if (ally.type() == Units.ZERG_QUEEN && Queen.get_base(ally) != null && p.distance(Queen.get_base(ally).location) > 15 && !lib) continue;
            if (!Game.on_creep(p) && Game.pathable(p) && ally.type() == Units.ZERG_QUEEN) continue;
            if (!Game.is_structure(ally.type()) && Game.is_combat(ally.type())) {
                if (!used.contains(ally.tag())) {
                    if (best == null ||
                            (best.distance(p) / Game.get_unit_type_data().get(best.type()).getMovementSpeed().orElse((float) 1)) >
                                    (ally.distance(p)) / Game.get_unit_type_data().get(ally.type()).getMovementSpeed().orElse((float) 1)) {
                        best = ally;
                    }
                }
            }
        }
        if (best != null) {
            UnitRoleManager.add(best, UnitRole.DEFENDER);
            used.add(best.tag());
        }
        return best;
    }

    private static AbsintheUnit closest_free_aggressor(Vector2d p) {
        AbsintheUnit best = null;
        for (AbsintheUnit ally : UnitRoleManager.get(UnitRole.ARMY)) {
            if (Game.is_spellcaster(ally.type())) continue;
            if (ally.type() == Units.ZERG_QUEEN) continue;
            if (ally.flying()) continue;
            if (!Game.is_structure(ally.type()) && Game.is_combat(ally.type())) {
                if (!used.contains(ally.tag())) {
                    if (best == null ||
                            (best.distance(p) / Game.get_unit_type_data().get(best.type()).getMovementSpeed().orElse((float) 1)) >
                                    (ally.distance(p)) / Game.get_unit_type_data().get(ally.type()).getMovementSpeed().orElse((float) 1)) {
                        best = ally;
                    }
                }
            }
        }
        if (best != null) {
            UnitRoleManager.add(best, UnitRoleManager.UnitRole.DEFENDER);
            used.add(best.tag());
        }
        return best;
    }

    public static boolean has_defense_point() {
        return defense_point != null;
    }

    public static Vector2d defense_point() {
        return defense_point;
    }

    private static Vector2d average_point_zergling(List<AbsintheUnit> l, Vector2d target_center) {
        float x = 0;
        float y = 0;
        int n = 0;
        for (AbsintheUnit u : l) {
            if (u.type() == Units.ZERG_ZERGLING) {
                if (u.distance(target_center) < 5) {
                    x += u.location().getX();
                    y += u.location().getY();
                    n++;
                }
            }
        }
        return Vector2d.of(x / n, y / n);
    }
}
