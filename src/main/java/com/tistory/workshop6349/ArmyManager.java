package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.tistory.workshop6349.unitcontrollers.Probe;

public class ArmyManager {

    public static Point2d target;
    public static boolean hasTarget;
    static {
        target = AScouting.closestEnemySpawn();
        hasTarget = true;
    }

    public static void startFrame() {

    }

    public static void onFrame() {
        outer: for (UnitInPool u: AGameInfoCache.getUnits(Alliance.SELF)) {
            if (target.distance(u.unit().getPosition().toPoint2d()) < 4) {
                for (UnitInPool e: AGameInfoCache.getUnits(Alliance.ENEMY)) {
                    if (e.unit().getType() != Units.PROTOSS_ADEPT_PHASE_SHIFT && !AGame.isChangeling(e.unit().getType())) {
                        if (!e.unit().getFlying().orElse(false) || AGameInfoCache.countFriendly(Units.PROTOSS_VOIDRAY) > 0) {
                            if (u.unit().getPosition().distance(e.unit().getPosition()) < 4) {
                                break outer;
                            }
                        }
                    }
                }
                hasTarget = false;
                break;
            }
        }
        if (!hasTarget) {
            for (UnitInPool e: AGameInfoCache.getUnits(Alliance.ENEMY)) {
                if (e.unit().getType() != Units.PROTOSS_ADEPT_PHASE_SHIFT) {
                    if (!e.unit().getFlying().orElse(false) || AGameInfoCache.countFriendly(Units.PROTOSS_VOIDRAY) > 0) {
                        target = e.unit().getPosition().toPoint2d();
                        hasTarget = true;
                        break;
                    }
                }
            }
        }

        if (AGame.armySupply() < 10) {
            if (!AWisdom.workerRush()) {
                if (!AWisdom.cannonRush() && !AWisdom.proxyDetected()) {
                    enemy_loop: for (UnitInPool u: AGameInfoCache.getUnits(Alliance.ENEMY)) {
                        if (AGame.isWorker(u.unit().getType())) {
                            for (ABase b : ABaseManager.bases) {
                                if (b.hasFriendlyCommandStructure() && u.unit().getPosition().toPoint2d().distance(b.location) < 12) {
                                    for (UnitInPool ally: AGameInfoCache.getUnits(Alliance.SELF, Units.PROTOSS_PROBE)) {
                                        if (!(ally.unit().getOrders().size() == 0) && (ally.unit().getOrders().get(0).getAbility() == Abilities.ATTACK || ally.unit().getOrders().get(0).getAbility() == Abilities.ATTACK_ATTACK)) {
                                            if (ally.unit().getOrders().get(0).getTargetedUnitTag().orElse(Tag.of((long) 0)).equals(u.unit().getTag())) {
                                                continue enemy_loop;
                                            }
                                        }
                                    }
                                    for (UnitInPool ally: AGameInfoCache.getUnits(Alliance.SELF, Units.PROTOSS_PROBE)) {
                                        if (Probe.canBuild(ally)) {
                                            AGame.unitCommand(ally, Abilities.ATTACK, u.unit());
                                            continue enemy_loop;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else {
                for (UnitInPool enemy: AGameInfoCache.getUnits(Alliance.ENEMY)) {
                    if (AGame.isWorker(enemy.unit().getType())) {
                        if (enemy.unit().getPosition().toPoint2d().distance(ABaseManager.mainBase().location) <= 20) {
                            for (UnitInPool ally: AGameInfoCache.getUnits(Alliance.SELF, Units.ZERG_DRONE)) {
                                if (ally.unit().getHealth().orElse((float) 0) > 10) {
                                    if (Probe.canBuild(ally)) {
                                        AGame.unitCommand(ally, Abilities.ATTACK, enemy.unit().getPosition().toPoint2d());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (UnitInPool ally: AGameInfoCache.getUnits(Alliance.SELF, Units.ZERG_DRONE)) {
            if (AScouting.isScout(ally)) {
                continue;
            }
            double best = 9999;
            if (!(ally.unit().getOrders().size() == 0)) {
                if (ally.unit().getOrders().get(0).getAbility() == Abilities.ATTACK) {
                    for (ABase b : ABaseManager.bases) {
                        if (b.hasFriendlyCommandStructure()) {
                            if (b.location.distance(ally.unit().getPosition().toPoint2d()) < best) {
                                best = b.location.distance(ally.unit().getPosition().toPoint2d());
                            }
                        }
                    }
                    if (best > 15) AGame.unitCommand(ally, Abilities.STOP);
                    if (ally.unit().getHealth().orElse((float) 0) < 6) {
                        AGame.unitCommand(ally, Abilities.STOP);
                    }
                }
            }
        }

    }

    public static void endFrame() {

    }
}
