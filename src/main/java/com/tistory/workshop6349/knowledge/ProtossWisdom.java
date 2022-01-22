package com.tistory.workshop6349.knowledge;

import com.tistory.workshop6349.economy.BaseManager;
import com.tistory.workshop6349.economy.EconomyManager;
import com.tistory.workshop6349.game.Game;
import com.tistory.workshop6349.game.GameInfoCache;
import com.tistory.workshop6349.game.RaceInterface;

public class ProtossWisdom {

    public static boolean should_build_workers() {
        return GameInfoCache.count(RaceInterface.get_race_worker()) < EconomyManager.total_minerals() + BaseManager.active_gases() * 3;
    }

    public static boolean should_expand() {
        if (Game.army_supply() < 20 * (BaseManager.base_count() - 1)) return false;
        return GameInfoCache.count(RaceInterface.get_race_worker()) + 6 >= EconomyManager.total_minerals() + BaseManager.active_gases() * 3;
    }

    public static boolean should_build_army() {
        return true;
    }
}
