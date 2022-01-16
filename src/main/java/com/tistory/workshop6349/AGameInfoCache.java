package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.data.*;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.github.ocraft.s2client.protocol.unit.UnitOrder;

import java.util.*;

public class AGameInfoCache {

    static Map<Ability, Integer> production = new HashMap<>();
    static Map<Tag, UnitInPool> allUnits = new HashMap<>();

    static Map<UnitType, Integer> countsFriendly = new HashMap<>();
    static Map<UnitType, Integer> countsEnemy = new HashMap<>();

    static Map<Tag, UnitInPool> visibleFriendly = new HashMap<>();
    static Map<Tag, UnitInPool> visibleEnemy = new HashMap<>();
    static Map<Tag, UnitInPool> visibleNeutral = new HashMap<>();

    static Set<Tag> claimedGases = new HashSet<>();
    static Set<Tag> trainingWorkers = new HashSet<>();


    static void startFrame() {
        production.clear();
        trainingWorkers.clear();

        visibleFriendly.clear();
        visibleEnemy.clear();
        visibleNeutral.clear();

        countsFriendly.clear();
        countsEnemy.clear();

        claimedGases.clear();

        for (UnitInPool u : AGame.getUnits()) {
            allUnits.put(u.getTag(), u);

            if (u.isAlive()) {
                if (u.unit().getAlliance() == Alliance.SELF) {
                    if (u.unit().getBuildProgress() > AConstants.DONE) {
                        countsFriendly.put(u.unit().getType(), countsFriendly.getOrDefault(u.unit().getType(), 0) + 1);
                    }
                    visibleFriendly.put(u.getTag(), u);
                    for (UnitOrder o : u.unit().getOrders()) {
                        production.put(o.getAbility(), production.getOrDefault(o.getAbility(), 0) + 1);

                        if (AGame.isWorker(u.unit().getType())) {
                            if (o.getAbility() == Abilities.BUILD_ASSIMILATOR
                                    || o.getAbility() == Abilities.BUILD_REFINERY
                                    || o.getAbility() == Abilities.BUILD_EXTRACTOR) {
                                claimedGases.add(o.getTargetedUnitTag().get());
                            }

                            if (o.getAbility() != Abilities.HARVEST_GATHER && o.getAbility() != Abilities.HARVEST_RETURN) {
                                for (UnitTypeData t: AGame.getUnitTypeData().values()) {
                                    if (o.getAbility() == t.getAbility().orElse(Abilities.INVALID)) {
                                        trainingWorkers.add(u.getTag());
                                        break;
                                    }
                                }
                                break;
                            }


                        }
                    }
                }
                else if (u.unit().getAlliance() == Alliance.ENEMY) {
                    countsEnemy.put(u.unit().getType(), countsEnemy.getOrDefault(u.unit().getType(), 0) + 1);
                    visibleEnemy.put(u.getTag(), u);
                }
                else {
                    visibleNeutral.put(u.getTag(), u);
                }
            }

            if (u.unit().getBuildProgress() < AConstants.DONE) {
                production.put(AGame.getUnitTypeData().get(u.unit().getType()).getAbility().orElse(Abilities.INVALID),
                        production.getOrDefault(AGame.getUnitTypeData().get(u.unit().getType()).getAbility().orElse(Abilities.INVALID), 0) + 1);
            }
        }
    }

    static void onFrame() {

    }

    static void endFrame() {

    }

    public static int countFriendly(UnitType type) {
        return countsFriendly.getOrDefault(type, 0);
    }

    public static int countEnemy(UnitType type) {
        return countsEnemy.getOrDefault(type, 0);
    }

    static ArrayList<UnitInPool> getUnits(UnitType type) {
        ArrayList<UnitInPool> units = new ArrayList<>();
        for (UnitInPool u: AGame.getUnits()) {
            if (u.unit().getType() == type) units.add(u);
        }
        return units;
    }

    public static ArrayList<UnitInPool> getUnits(Alliance team) {
        if (team == Alliance.SELF) return new ArrayList<>(visibleFriendly.values());
        if (team == Alliance.NEUTRAL) return new ArrayList<>(visibleNeutral.values());
        return new ArrayList<>(visibleEnemy.values());
    }

    public static ArrayList<UnitInPool> getUnits(Alliance team, UnitType type) {
        ArrayList<UnitInPool> units = new ArrayList<>();
        if (team == Alliance.SELF) {
            for (UnitInPool u: visibleFriendly.values()) {
                if (u.unit().getType() == type) units.add(u);
            }
        } else if (team == Alliance.ENEMY){
            for (UnitInPool u: visibleEnemy.values()) {
                if (u.unit().getType() == type) units.add(u);
            }
        } else {
            for (UnitInPool u: visibleNeutral.values()) {
                if (u.unit().getType() == type) units.add(u);
            }
        }
        return units;
    }

    public static boolean geyserIsFree(UnitInPool u) {
        for (UnitInPool e : getUnits(Alliance.SELF, Units.PROTOSS_ASSIMILATOR)) {
            if (e.unit().getPosition().toPoint2d().distance(u.unit().getPosition().toPoint2d()) < 1) {
                return false;
            }
        }
        return !claimedGases.contains(u.getTag());
    }

    public static int inProgress(UnitType t) {
        return production.getOrDefault(AGame.getUnitTypeData().get(t).getAbility().orElse(Abilities.INVALID), 0);
    }

    public static boolean isResearching(Upgrade u) {
        if (AGame.getAbilityData().get(AGame.getUpgradeData().get(u).getAbility().get()).getRemapsToAbility().isPresent()) {
            if (production.getOrDefault(AGame.getAbilityData().get(AGame.getUpgradeData().get(u).getAbility().get()).getRemapsToAbility().get(), 0) > 0) {
                return true;
            }
        }
        return production.getOrDefault(AGame.getUpgradeData().get(u).getAbility().get(), 0) > 0;
    }

}
