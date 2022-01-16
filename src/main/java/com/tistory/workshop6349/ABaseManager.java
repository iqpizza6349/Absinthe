package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.tistory.workshop6349.unitcontrollers.Probe;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class ABaseManager {

    // the index of bases must never change
    public static ArrayList<ABase> bases = new ArrayList<>();
    static ArrayList<Point2d> expos = new ArrayList<>();
    private static final Map<Pair<Integer, Integer>, Float> distances = new HashMap<>();

    static void startGame() {
        bases.clear();
        calculateExpansions();

        for (Point2d p : expos) {
            AGame.drawBox(p, Color.GREEN);
        }
        AGame.debug.sendDebug();

        for (Point2d e: expos) bases.add(new ABase(e));
        UnitInPool main = AGameInfoCache.getUnits(Alliance.SELF, Units.PROTOSS_NEXUS).get(0);
        // Fix the placement for our main ABase
        for (ABase b : bases) {
            if (b.location.distance(main.unit().getPosition().toPoint2d()) < 10) {
                b.location = main.unit().getPosition().toPoint2d();
            }
        }
        for (int i = 0; i < bases.size(); i++) {
            for (int j = 0; j < bases.size(); j++) {
                Point2d first = bases.get(i).location;
                Point2d second = bases.get(j).location;
                float dist = AGame.pathingDistance(first, second);
                if (i != j) {
                    while (Math.abs(dist) < 0.1) {
                        first = Point2d.of(first.getX() + 1, first.getY());
                        second = Point2d.of(second.getX() + 1, second.getY());
                        dist = AGame.pathingDistance(first, second);
                    }
                }
                distances.put(new ImmutablePair<>(i, j), dist);
                distances.put(new ImmutablePair<>(j, i), dist);
            }
        }
//        onUnitCreated(main);
        AGame.unitCommand(main, Abilities.RALLY_NEXUS, main.unit());
    }

    // TODO onUnitCreated()은 나중에 개발

    static void onFrame() {
        for (UnitInPool u: AGame.getUnits()) {
            if (AGame.isTownHall(u.unit().getType())) {
                for (ABase b: bases) {
                    if (b.location.distance(u.unit().getPosition().toPoint2d()) < 5) {
                        b.setCommandStructure(u);
                    }
                }
            }
        }
        for (ABase b: bases) {
            b.update();
            if (b.hasWalkingWorker()
                    && b.walkingWorker.unit().getPosition().toPoint2d().distance(b.location) > 4
                    && (b.walkingWorker.unit().getOrders().size() == 0
                    || b.walkingWorker.unit().getOrders().get(0).getAbility() != Abilities.BUILD_NEXUS)) {
                AGame.unitCommand(b.walkingWorker, Abilities.MOVE, b.location);
            }
            if (b.hasWalkingWorker() && !b.walkingWorker.isAlive()) {
                b.walkingWorker = null;
            }
        }
    }

    static boolean isWalkingProbe(UnitInPool u) {
        for (ABase b: bases) {
            if (b.walkingWorker == u) {
                return true;
            }
        }
        return false;
    }

    public static long mainBaseFrame = -1;
    public static ABase mainBase = null;
    public static ABase mainBase() {
        if (mainBaseFrame != AGame.getFrame()) {
            mainBaseFrame = AGame.getFrame();
            ABase best = null;
            for (ABase b: bases) {
                if (b.hasFriendlyCommandStructure() && b.commandStructure.unit().getBuildProgress() > 0.999) {
                    if (best == null || best.location.distance(AScouting.closestEnemySpawn(best.location)) < b.location.distance(AScouting.closestEnemySpawn(b.location))) {
                        best = b;
                    }
                }
            }
            if (best == null) {
                best = bases.get(0);
            }
            mainBase = best;
        }
        return mainBase;
    }

    public static float getDistance(ABase b1, ABase b2) {
        return distances.get(new ImmutablePair<>(bases.indexOf(b1), bases.indexOf(b2)));
    }

    public static long nextBaseFrame = -1;
    public static ABase nextBase = null;
    public static ABase getNextBase() {
        if (nextBaseFrame != AGame.getFrame()) {
            ABase best = null;
            double best_dist = 9999;
            for (ABase b: bases) {
                if (b.hasFriendlyCommandStructure()) continue;
                if (best == null || (getDistance(mainBase(), b) - getDistance(closestBase(AScouting.closestEnemySpawn()), b)) < best_dist) {
                    best = b;
                    best_dist = (getDistance(mainBase(), b) - getDistance(closestBase(AScouting.closestEnemySpawn()), b));
                }
            }
            nextBase = best;
            nextBaseFrame = AGame.getFrame();
        }
        return nextBase;
    }

    public static int baseCount(Alliance a) {
        int result = 0;
        for (ABase b: bases) {
            if (b.hasCommandStructure() && b.commandStructure.unit().getAlliance() == a) {
                result++;
            }
        }
        return result;
    }

    static Point2d getPlacementLocation(UnitType structure, Point2d ABase, int minDist, int maxDist) {
        Point2d result = Point2d.of(0, 0);
        int limit = 0;
        while (!AGame.canPlace(AGame.getUnitTypeData().get(structure).getAbility().orElse(Abilities.INVALID), result)
                || ABase.distance(result) < minDist) {
            float rx = (float) Math.random() * 2 - 1;
            float ry = (float) Math.random() * 2 - 1;
            result = Point2d.of(ABase.getX() + rx * maxDist, ABase.getY() + ry * maxDist);
            if (++limit == 500) {
                break;
            }
        }
        return result;
    }

    static void build(UnitType structure) {
        if (AGame.isTownHall(structure)) {
            if (getNextBase().hasWalkingWorker()) {
                if (getNextBase().walkingWorker.unit().getOrders().size() == 0
                        || getNextBase().walkingWorker.unit().getOrders().get(0).getAbility() != Abilities.BUILD_NEXUS) {
                    AGame.unitCommand(getNextBase().walkingWorker, AGame.getUnitTypeData().get(structure).getAbility().orElse(Abilities.INVALID), getNextBase().location);
                }
            }
            else {
                UnitInPool worker = getFreeWorker(getNextBase().location);
                if (worker != null) {
                    AGame.unitCommand(worker, Abilities.BUILD_NEXUS, getNextBase().location);
                    return;
                }
            }
        }
        else if (structure == Units.PROTOSS_ASSIMILATOR) {
            // try to build at safe bases first
            for (ABase b: ABaseManager.bases) {
                if (AThreatManager.isSafe(b.location)) {
                    if (b.hasFriendlyCommandStructure() && b.commandStructure.unit().getBuildProgress() > AConstants.DONE) {
                        for (UnitInPool gas: b.gases) {
                            if (AGameInfoCache.geyserIsFree(gas)) {
                                UnitInPool worker = getFreeWorker(getNextBase().location);
                                if (worker != null) {
                                    AGame.unitCommand(worker, Abilities.BUILD_ASSIMILATOR, gas.unit());
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            for (ABase b: ABaseManager.bases) {
                if (b.hasFriendlyCommandStructure() && b.commandStructure.unit().getBuildProgress() > AConstants.DONE) {
                    for (UnitInPool gas: b.gases) {
                        if (AGameInfoCache.geyserIsFree(gas)) {
                            UnitInPool worker = getFreeWorker(getNextBase().location);
                            if (worker != null) {
                                AGame.unitCommand(worker, Abilities.BUILD_ASSIMILATOR, gas.unit());
                                return;
                            }
                        }
                    }
                }
            }
        }
        else if (structure == Units.PROTOSS_PHOTON_CANNON) {
            Point2d location = getCannonPlacementLocation(getForwardBase());
            UnitInPool worker = getFreeWorker(location);
            if (worker != null) {
                AGame.unitCommand(worker, Abilities.BUILD_PHOTON_CANNON, location);
                return;
            }
        }
        else {
            Point2d location = getPlacementLocation(structure, mainBase().location, 6, 15);
            UnitInPool worker = getFreeWorker(location);
            if (worker != null) {
                AGame.unitCommand(worker, AGame.getUnitTypeData().get(structure).getAbility().get(), location);
                return;
            }
        }
    }

    public static UnitInPool getFreeWorker(Point2d location) {
        UnitInPool best = null;
        unitloop: for (UnitInPool u : AGameInfoCache.getUnits(Alliance.SELF, Units.PROTOSS_PROBE)) {
            for (ABase b: bases) {
                if (b.walkingWorker == u) {
                    continue unitloop;
                }
            }
            if (Probe.canBuild(u)) {
                if (best == null || location.distance(u.unit().getPosition().toPoint2d()) < location.distance(best.unit().getPosition().toPoint2d())) {
                    best = u;
                }
            }
        }
        return best;
    }

    static int activeAssimilator() {
        int total = 0;
        for (UnitInPool u : AGameInfoCache.getUnits(Alliance.SELF, Units.PROTOSS_ASSIMILATOR)) {
            if (u.unit().getVespeneContents().orElse(0) > 0) {
                total++;
            }
        }
        return total;
    }

    private static final Map<Integer, Point2d> getNumbers = new HashMap<>();
    public static Point2d getBase(int n) {
        if (!getNumbers.containsKey(n)) {
            ArrayList<Point2d> found = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                ABase best = null;
                for (ABase b: bases) {
                    if (best == null
                            || (getDistance(mainBase(), b) - getDistance(closestBase(AScouting.closestEnemySpawn()), b))
                            < (getDistance(mainBase(), best) - (getDistance(closestBase(AScouting.closestEnemySpawn()), best)))) {
                        if (!found.contains(b.location)) {
                            best = b;
                        }
                    }
                }
                found.add(best.location);
                if (found.size() >= n) {
                    break;
                }
            }
            getNumbers.put(n, found.get(found.size() - 1));
        }
        return getNumbers.get(n);
    }

    static void buildDefensiveAirCannon() {
        outer: for (ABase b: bases) {
            if (AGame.minerals() < 75) {
                return;
            }
            if (b.hasFriendlyCommandStructure() && !(b.commandStructure.unit().getBuildProgress() < .999)) {
                for (UnitInPool spore: AGameInfoCache.getUnits(Alliance.SELF, Units.PROTOSS_PHOTON_CANNON)) {
                    if (spore.unit().getPosition().toPoint2d().distance(b.location) <= 9) {
                        continue outer;
                    }
                }
                Point2d spore = getAirCannonPlacementLocation(b);
                if (spore.distance(Point2d.of(0, 0)) < 5) {
                    continue outer;
                }
                UnitInPool worker = getFreeWorker(spore);
                if (worker != null) {
                    AGame.unitCommand(worker, Abilities.BUILD_PHOTON_CANNON, spore);
                    AGame.spend(75, 0);
                    return;
                }
            }
        }
    }

    static ABase closestBase(Point2d p) {
        ABase best = null;
        for (ABase b: bases) {
            if (best == null || p.distance(best.location) > b.location.distance(p)) {
                best = b;
            }
        }
        return best;
    }

    static Point2d getCannonPlacementLocation(ABase b) {
        Point2d target = AScouting.closestEnemySpawn();
        target = Point2d.of(target.getX() + 4, target.getY());
        Point2d result = null;
        for (int i = 0; i < 200; i++) {
            double rx = Math.random() * 2 - 1;
            double ry = Math.random() * 2 - 1;
            Point2d test = Point2d.of((float) (b.location.getX() + rx * 10), (float) (b.location.getY() + ry * 10));
            if (AGame.canPlace(Abilities.BUILD_PHOTON_CANNON, test)) {
                if (result == null || AGame.pathingDistance(result,  target) > AGame.pathingDistance(test, target)) {
                    result = test;
                }
            }
        }
        return result;
    }

    public static long forward_base_frame = -1;
    public static ABase forward_base = null;
    public static ABase getForwardBase() {
        if (forward_base_frame != AGame.getFrame()) {
            ABase best = null;
            ABase target = closestBase(AScouting.closestEnemySpawn());
            for (ABase b: bases) {
                if (b.hasFriendlyCommandStructure() && !(b.commandStructure.unit().getBuildProgress() < 0.999)) {
                    if (best == null || getDistance(b, target) < getDistance(best, target)) {
                        best = b;
                    }
                }
            }

            if (best == null) best = bases.get(0);
            AGame.drawBox(best.location, Color.GREEN);
            forward_base = best;
            forward_base_frame = AGame.getFrame();
        }
        return forward_base;
    }

    static Point2d getAirCannonPlacementLocation(ABase b) {
        float x = 0;
        float y = 0;
        int total = 0;
        for (UnitInPool min: AGameInfoCache.getUnits(Alliance.NEUTRAL)) {
            if (min.unit().getMineralContents() .orElse(0)> 0 && min.unit().getPosition().toPoint2d().distance(b.location) < 8) {
                x += min.unit().getPosition().getX();
                y += min.unit().getPosition().getY();
                total++;
            }
        }
        x /= total;
        y /= total;
        x = b.location.getX() - x;
        y = b.location.getY() - y;
        Vector2d offset = Utilities.normalize(new Vector2d(x, y));
        for (int i = 0; i < 20; i++) {
            Point2d p = Point2d.of((float) (b.location.getX() - (2.5 + 0.1 * i) * offset.x), (float) (b.location.getY() - (2.5 * 0.1 * i) * offset.y));
            if (AGame.canPlace(Abilities.BUILD_PHOTON_CANNON, p)) {
                return p;
            }
        }
        return Point2d.of(0, 0);
    }

    static void calculateExpansions() {
        expos.clear();
        ArrayList<Set<UnitInPool>> mineral_lines = new ArrayList<>();
        outer: for (UnitInPool unit: AGameInfoCache.getUnits(Alliance.NEUTRAL)) {
            if (unit.unit().getType().toString().toLowerCase().contains("mineral") || unit.unit().getType().toString().toLowerCase().contains("geyser")) {
                for (Set<UnitInPool> lines : mineral_lines) {
                    for (UnitInPool patch : lines) {
                        if (patch.unit().getPosition().distance(unit.unit().getPosition()) < 16) {
                            lines.add(unit);
                            continue outer;
                        }
                    }
                }
                Set<UnitInPool> adder = new HashSet<>();
                adder.add(unit);
                mineral_lines.add(adder);
            }
        }
        for (Set<UnitInPool> line : mineral_lines) {
            float x = 0;
            float y = 0;
            int count = 0;
            for (UnitInPool patch : new ArrayList<>(line)) {
                x += patch.unit().getPosition().getX();
                y += patch.unit().getPosition().getY();
                count++;
            }
            x = x/count;
            y = y/count;
            Vector2d average = new Vector2d(x, y);

            Point2d best = null;

            List<Point2d> points = new ArrayList<>();
            for (int x_offset = -10; x_offset < 11; x_offset++) {
                for (int y_offset = -10; y_offset < 11; y_offset++) {
                    Point2d current = Point2d.of((float) (average.x + x_offset), (float) (average.y + y_offset));
                    points.add(current);
                }
            }
            List<Boolean> results = AGame.canPlace(Abilities.BUILD_NEXUS, points);
            for (int x_offset = -10; x_offset < 11; x_offset++) {
                for (int y_offset = -10; y_offset < 11; y_offset++) {
                    Point2d current = Point2d.of((float) (average.x + x_offset), (float) (average.y + y_offset));
                    if (best == null || average.toPoint2d().distance(current) < average.toPoint2d().distance(best)) {
                        if (results.get((x_offset + 10) * 21 + (y_offset + 10))) {
                            best = current;
                        }
                    }
                }
            }

            if (best != null) {
                expos.add(best);
            }
        }
    }

    public static boolean needsExpand() {
        int patches = 0;
        int gases = ABaseManager.activeAssimilator();
        for (ABase b : ABaseManager.bases) {
            if (b.hasFriendlyCommandStructure()) {
                patches += b.minerals.size();
            }
        }
        return AGameInfoCache.countFriendly(Units.PROTOSS_PROBE) > (3 * gases + 2 * patches);
    }

}
