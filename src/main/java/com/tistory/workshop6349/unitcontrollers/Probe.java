package com.tistory.workshop6349.unitcontrollers;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.tistory.workshop6349.*;

public class Probe {

    public static void onFrame(UnitInPool u) {
        if (u.unit().getOrders().size() == 0 && canBuild(u)) {
            AEconomyManager.assignWorker(u);
        }
    }

    public static boolean canBuild(UnitInPool u) {
        for (ABase b : ABaseManager.bases) {
            if (u == b.walkingWorker) {
                return false;
            }
        }
        try {
            return !(AScouting.scout == u || AScouting.patrolScout == u)
                    && (u.unit().getOrders().size() == 0
                    || (u.unit().getOrders().get(0).getTargetedUnitTag().isPresent()
                    && u.unit().getOrders().get(0).getAbility() == Abilities.HARVEST_GATHER
                    && AGame.getUnit(u.unit().getOrders().get(0).getTargetedUnitTag().get()).unit().getMineralContents().orElse(0) > 0));
        } catch (Exception e) {
            return false;
        }
    }

}
