package com.tistory.workshop6349.build;

import com.github.ocraft.s2client.protocol.data.UnitType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class Build {

    public static int build_index = 0;
    public static int ideal_gases = 8;
    public static boolean pull_off_gas = false;
    public static List<Pair<Integer, UnitType>> build = new ArrayList<>();

}
