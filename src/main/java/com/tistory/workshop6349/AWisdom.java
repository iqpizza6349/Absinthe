package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class AWisdom {

    public static boolean proxyDetected() {
        if (AGame.armySupply() >= 30) return false;
        for (UnitInPool u: AGameInfoCache.getUnits(Alliance.ENEMY)) {
            if (AGame.isStructure(u.unit().getType()) && u.unit().getType() != Units.PROTOSS_PYLON) {
                if (u.unit().getPosition().toPoint2d().distance(ABaseManager.mainBase().location) < u.unit().getPosition().toPoint2d().distance(AScouting.closestEnemySpawn())) {
                    return true;
                }
            }
        }
        return false;
    }
    public static boolean airDetected() {
        for (UnitInPool u: AGameInfoCache.getUnits(Alliance.ENEMY)) {
            if (u.unit().getType() == Units.ZERG_SPIRE || u.unit().getType() == Units.TERRAN_STARPORT || u.unit().getType() == Units.PROTOSS_STARGATE) {
                return true;
            }
        }
        return false;
    }
    public static boolean allInDetected() {
        if (AGame.armySupply() >= 30) return false;
        int t1 = AGameInfoCache.countEnemy(Units.TERRAN_BARRACKS) + AGameInfoCache.countEnemy(Units.PROTOSS_GATEWAY);
        return t1 >= 3 * enemyBases() && enemyBases() != 0;
    }
    public static boolean aggressionDetected() {
        if (AGame.armySupply() >= 30) return false;
        int t1 = AGameInfoCache.countEnemy(Units.TERRAN_BARRACKS) + AGameInfoCache.countEnemy(Units.PROTOSS_GATEWAY);
        return t1 >= 2 * enemyBases() && enemyBases() == 1;
    }
    public static int enemyBases() {
        int result = 0;
        for (UnitInPool u: AGameInfoCache.getUnits(Alliance.ENEMY)) {
            if (AGame.isTownHall(u.unit().getType())) result++;
        }
        return result;
    }
    public static boolean playSafe() {
        return enemyBases() <= 1;
    }
    public static boolean cannonRush() {
        for (UnitInPool u: AGameInfoCache.getUnits(Alliance.ENEMY, Units.PROTOSS_PHOTON_CANNON)) {
            if (u.unit().getPosition().toPoint2d().distance(ABaseManager.mainBase().location) < u.unit().getPosition().toPoint2d().distance(AScouting.closestEnemySpawn())) {
                return true;
            }
        }
        return false;
    }

    public static int enemyProduction() {
        int production = 0;
        for (UnitInPool u: AGameInfoCache.getUnits(Alliance.ENEMY)) {
            if (ABalance.isProductionStructure(u.unit().getType())) {
                production++;
            }
        }
        return production;
    }

    public static boolean confused() {
        if (AGame.getOpponentRace() == Race.PROTOSS) return false;
        return enemyProduction() == 0 && enemyBases() >= 1;
    }

    public static boolean ahead() {
        return AGame.armyKilled() - AGame.armyLost() > (200 * ((AGame.getFrame() / AConstants.FPS)/ 60.0));
    }

    public static boolean workerRush() {
        int total = 0;
        outer: for (UnitInPool enemy: AGameInfoCache.getUnits(Alliance.ENEMY)) {
            if (AGame.isWorker(enemy.unit().getType())) {
                for (ABase b: ABaseManager.bases) {
                    if (b.hasFriendlyCommandStructure() && b.commandStructure.unit().getBuildProgress() > AConstants.DONE) {
                        if (enemy.unit().getPosition().toPoint2d().distance(b.location) < 15) {
                            total++;
                            continue outer;
                        }
                    }
                }
            }
        }
        return total >= 5;
    }
}
