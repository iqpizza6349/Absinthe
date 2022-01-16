package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class AEconomyManager {

    public static void onFrame() {
        // worker transfer code
        for (ABase b : ABaseManager.bases) {
            if (!b.hasFriendlyCommandStructure()) continue;
            if (b.commandStructure.unit().getAssignedHarvesters().orElse(0) > b.commandStructure.unit().getIdealHarvesters().orElse(0)) {
                for (ABase target: ABaseManager.bases) {
                    if (!target.hasFriendlyCommandStructure()) continue;
                    if (target.minerals.size() == 0) continue;
                    if (target.commandStructure.unit().getAssignedHarvesters().orElse(0) + AGameInfoCache.inProgress(Units.PROTOSS_PROBE) < target.commandStructure.unit().getIdealHarvesters().orElse(0)) {
                        for (UnitInPool worker : AGameInfoCache.getUnits(Alliance.SELF, Units.PROTOSS_PROBE)) {
                            if (worker.unit().getPosition().toPoint2d().distance(b.location) < 10) {
                                // TODO remove try catch, fix crashing
                                try {
                                    if (worker.unit().getOrders().get(0).getAbility() == Abilities.HARVEST_GATHER && AGame.getUnit(worker.unit().getOrders().get(0).getTargetedUnitTag().get()).unit().getMineralContents().orElse(0) > 0) {
                                        AGame.unitCommand(worker, Abilities.SMART, target.minerals.get(0).unit());
                                        return;
                                    }
                                } catch (Exception e) {
                                    AGame.unitCommand(worker, Abilities.SMART, target.minerals.get(0).unit());
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void assignWorker(UnitInPool u) {
        for (ABase b : ABaseManager.bases) {
            if (b.hasFriendlyCommandStructure() && b.commandStructure.unit().getBuildProgress() > 0.999) {
                if (b.commandStructure.unit().getAssignedHarvesters().orElse(0) < b.commandStructure.unit().getIdealHarvesters().orElse(0)) {
                    if (b.minerals.size() > 0) {
                        AGame.unitCommand(u, Abilities.SMART, b.minerals.get(0).unit());
                        return;
                    }
                }
            }
        }
        for (ABase b : ABaseManager.bases) {
            if (b.hasFriendlyCommandStructure() && b.commandStructure.unit().getBuildProgress() > 0.999) {
                if (b.commandStructure.unit().getAssignedHarvesters().orElse(0) < b.commandStructure.unit().getIdealHarvesters().orElse(0) * 1.5) {
                    if (b.minerals.size() > 0) {
                        AGame.unitCommand(u, Abilities.SMART, b.minerals.get(0).unit());
                        return;
                    }
                }
            }
        }
    }

}
