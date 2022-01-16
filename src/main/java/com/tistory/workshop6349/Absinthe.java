package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;

public class Absinthe extends S2Agent {

    public static double timeSum = 0;
    public static int frame = 0;
    public static double max = -1;

    @Override
    public void onGameFullStart() {
        long startTime = System.nanoTime();
        AGame.startFrame(observation(), actions(), query(), debug());
        AGameInfoCache.startFrame();

    }

    @Override
    public void onStep() {

    }

    @Override
    public void onUnitCreated(UnitInPool unitInPool) {

    }

    @Override
    public void onGameEnd() {

    }
}
