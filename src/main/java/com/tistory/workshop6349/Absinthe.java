package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.S2Agent;
import com.github.ocraft.s2client.bot.gateway.UnitInPool;
import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.tistory.workshop6349.army.*;
import com.tistory.workshop6349.build.BuildExecutor;
import com.tistory.workshop6349.build.BuildPlanner;
import com.tistory.workshop6349.build.UpgradeManager;
import com.tistory.workshop6349.economy.BaseManager;
import com.tistory.workshop6349.economy.EconomyManager;
import com.tistory.workshop6349.economy.MiningOptimizer;
import com.tistory.workshop6349.enemy.EnemyBaseDefense;
import com.tistory.workshop6349.enemy.EnemyModel;
import com.tistory.workshop6349.enemy.ResourceTracking;
import com.tistory.workshop6349.game.AbsintheUnit;
import com.tistory.workshop6349.game.Game;
import com.tistory.workshop6349.game.GameInfoCache;
import com.tistory.workshop6349.knowledge.Scouting;
import com.tistory.workshop6349.knowledge.Wisdom;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Absinthe extends S2Agent {

    private static final String replayName = "C:\\Users\\IQPIZZA\\Desktop\\Replays\\Absinthe_";

    private static double timeSum;
    private static int frame;
    private static double max = -1;

    @Override
    public void onGameFullStart() {
        long startTime = System.nanoTime();
        Game.start_frame(observation(), actions(), query(), debug());
        GameInfoCache.start_frame();
        BaseManager.start_game();
        BuildPlanner.decide_build();
        UpgradeManager.start_game();

        System.out.println("Start game took " + ((System.nanoTime() - startTime) / 1000000.0) + " ms");
    }

    @Override
    public void onStep() {
        long startTime = System.nanoTime();

        try {
            if (Game.get_true_frame() % Constants.FRAME_SKIP == 0) {
                Game.start_frame(observation(), actions(), query(), debug());

                GameInfoCache.start_frame();
                UnitRoleManager.on_frame();
                ResourceTracking.on_frame();
                EnemyModel.on_frame();
                EnemySquadManager.on_frame();
                ThreatManager.on_frame();
                UnitMovementManager.on_frame();
                MiningOptimizer.on_frame();
                BanelingAvoidance.on_frame();
                EnemyBaseDefense.on_frame();

                Scouting.on_frame();
                ArmyManager.on_frame();
                BaseManager.on_frame();
                EconomyManager.on_frame();
                BuildPlanner.on_frame();
                BuildExecutor.on_frame();
                UnitManager.onFrame();
                Game.end_frame();

                if (Wisdom.cannon_rush()) {
                    Game.write_text("Enemy Strategy: Cannon Rush");
                }
                else if (Wisdom.worker_rush()) {
                    Game.write_text("Enemy Strategy: Worker Rush");
                }
                else if (Wisdom.proxy_detected()) {
                    Game.write_text("Enemy Strategy: Proxy Cheese");
                }
                else if (Wisdom.all_in_detected()) {
                    Game.write_text("Enemy Strategy: All-in");
                }
                else {
                    Game.write_text("Enemy Strategy: Macro");
                }
            }

            if (Constants.DEBUG) {
                Game.debug.sendDebug();
                timeSum += ((System.nanoTime() - startTime) / 1000000.0);
                if (((System.nanoTime() - startTime) / 1000000.0) > max) {
                    max = (System.nanoTime() - startTime) / 1000000.0;
                }
                frame++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUnitCreated(UnitInPool unitInPool) {
        BaseManager.on_unit_created(AbsintheUnit.getInstance(unitInPool));
    }

    @Override
    public void onUnitDestroyed(UnitInPool unitInPool) {
        if (unitInPool.unit().getAlliance() == Alliance.ENEMY) {
            EnemyModel.removeFromModel(AbsintheUnit.getInstance(unitInPool));
        }
    }

    @Override
    public void onGameEnd() {
        Counter.print();
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HH_mm_ss"));
        try {
            control().saveReplay(Path.of(replayName + date + ".SC2Replay"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
