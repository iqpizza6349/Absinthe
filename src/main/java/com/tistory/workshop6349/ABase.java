package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Alliance;

import java.util.ArrayList;

public class ABase {

    public Point2d location;
    ArrayList<UnitInPool> gases;
    ArrayList<UnitInPool> minerals;
    
    public UnitInPool walkingWorker = null;
    public UnitInPool commandStructure = null; // 넥서스, 해처리, 커맨드 와 같은 중심 건물

    public ABase(Point2d location) {
        this.location = location;
        this.minerals = new ArrayList<>();
        this.gases = new ArrayList<>();
    }

    public void update() {
        minerals = new ArrayList<>();
        gases = new ArrayList<>();

        if (hasWalkingWorker() && !walkingWorker.isAlive()) {
            walkingWorker = null;
        }

        if ((hasFriendlyCommandStructure() || hasEnemyCommandStructure())
                && !commandStructure.isAlive()) {
            commandStructure = null;
        }

        if (hasFriendlyCommandStructure() || hasEnemyCommandStructure()) {
            for (UnitInPool u : AGameInfoCache.getUnits(Alliance.NEUTRAL)) {
                if (u.unit().getPosition().toPoint2d().distance(location) < 10) {
                    if (u.unit().getMineralContents().orElse(0) > 0) {
                        minerals.add(u);
                    }
                    if (u.unit().getVespeneContents().orElse(0) > 0) {
                        gases.add(u);
                    }
                }
            }
        }
    }

    public void setWalkingWorker(UnitInPool p) {
        this.walkingWorker = p;
    }

    public void setCommandStructure(UnitInPool p) {
        this.commandStructure = p;
    }

    public boolean hasFriendlyCommandStructure() {
        return commandStructure != null && commandStructure.unit().getAlliance() == Alliance.SELF;
    }

    public boolean hasEnemyCommandStructure() {
        return commandStructure != null && commandStructure.unit().getAlliance() == Alliance.ENEMY;
    }

    public boolean hasCommandStructure() {
        return commandStructure != null;
    }

    public boolean hasWalkingWorker() {
        return walkingWorker != null;
    }


}
