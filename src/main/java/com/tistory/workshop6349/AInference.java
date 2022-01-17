package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Units;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AInference {

    public static Set<Tag> registered = new HashSet<>();
    public static Map<UnitType, ABound> bounds = new HashMap<>();
    public static void onFrame() {
        for (UnitInPool u: AGameInfoCache.getUnits(Alliance.ENEMY)) {
            if (!registered.contains(u.getTag())) {
                registered.add(u.getTag());
                // if a unit is a structure, we call update on it directly
                int build_time = (int) AGame.getUnitTypeData().get(u.unit().getType()).getBuildTime().orElse((float) 0).floatValue();
                int frames_done = (int) (u.unit().getBuildProgress() * build_time);
                if (AGame.isStructure(u.unit().getType())) {
                    update(u.unit().getType(), (int) ((build_time - frames_done) + AGame.getFrame()), u.unit().getBuildProgress() < 0.999);
                }
                else if (AGame.getUnitTypeData().get(u.unit().getType()).getTechRequirement().isPresent()) {
                    if (AGame.getUnitTypeData().get(u.unit().getType()).getTechRequirement().get() != Units.INVALID) {
                        update(AGame.getUnitTypeData().get(u.unit().getType()).getTechRequirement().get(), (int) (AGame.getFrame() - frames_done), false);
                    }
                }
            }
        }
    }

    // updates our information with the knowledge of a unit on a frame
    // makes recursive calls to itself for tech requirements
    public static void update(UnitType u, int frame, boolean exact) {
        if (!bounds.containsKey(u)) AGame.chat("You have a: " + u.toString());
        bounds.put(u, bounds.getOrDefault(u, new ABound(frame, exact).update(frame, exact)));
        if (AGame.getUnitTypeData().get(u).getTechRequirement().isPresent()) {
            if (AGame.getUnitTypeData().get(u).getTechRequirement().get() != Units.INVALID) {
                update(AGame.getUnitTypeData().get(u).getTechRequirement().get(), (int) (frame - AGame.getUnitTypeData().get(u).getBuildTime().orElse((float) 0)), false);
            }
        }
    }

}
