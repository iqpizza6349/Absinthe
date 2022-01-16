package com.tistory.workshop6349;

import com.github.ocraft.s2client.protocol.data.UnitType;
import com.github.ocraft.s2client.protocol.data.Upgrade;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ABuild {
    public static int buildIndex = 0;
    public static int idealNexus = 0;
    public static int idealWorkers = 20;
    public static int idealGases = 6;
    public static int pushSupply = 20;
    public static int techProbes = 30;
    public static boolean scout = true;
    public static boolean pullOffGas = false;
    public static List<UnitType> composition = new ArrayList<>();
    public static Set<Upgrade> upgrades = new HashSet<>();
    public static List<Pair<Integer, UnitType>> build = new ArrayList<>();
}
