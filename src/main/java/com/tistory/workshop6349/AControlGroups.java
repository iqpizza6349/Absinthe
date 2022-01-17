package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.github.ocraft.s2client.protocol.unit.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AControlGroups {

    public static Map<Integer, ArrayList<UnitInPool>> groups = new HashMap<>();
    public static Map<Tag, Integer> assignments = new HashMap<>();

    public static void onFrame() {
        for (UnitInPool u : AGameInfoCache.getUnits(Alliance.SELF)) {
            if (AGame.isCombat(u.unit().getType()) && !AGame.isWorker(u.unit().getType())) {
                if (!assignments.containsKey(u.getTag())) {
                    add(u, 0);
                }
            }
        }
        ArrayList<Integer> empty = new ArrayList<>();
        for (Integer g : groups.keySet()) {
            ArrayList<UnitInPool> new_group = new ArrayList<>();
            for (UnitInPool u : groups.get(g)) {
                if (u.isAlive()) {
                    new_group.add(u);
                }
            }
            groups.put(g, new_group);
            if (groups.get(g).size() == 0) empty.add(g);
        }
        for (int i : empty) {
            disband(i);
        }
    }

    public static void add(UnitInPool u, int group) {
        if (assignments.containsKey(u.getTag())) {
            groups.get(assignments.get(u.getTag())).remove(u);
        }
        assignments.put(u.getTag(), group);
        if (!groups.containsKey(group)) {
            groups.put(group, new ArrayList<>());
        }
        groups.get(group).add(u);
    }

    public static void disband(int group) {
        for (UnitInPool u : groups.remove(group)) {
            add(u, 0);
        }
    }

    public static int create(UnitInPool u) {
        int group = 0;
        while (groups.containsKey(group)) group++;
        groups.put(group, new ArrayList<>());
        groups.get(group).add(u);
        return group;
    }

    public static ArrayList<UnitInPool> get(int i) {
        if (groups.containsKey(i)) {
            return groups.get(i);
        }
        return new ArrayList<>();
    }

}
