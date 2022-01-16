package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.gateway.*;
import com.github.ocraft.s2client.protocol.action.ActionChat;
import com.github.ocraft.s2client.protocol.data.*;
import com.github.ocraft.s2client.protocol.debug.Color;
import com.github.ocraft.s2client.protocol.game.PlayerInfo;
import com.github.ocraft.s2client.protocol.game.Race;
import com.github.ocraft.s2client.protocol.query.AvailableAbilities;
import com.github.ocraft.s2client.protocol.query.QueryBuildingPlacement;
import com.github.ocraft.s2client.protocol.response.ResponseGameInfo;
import com.github.ocraft.s2client.protocol.spatial.Point;
import com.github.ocraft.s2client.protocol.spatial.Point2d;
import com.github.ocraft.s2client.protocol.unit.Tag;
import com.github.ocraft.s2client.protocol.unit.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AGame {

    static ActionInterface action;
    static ObservationInterface observation;
    static QueryInterface query;
    static DebugInterface debug;
    
    static ResponseGameInfo gameInfo = null;
    static Map<UnitType, UnitTypeData> unitTypeData = null;
    static Map<Upgrade, UpgradeData> upgradeData = null;
    static Map<Ability, AbilityData> abilityData = null;

    /*
     * The money that has been spent this frame
     * index 0 is minerals
     * index 1 is gas
     */
    static int[] spending = new int[2];

    public static void startFrame(ObservationInterface o, ActionInterface a, QueryInterface q, DebugInterface d) {
        observation = o;
        action = a;
        query = q;
        debug = d;

        spending = new int[2];
    }

    public static void onFrame() {

    }

    public static void endFrame() {
        action.sendActions();
    }

    public static void unitCommand(Unit u, Ability a, Unit t, boolean queued) {
        if (u.getOrders().size() > 0 && !queued) {
            if (u.getOrders().get(0).getAbility() == a) {
                if (u.getOrders().get(0).getTargetedUnitTag().isPresent()) {
                    if (t.getTag() == u.getOrders().get(0).getTargetedUnitTag().get()) {
                        return;
                    }
                }
            }
        }
        ACounter.increment(u.getType().toString());
        action.unitCommand(u, a, t, queued);
    }

    public static void unitCommand(Unit u, Ability a, Point2d p, boolean queued) {
        if (u.getOrders().size() > 0 && !queued) {
            if (u.getOrders().get(0).getAbility() == a) {
                if (u.getOrders().get(0).getTargetedWorldSpacePosition().isPresent()) {
                    if (p.distance(u.getOrders().get(0).getTargetedWorldSpacePosition().get().toPoint2d()) < 1) {
                        return;
                    }
                }
            }
        }
        ACounter.increment(u.getType().toString());
        action.unitCommand(u, a, p, queued);
    }

    public static void unitCommand(Unit u, Ability a, boolean queued) {
        ACounter.increment(u.getType().toString());
        action.unitCommand(u, a, queued);
    }

    public static void unitCommand(Unit u, Ability a, Unit t) {
        unitCommand(u, a, t, false);
    }

    public static void unitCommand(Unit u, Ability a, Point2d p) {
        unitCommand(u, a, p, false);
    }

    public static void unitCommand(Unit u, Ability a) {
        unitCommand(u, a, false);
    }

    public static void unitCommand(UnitInPool u, Ability a, Unit t, boolean queued) {
        unitCommand(u.unit(), a, t, queued);
    }

    public static void unitCommand(UnitInPool u, Ability a, Point2d p, boolean queued) {
        unitCommand(u.unit(), a, p, queued);
    }

    public static void unitCommand(UnitInPool u, Ability a, boolean queued) {
        unitCommand(u.unit(), a, queued);
    }

    public static void unitCommand(UnitInPool u, Ability a, Unit t) {
        unitCommand(u.unit(), a, t, false);
    }

    public static void unitCommand(UnitInPool u, Ability a, Point2d p) {
        unitCommand(u.unit(), a, p, false);
    }

    public static void unitCommand(UnitInPool u, Ability a) {
        unitCommand(u.unit(), a, false);
    }

    public static boolean hasUpgrade(Upgrade u) {
        return observation.getUpgrades().contains(u);
    }

    public static List<UnitInPool> getUnits() {
        return observation.getUnits();
    }

    public static UnitInPool getUnit(Tag t) {
        return observation.getUnit(t);
    }

    public static void chat(String s) {
        if (AConstants.CHAT) {
            action.sendChat(s, ActionChat.Channel.BROADCAST);
        }
    }

    public static double getGameTime() {
        return observation.getGameLoop() / AConstants.FPS;
    }

    public static long getFrame() {
        return observation.getGameLoop();
    }

    public static boolean canPlace(Ability a, Point2d p) {
        return query.placement(a, p);
    }

    public static List<Boolean> canPlace(Ability a, List<Point2d> points) {
        List<QueryBuildingPlacement> queries = new ArrayList<>();
        for (Point2d p : points) {
            queries.add(QueryBuildingPlacement.placeBuilding().useAbility(a).on(p).build());
        }
        return query.placement(queries);
    }

    public static int supply() {
        return observation.getFoodUsed();
    }

    public static int supplyCap() {
        return observation.getFoodCap();
    }

    public static int armySupply() {
        return observation.getFoodArmy();
    }

    public static int minerals() {
        return observation.getMinerals() - spending[0];
    }

    public static int gas() {
        return observation.getVespene() - spending[1];
    }

    public static void spend(int m, int g) {
        spending[0] += m;
        spending[1] += g;
    }

    public static void purchase(Upgrade u) {

    }

    public static void purchase(UnitType unitType) {

    }

    public static Map<UnitType, UnitTypeData> getUnitTypeData() {
        if (unitTypeData == null) {
            unitTypeData = observation.getUnitTypeData(false);
        }
        return unitTypeData;
    }

    public static Map<Upgrade, UpgradeData> getUpgradeData() {
        if (upgradeData == null) {
            upgradeData = observation.getUpgradeData(false);
        }
        return upgradeData;
    }

    public static Map<Ability, AbilityData> getAbilityData() {
        if (abilityData == null) {
            abilityData = observation.getAbilityData(false);
        }
        return abilityData;
    }

    public static ResponseGameInfo getGameInfo() {
        if (gameInfo == null) {
            gameInfo = observation.getGameInfo();
        }
        return gameInfo;
    }

    public static int getPlayerId() {
        return observation.getPlayerId();
    }

    public static Race getOpponentRace() {
        for (PlayerInfo player : getGameInfo().getPlayersInfo()) {
            if (player.getPlayerId() != getPlayerId()) {
                return player.getRequestedRace();
            }
        }
        return Race.RANDOM;
    }

    public static boolean pathAble(Point2d p) {
        return observation.isPathable(p);
    }

    public static boolean buildAble(Point2d p) {
        return observation.isPathable(p);
    }

    public static float height(Point2d p) {
        return observation.terrainHeight(p);
    }

    public static float pathingDistance(Point2d a, Point2d b) {
        return query.pathingDistance(a, b);
    }

    public static boolean canAfford(UnitType u) {
        int minerals = getUnitTypeData().get(u).getMineralCost().orElse(0);
        int gas = getUnitTypeData().get(u).getVespeneCost().orElse(0);

        // 희생해야하는 경우 추가 비용
        // 예) 일벌레 변태

        return minerals <= minerals() && gas <= gas();
    }

    public static boolean canAfford(Upgrade u) {
        int minerals = getUpgradeData().get(u).getMineralCost().orElse(0);
        int gas = getUpgradeData().get(u).getVespeneCost().orElse(0);

        return minerals <= minerals() && gas <= gas();
    }

    public static boolean isTownHall(UnitType u) {
        return u.equals(Units.PROTOSS_NEXUS)
                || u.equals(Units.TERRAN_COMMAND_CENTER)
                || u.equals(Units.TERRAN_COMMAND_CENTER_FLYING)
                || u.equals(Units.TERRAN_ORBITAL_COMMAND)
                || u.equals(Units.TERRAN_ORBITAL_COMMAND_FLYING)
                || u.equals(Units.TERRAN_PLANETARY_FORTRESS)
                || u.equals(Units.ZERG_HATCHERY)
                || u.equals(Units.ZERG_LAIR)
                || u.equals(Units.ZERG_HIVE);
    }

    public static boolean isWorker(UnitType u) {
        return u.equals(Units.ZERG_DRONE)
                || u.equals(Units.TERRAN_SCV)
                || u.equals(Units.PROTOSS_PROBE);
    }

    public static boolean isStructure(UnitType u) {
        if (isTownHall(u)) {
            return true;
        }

        return (getUnitTypeData().get(u).getFoodRequired().orElse((float) 0) == 0
                && getUnitTypeData().get(u).getFoodProvided().orElse((float) 0) == 0
                && (getUnitTypeData().get(u).getMineralCost().orElse(0) > 0 || getUnitTypeData().get(u).getVespeneCost().orElse(0) > 0))
                || (getUnitTypeData().get(u).getRace().orElse(Race.NO_RACE) != Race.ZERG && getUnitTypeData().get(u).getFoodProvided().orElse((float) 0) > 0);
    }

    public static boolean isPlaceable(Point2d p) {
        return AGame.observation.isPlacable(p);
    }

    public static AvailableAbilities availableAbilities(UnitInPool u) {
        return query.getAbilitiesForUnit(u.unit(), false);
    }

    public static List<AvailableAbilities> availableAbilities(List<UnitInPool> u) {
        List<Unit> parsed = new ArrayList<>();
        for (UnitInPool unitInPool : u) {
            parsed.add(unitInPool.unit());
        }
        return query.getAbilitiesForUnits(parsed, false);
    }

    public static void drawBox(Point2d current, Color c) {
        if (AConstants.DEBUG) {
            debug.debugBoxOut(Point.of(current.getX(), current.getY(), (float) (Math.max(AGame.height(current) + .5, 0))),
                    Point.of((float) (current.getX() + .5), (float) (current.getY() + .5), (float) (Math.max(AGame.height(current) + .5, 0))), c);
        }
    }

    public static void drawLine(Point2d a, Point2d b, Color c) {
        if (AConstants.DEBUG) {
            debug.debugLineOut(Point.of(a.getX(), a.getY(), (float) (AGame.height(a) + .5)),
                    Point.of((float) (b.getX()), (float) (b.getY()), (float) (AGame.height(b) + .5)), c);
        }
    }

    public static float armyKilled() {
        return AGame.observation.getScore().getDetails().getKilledMinerals().getArmy()
                + AGame.observation.getScore().getDetails().getKilledVespene().getArmy();
    }

    public static float armyLost() {
        return AGame.observation.getScore().getDetails().getLostMinerals().getArmy()
                + AGame.observation.getScore().getDetails().getLostVespene().getArmy();
    }

    public static boolean hitsAir(UnitType u) {
        for (Weapon w : getUnitTypeData().get(u).getWeapons()) {
            if (w.getTargetType().equals(Weapon.TargetType.AIR) || w.getTargetType().equals(Weapon.TargetType.ANY)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCombat(UnitType u) {
        return getUnitTypeData().get(u).getWeapons().size() > 0 && !isWorker(u);
    }

    public static boolean isChangeling(UnitType u) {
        return u.toString().toLowerCase().contains("changeling");
    }

}
