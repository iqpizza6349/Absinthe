package com.tistory.workshop6349.build;

import com.github.ocraft.s2client.protocol.game.Race;
import com.tistory.workshop6349.game.Game;

public class BuildExecutor {

    public static void on_frame() {
        if (Game.race() == Race.ZERG) {
            TechLevelManager.on_frame();
            ZergBuildExecutor.on_frame();
        } else if (Game.race() == Race.PROTOSS) {
            ProtossBuildExecutor.on_frame();
        }
    }
}
