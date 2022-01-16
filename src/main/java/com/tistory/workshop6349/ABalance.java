package com.tistory.workshop6349;

import com.github.ocraft.s2client.protocol.data.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ABalance {

    static Map<UnitType, UnitType> overrides;
    static {
        overrides = new HashMap<>();
        overrides.put(Units.PROTOSS_CYBERNETICS_CORE, Units.PROTOSS_GATEWAY);
    }

    static boolean hasTechRequirement(UnitType u) {
        UnitType current = u;
        while (AGame.getUnitTypeData().get(current).getTechRequirement().orElse(Units.INVALID) != Units.INVALID) {
            if (overrides.containsKey(current)) {
                current = overrides.get(current);
            }
            else {
                current = AGame.getUnitTypeData().get(current).getTechRequirement().orElse(Units.INVALID);
            }
            if (AGameInfoCache.getUnits(current).size() == 0) return true;
        }
        return false;
    }

    static UnitType nextTechRequirement(UnitType u) {
        UnitType current = u;
        UnitType best = Units.INVALID;
        while (AGame.getUnitTypeData().get(current).getTechRequirement().orElse(Units.INVALID) != Units.INVALID) {
            if (overrides.containsKey(current)) {
                current = overrides.get(current);
            }
            else {
                current = AGame.getUnitTypeData().get(current).getTechRequirement().orElse(Units.INVALID);
            }
            if (AGameInfoCache.getUnits(current).size() == 0) best = current;
        }
        return best;
    }

    static Set<UnitType> getProductionStructures(UnitType u) {
        Set<UnitType> result = new HashSet<>();
        for (UnitTypeData d : AGame.getUnitTypeData().values()) {
            for (Ability a: d.getUnitType().getAbilities()) {
                if (AGame.getUnitTypeData().get(u).getAbility().orElse(Abilities.INVALID) == a) {
                    result.add(d.getUnitType());
                }
            }
        }
        return result;
    }

    public static boolean isProductionStructure(UnitType u) {
        return u == Units.TERRAN_BARRACKS || u == Units.TERRAN_FACTORY || u == Units.TERRAN_STARPORT || u == Units.PROTOSS_GATEWAY || u == Units.PROTOSS_ROBOTICS_FACILITY || u == Units.PROTOSS_STARGATE || u == Units.ZERG_HATCHERY;
    }

    public static UnitType getTechStructure(UnitType u) {
        if (overrides.containsKey(u)) {
            return overrides.get(u);
        }
        return AGame.getUnitTypeData().get(u).getTechRequirement().get();
    }

}
