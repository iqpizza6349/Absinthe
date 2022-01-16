package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.tistory.workshop6349.unitcontrollers.Probe;

import java.util.ArrayList;

public class AScouting {

    public static UnitInPool scout = null;
    public static UnitInPool patrolScout = null;
    public static ArrayList<Point2d> spawns = new ArrayList<>();
    public static int patrolBase = 2;

    public static boolean hasPulledBack = false;


    public static void startGame() {

    }

    public static void startFrame() {

    }
    public static void onFrame() {
        if (spawns.size() == 0) {
            AGame.getGameInfo().getStartRaw().ifPresent(StartRaw -> spawns = new ArrayList<>(StartRaw.getStartLocations()));
        }
        if (spawns.size() > 1) {
            for (UnitInPool u: AGameInfoCache.getUnits(Alliance.ENEMY)) {
                if (AGame.isStructure(u.unit().getType())) {
                    Point2d spawn = closestEnemySpawn(u.unit().getPosition().toPoint2d());
                    spawns = new ArrayList<>();
                    spawns.add(spawn);
                    break;
                }
            }
        }
        if (spawns.size() > 1) {
            outer: for (UnitInPool u: AGameInfoCache.getUnits(Alliance.SELF)) {
                for (Point2d s: spawns) {
                    if (s.distance(u.unit().getPosition().toPoint2d()) < 8) {
                        spawns.remove(s);
                        break outer;
                    }
                }
            }
        }
        if (scout == null && AGameInfoCache.countFriendly(Units.PROTOSS_PROBE) > 16 && ABuild.scout) {
            assignScout();
        }
        if (scout == null && AGameInfoCache.countFriendly(Units.PROTOSS_PROBE) > 12 && spawns.size() >= 3 && ABuild.scout) {
            assignScout();
        }
        if (AWisdom.confused() && AGame.armySupply() < 20 && patrolBase < 7) {
            if (patrolScout == null) {
                assignPatrolScout();
            }
        } else {
            if (patrolScout != null) {
                if (patrolScout.isAlive()) {
                    AGame.unitCommand(patrolScout, Abilities.STOP);
                }
                patrolScout = null;
            }
        }

        if (scout != null && scout.isAlive()) {
            if (scout.unit().getOrders().size() == 0 || scout.unit().getOrders().get(0).getAbility() != Abilities.MOVE) {
                AGame.unitCommand(scout, Abilities.MOVE, ABaseManager.getPlacementLocation(Units.PROTOSS_PYLON, closestEnemySpawn(scout.unit().getPosition().toPoint2d()), 5, 15));
            }
        }

        if (patrolScout != null && patrolScout.isAlive()) {
            Point2d target = ABaseManager.getBase(patrolBase);
            if (target.distance(patrolScout.unit().getPosition().toPoint2d()) < 4) {
                patrolBase++;
            } else if (patrolScout.unit().getOrders().size() == 0 || patrolScout.unit().getOrders().get(0).getAbility() != Abilities.MOVE) {
                AGame.unitCommand(patrolScout, Abilities.MOVE, target);
            }
        }

        if (!hasPulledBack) {
            if (AWisdom.proxyDetected() || AWisdom.allInDetected() || AWisdom.airDetected()) {
                hasPulledBack = true;
                for (UnitInPool overlord: AGameInfoCache.getUnits(Alliance.SELF, Units.ZERG_OVERLORD)) {
                    AGame.unitCommand(overlord, Abilities.MOVE, ABaseManager.mainBase().location);
                }
            }
        }

    }
    public static void endFrame() {

    }

    public static Point2d closestEnemySpawn(Point2d s) {
        Point2d best = null;
        for (Point2d p: spawns) {
            if (best == null || s.distance(p) < s.distance(best)) {
                best = p;
            }
        }
        return best;
    }

    private static Point2d closest_enemy_spawn = null;
    private static int closestEnemySpawnFrame = -1;
    public static Point2d closestEnemySpawn() {
        if (AGame.getFrame() != closestEnemySpawnFrame) {
            closestEnemySpawnFrame = (int) AGame.getFrame();
            if (ABaseManager.mainBase() != null) {
                closest_enemy_spawn = closestEnemySpawn(ABaseManager.mainBase().location);
            }
            closest_enemy_spawn = closestEnemySpawn(Point2d.of(0, 0));
        }
        return closest_enemy_spawn;
    }

    public static void assignScout() {
        for (UnitInPool unit: AGameInfoCache.getUnits(Alliance.SELF, Units.PROTOSS_PROBE)) {
            if (Probe.canBuild(unit)) {
                scout = unit;
                return;
            }
        }
    }

    public static void assignPatrolScout() {
        for (UnitInPool unit: AGameInfoCache.getUnits(Alliance.SELF, Units.PROTOSS_PROBE)) {
            if (Probe.canBuild(unit)) {
                patrolScout = unit;
                return;
            }
        }
    }

    public static boolean isScout(UnitInPool a) {
        return (scout != null && a.getTag().equals(scout.getTag())) || (patrolScout != null && a.getTag().equals(patrolScout.getTag()));
    }
}
