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
        ABaseManager.startGame();
        AScouting.startFrame();

        if (AConstants.DEBUG) {
            AGame.chat("Absinthe version 1.0 TEST");
        }
        System.out.println("Start game took " + ((System.nanoTime() - startTime) / 1000000.0) + " ms");
    }

    @Override
    public void onStep() {
        long startTime = System.nanoTime();

        AGame.startFrame(observation(), actions(), query(), debug());
        if ((AGame.getFrame() % AConstants.FRAME_SKIP) == 0) {
            AGameInfoCache.startFrame();

            AScouting.onFrame();

            AThreatManager.onFrame();

            ABaseManager.onFrame();

            AEconomyManager.onFrame();

            AMapAnalysis.on_frame();

            AGameInfoCache.endFrame();
        }

        if (AConstants.DEBUG) {
            AGame.debug.sendDebug();
            timeSum += ((System.nanoTime() - startTime) / 1000000.0);
            if (((System.nanoTime() - startTime) / 1000000.0) > max) {
                max = (System.nanoTime() - startTime) / 1000000.0;
            }
            frame++;
            System.out.println("Average " + (timeSum / frame));
            System.out.println("Max " + max);
            System.out.println("----------------------------------");
        }
    }

    @Override
    public void onUnitCreated(UnitInPool unitInPool) {

    }

    @Override
    public void onGameEnd() {
        ACounter.print();
    }
}
