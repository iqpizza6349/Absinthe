package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.Abilities;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Upgrade;
import com.github.ocraft.s2client.protocol.unit.Alliance;

public class ATechManager {

    public static void onFrame() {
        outer: for (Upgrade u : ABuild.upgrades) {
            if (!(AGame.hasUpgrade(u)) && !AGameInfoCache.isResearching(u)) {
                for (UnitType t: AGame.getUnitTypeData().keySet()) {
                    if (t.getAbilities().contains(AGame.getUpgradeData().get(u).getAbility().orElse(Abilities.INVALID))
                            || t.getAbilities().contains(AGame.getAbilityData().get(AGame.getUpgradeData().get(u).getAbility().orElse(Abilities.INVALID)).getRemapsToAbility().orElse(Abilities.INVALID))) {
                        for (UnitInPool up: AGameInfoCache.getUnits(Alliance.SELF, t)) {
                            if (up.unit().getOrders().size() == 0 && up.unit().getBuildProgress() > 0.999) {
                                if (AGame.canAfford(u)) {
                                    AGame.unitCommand(up, AGame.getUpgradeData().get(u).getAbility().get());
                                }
                                AGame.purchase(u);
                                continue outer;
                            }
                        }
                    }
                }
            }
        }
    }

}
