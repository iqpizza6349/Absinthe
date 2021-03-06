package com.tistory.workshop6349.economy;

import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.tistory.workshop6349.Vector2d;
import com.tistory.workshop6349.army.EnemySquadManager;
import com.tistory.workshop6349.army.ThreatManager;
import com.tistory.workshop6349.game.AbsintheUnit;
import com.tistory.workshop6349.game.Game;
import com.tistory.workshop6349.game.GameInfoCache;
import com.tistory.workshop6349.game.RaceInterface;
import com.tistory.workshop6349.knowledge.Scouting;
import com.tistory.workshop6349.unitcontrollers.Worker;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class BaseManager {

    // the index of bases must never change
    public static final ArrayList<Base> bases = new ArrayList<>();
    private static final Map<Pair<Integer, Integer>, Float> distances = new HashMap<>();

    public static void start_game() {
        bases.clear();

        for (Vector2d p : Game.expansions()) {
            bases.add(new Base(p));
        }

        AbsintheUnit main = GameInfoCache.get_units(Alliance.SELF, RaceInterface.get_race_command_structure()).get(0);

        bases.add(new Base(main.location()));

        for (Base base : bases) {
            if (main.distance(base) < 10) {
                base.location = main.location();
            }
        }


        for (int i = 0; i < bases.size(); i++) {
            for (int j = 0; j < bases.size(); j++) {
                Vector2d first = bases.get(i).location;
                Vector2d second = bases.get(j).location;
                float dist = Game.pathing_distance(first, second);
                if (i != j) {
                    //offset positions to account for a structure blocking the position
                    int offsetX = 1;
                    while (Math.abs(dist) < 0.1 && offsetX <= 5) {
                        Vector2d offsetFirst = Vector2d.of(first.getX() + offsetX, first.getY());
                        Vector2d offsetSecond = Vector2d.of(second.getX() + offsetX, second.getY());
                        dist = Game.pathing_distance(offsetFirst, offsetSecond);
                        offsetX++;
                    }

                    //base is unreachable (eg island)
                    if (Math.abs(dist) < 0.1) {
                        dist = 1000 + (float)first.distance(second);
                    }
                }
                distances.put(new ImmutablePair<>(i, j), dist);
                distances.put(new ImmutablePair<>(j, i), dist);
            }
        }

        main.use_ability(Abilities.RALLY_HATCHERY_WORKERS, main);
    }

    public static void on_unit_created(AbsintheUnit u) {
        if (u.type()== Units.ZERG_QUEEN) {
            if (inject_queen_count() > 3) return;
            Base best = null;
            for (Base base: bases) {
                if (!base.has_queen() && base.has_friendly_command_structure()) {
                    if (base.command_structure.done()) {
                        if (best == null || u.distance(best) > u.distance(base)) {
                            best = base;
                        }
                    }
                }
            }
            if (best == null) {
                for (Base base: bases) {
                    if (!base.has_queen() && base.has_friendly_command_structure()) {
                        if (best == null || u.distance(best) > u.distance(base)) {
                            best = base;
                        }
                    }
                }
            }
            if (best != null) best.set_queen(u);
        }
    }

    public static void on_frame() {
        for (AbsintheUnit unit: GameInfoCache.get_units()) {
            if (unit.is_command()) {
                for (Base base: bases) {
                    if (unit.distance(base) < 5 && !unit.flying()) {
                        Game.draw_line(base.location, unit.location(), Color.RED);
                        base.set_command_structure(unit);
                        base.set_walking_drone(null);
                    }
                }
            }
        }
        for (Base b: bases) {
            b.update();
            if (b.has_walking_drone() && b.walking_drone.distance(b.location) > 4 && (b.walking_drone.ability() != Game.production_ability(RaceInterface.get_race_command_structure()))) {
                b.walking_drone.move(b.location);
            }
            if (b.has_walking_drone() && !b.walking_drone.alive()) b.walking_drone = null;
        }
        for (AbsintheUnit ling : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_ZERGLING_BURROWED)) {
            for (AbsintheUnit drone: GameInfoCache.get_units(Alliance.SELF, Units.ZERG_DRONE)) {
                if (drone.distance(ling) < 10) {
                    ling.use_ability(Abilities.BURROW_UP);
                }
            }
        }
    }

    static boolean is_walking_drone(AbsintheUnit u) {
        for (Base b: bases) {
            if (b.walking_drone == u) {
                return true;
            }
        }
        return false;
    }

    public static Base main_base = null;
    public static Base main_base() {
        if (main_base == null || !main_base.has_friendly_command_structure()) {
            Base best = null;
            for (Base b: bases) {
                if (b.has_friendly_command_structure() && b.command_structure.done()) {
                    if (best == null || best.location.distance(Scouting.closest_enemy_spawn(best.location)) < b.location.distance(Scouting.closest_enemy_spawn(b.location))) {
                        best = b;
                    }
                }
            }
            if (best == null) best = bases.get(0);
            main_base = best;
        }

        return main_base;
    }

    private static float get_distance(Base b1, Base b2) {
        return distances.get(new ImmutablePair<>(bases.indexOf(b1), bases.indexOf(b2)));
    }

    private static long next_base_frame = -1;
    private static Base next_base = null;
    public static Base get_next_base() {
        if (next_base_frame != Game.get_frame()) {
            Base best = null;
            double best_dist = 9999;
            for (Base b: bases) {
                if (b.has_command_structure()) continue;
                if (!ThreatManager.is_safe(b.location)) continue;
                if (best == null || (get_distance(main_base(), b) - get_distance(closest_base(Scouting.closest_enemy_spawn()), b)) < best_dist) {
                    best = b;
                    best_dist = (get_distance(main_base(), b) - get_distance(closest_base(Scouting.closest_enemy_spawn()), b));
                }
            }
            next_base = best;
            next_base_frame = Game.get_frame();
        }
        return next_base;
    }

    private static int inject_queen_count() {
        int result = 0;
        for (Base b : bases) {
            if (b.has_friendly_command_structure() && b.has_queen()) result++;
        }
        return result;
    }

    public static int base_count() {
        int result = 0;
        for (Base b: bases) {
            if (b.has_command_structure() && b.has_friendly_command_structure()) result++;
        }
        return result;
    }

    public static Vector2d get_placement_location(UnitType structure, Vector2d base, int min_dist, int max_dist) {
        Vector2d result = Vector2d.of(0, 0);
        int limit = 0;
        while (!Game.can_place(Game.get_unit_type_data().get(structure).getAbility().orElse(Abilities.INVALID), result) || base.distance(result) < min_dist) {
            float rx = (float) Math.random() * 2 - 1;
            float ry = (float) Math.random() * 2 - 1;
            result = Vector2d.of(base.getX() + rx * max_dist, base.getY() + ry * max_dist);
            if (++limit == 100) break;
        }
        return result;
    }

    public static void build(UnitType structure) {
        if (Game.is_town_hall(structure)) {
            if (get_next_base().has_walking_drone()) {
                if (get_next_base().walking_drone.ability() != Game.production_ability(RaceInterface.get_race_command_structure())) {
                    get_next_base().walking_drone.use_ability(Game.production_ability(structure), get_next_base().location);
                }
            }
            else {
                AbsintheUnit worker = get_free_worker(get_next_base().location);
                if (worker != null) {
                    worker.use_ability(Game.production_ability(RaceInterface.get_race_command_structure()), get_next_base().location);
                }
            }
        }
        else if (Game.is_gas_structure(structure)) {
            // try to build at safe bases first
            for (Base b: BaseManager.bases) {
                if (ThreatManager.is_safe(b.location)) {
                    if (b.has_friendly_command_structure() && b.command_structure.done()) {
                        for (AbsintheUnit gas: b.gases) {
                            if (GameInfoCache.geyser_is_free(gas)) {
                                AbsintheUnit worker = get_free_worker(gas.location());
                                if (worker != null) {
                                    worker.use_ability(Game.production_ability(RaceInterface.get_race_gas()), gas);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            for (Base b: BaseManager.bases) {
                if (b.has_friendly_command_structure() && b.command_structure.done()) {
                    for (AbsintheUnit gas: b.gases) {
                        if (GameInfoCache.geyser_is_free(gas)) {
                            AbsintheUnit worker = get_free_worker(get_next_base().location);
                            if (worker != null) {
                                worker.use_ability(Abilities.BUILD_EXTRACTOR, gas);
                                return;
                            }
                        }
                    }
                }
            }
        }
        else if (structure == Units.ZERG_SPINE_CRAWLER) {
            Base nat = get_natural();
            Vector2d location;
            if (nat == null) {
                location = get_spine_placement_location(get_forward_base());
            } else {
                location = get_spine_placement_location(nat);
            }

            AbsintheUnit worker = get_free_worker(location);
            if (worker != null) {
                worker.use_ability(Abilities.BUILD_SPINE_CRAWLER, location);
                return;
            }
        }
        else {
            Vector2d location = get_placement_location(structure, main_base().location, 6, 15);
            AbsintheUnit worker = get_free_worker(location);
            if (worker != null) {
                worker.use_ability(Game.production_ability(structure), location);
                return;
            }
        }
    }

    public static AbsintheUnit get_free_worker(Vector2d location) {
        AbsintheUnit best = null;
        unitloop: for (AbsintheUnit unit : GameInfoCache.get_units(Alliance.SELF, RaceInterface.get_race_worker())) {
            for (Base b: bases) {
                if (b.walking_drone == unit) continue unitloop;
            }
            if (Worker.can_build(unit)) {
                if (best == null || unit.distance(location) < best.distance(location)) {
                    best = unit;
                }
            }
        }
        return best;
    }

    public static int active_gases() {
        int total = 0;
        for (AbsintheUnit unit: GameInfoCache.get_units(Alliance.SELF, RaceInterface.get_race_gas())) {
            if (unit.gas() > 0) {
                total++;
            }
        }
        return total;
    }

    private static final Map<Integer, Vector2d> get_numbers = new HashMap<>();
    public static Vector2d get_base(int n) {
        if (!get_numbers.containsKey(n)) {
            ArrayList<Vector2d> found = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                Base best = null;
                for (Base b: bases) {
                    if (best == null|| (get_distance(main_base(), b) - get_distance(closest_base(Scouting.closest_enemy_spawn()), b)) < (get_distance(main_base(), best) - (get_distance(closest_base(Scouting.closest_enemy_spawn()), best)))) {
                        if (!found.contains(b.location)) {
                            best = b;
                        }
                    }
                }
                found.add(best.location);
                if (found.size() >= n) break;
            }
            get_numbers.put(n, found.get(found.size() - 1));
        }
        return get_numbers.get(n);
    }

    public static Base closest_base(Vector2d p) {
        Base best = null;
        for (Base b: bases) {
            if (best == null || p.distance(best.location) > b.location.distance(p)) {
                best = b;
            }
        }
        return best;
    }

    public static Base closest_friendly_base(Vector2d p) {
        Base best = null;
        for (Base b: bases) {
            if (b.has_friendly_command_structure()) {
                if (best == null || p.distance(best.location) > b.location.distance(p)) {
                    best = b;
                }
            }
        }
        return best;
    }

    private static Vector2d get_spine_placement_location(Base b) {
        Vector2d target = Scouting.closest_enemy_spawn();
        target = Vector2d.of(target.getX() + 4, target.getY());
        Vector2d result = null;
        for (int i = 0; i < 200; i++) {
            double rx = Math.random() * 2 - 1;
            double ry = Math.random() * 2 - 1;
            Vector2d test = Vector2d.of((float) (b.location.getX() + rx * 10), (float) (b.location.getY() + ry * 10));
            if (Game.can_place(Abilities.MORPH_SPINE_CRAWLER_ROOT, test)) {
                if (result == null || Game.pathing_distance(result,  target) > Game.pathing_distance(test, target)) {
                    result = test;
                }
            }
        }
        return result;
    }

    private static long forward_base_frame = -1;
    private static Base forward_base = null;
    public static Base get_forward_base() {
        if (forward_base_frame != Game.get_frame()) {
            Base best = null;
            Vector2d target = closest_base(Scouting.closest_enemy_spawn()).location;
            int best_size = 0;
            for (Set<AbsintheUnit> squad : EnemySquadManager.enemy_squads) {
                if (squad.size() > best_size) {
                    target = EnemySquadManager.average_point(new ArrayList<>(squad));
                    best_size = squad.size();
                }
            }
            for (Base b: bases) {
                if (b.has_friendly_command_structure() && b.command_structure.done()) {
                    if (best == null || get_distance(b, closest_base(target)) < get_distance(best, closest_base(target))) {
                        best = b;
                    }
                }
            }

            if (best == null) best = bases.get(0);
            forward_base = best;
            forward_base_frame = Game.get_frame();
        }
        return forward_base;
    }

    public static Base closest_occupied_base(Vector2d p) {
        Base best = null;
        for (Base b : bases) {
            if (b.has_command_structure()) {
                if (best == null || b.location.distance(p) < best.location.distance(p)) {
                    best = b;
                }
            }
        }
        return best;
    }

    private static Vector2d get_spore_placement_location(Base b) {
        float x = 0;
        float y = 0;
        int total = 0;
        for (AbsintheUnit min: GameInfoCache.get_units(Alliance.NEUTRAL)) {
            if (min.minerals() > 0 && min.distance(b.location) < 8) {
                x += min.location().getX();
                y += min.location().getY();
                total++;
            }
        }
        x /= total;
        y /= total;
        for (AbsintheUnit spore : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_SPORE_CRAWLER)) {
            if (spore.distance(Vector2d.of(x, y)) < 4) {
                return null;
            }
        }
        float new_x = b.location.getX() - x;
        float new_y = b.location.getY() - y;
        Vector2d offset = new Vector2d(new_x, new_y).normalized();
        for (int i = 0; i < 20; i++) {
            Vector2d p = Vector2d.of((float) (x + (2.5 + 0.1 * i) * offset.getX()), (float) (y + (2.5 * 0.1 * i) * offset.getY()));
            if (Game.can_place(Abilities.MORPH_SPORE_CRAWLER_ROOT, p)) {
                return p;
            }
        }
        return null;
    }

    private static Vector2d get_spore_placement(Base b, Vector2d slider) {

        for (AbsintheUnit spore : GameInfoCache.get_units(Alliance.SELF, Units.ZERG_SPORE_CRAWLER)) {
            if (spore.distance(slider) < 3) {
                return null;
            }
        }

        Vector2d offset = b.location.directionTo(slider);
        for (int i = 0; i < 20; i++) {
            Vector2d p = Vector2d.of((float) (slider.getX() - (2.5 + 0.1 * i) * offset.getX()), (float) (slider.getY() - (2.5 * 0.1 * i) * offset.getY()));
            if (Game.can_place(Abilities.MORPH_SPORE_CRAWLER_ROOT, p)) {
                return p;
            }
        }
        return null;
    }

    public static void build_defensive_spores() {
        for (Base b: bases) {
            if (Game.minerals() < 75) {
                Game.spend(75, 0);
                return;
            }
            if (b.has_friendly_command_structure() && b.command_structure.done()) {
                Vector2d spore = get_spore_placement_location(b);
                if (spore != null) {
                    AbsintheUnit worker = get_free_worker(spore);
                    if (worker != null) {
                        worker.use_ability(Abilities.BUILD_SPORE_CRAWLER, spore);
                        Game.spend(75, 0);
                        return;
                    }
                }
            }
        }
    }

    public static void build_triangle_spores() {
        for (Base b: bases) {
            if (Game.minerals() < 75) {
                return;
            }
            if (b.has_friendly_command_structure() && b.command_structure.done()) {
                Vector2d[] spore = get_spore_triangle_placement_locations(b);
                for (Vector2d p : spore) {
                    if (p != null) {
                        AbsintheUnit worker = get_free_worker(p);
                        if (worker != null) {
                            worker.use_ability(Abilities.BUILD_SPORE_CRAWLER, p);
                            Game.spend(75, 0);
                            return;
                        }
                    }
                }
            }
        }
    }

    private static Vector2d[] get_spore_triangle_placement_locations(Base b) {

        Vector2d[] results = new Vector2d[2];

        AbsintheUnit first = null;
        AbsintheUnit second = null;
        double best = -1;
        List<AbsintheUnit> resources = new ArrayList<>();
        resources.addAll(b.minerals);
        resources.addAll(b.gases);
        for (AbsintheUnit res1 : resources) {
            for (AbsintheUnit res2: resources) {
                if (res1.distance(res2) > best) {
                    first = res1;
                    second = res2;
                    best = res1.distance(res2);
                }
            }
        }
        results[0] = get_spore_placement(b, first.location());
        results[1] = get_spore_placement(b, second.location());

        return results;
    }


    /*
     * Our natural base is the closest base to our main, that isn't our main, that is also closer to our opponent
     */
    public static Base get_natural() {
        Base best = null;
        for (Base b : bases) {
            if (b.has_friendly_command_structure() && b != main_base()) {
                if (get_distance(b, closest_base(Scouting.closest_enemy_spawn())) < get_distance(main_base(), closest_base(Scouting.closest_enemy_spawn()))) {
                    if (best == null || get_distance(main_base, best) > get_distance(main_base, b)) {
                        best = b;
                    }
                }
            }
        }
        return best;
    }

}
