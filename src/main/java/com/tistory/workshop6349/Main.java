package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.S2Coordinator;
import com.github.ocraft.s2client.protocol.game.Difficulty;
import com.github.ocraft.s2client.protocol.game.LocalMap;
import com.github.ocraft.s2client.protocol.game.Race;

import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    public static boolean playLadder = false;

    public static void main(String[] args) {
        Absinthe absinthe = new Absinthe();
        S2Coordinator s2Coordinator;

        if (playLadder) {
            System.out.println("Starting Ladder Game");
            s2Coordinator = S2Coordinator.setup()
                    .loadLadderSettings(args)
                    .setParticipants(S2Coordinator.createParticipant(Race.PROTOSS, absinthe))
                    .connectToLadder()
                    .joinGame();
        }
        else {
            System.out.println("Starting regular Game");
            System.out.println("Enter the race would like the human to play");
            Scanner scanner = new Scanner(System.in);
            Race choiceRace = Race.NO_RACE;
            while (choiceRace == Race.NO_RACE) {
                String currentOrder = scanner.nextLine();
                switch (currentOrder.toLowerCase()) {
                    case "terran" -> choiceRace = Race.TERRAN;
                    case "zerg" -> choiceRace = Race.ZERG;
                    case "protoss" -> choiceRace = Race.PROTOSS;
                    case "random" -> choiceRace = Race.RANDOM;
                }
            }
            scanner.close();
            s2Coordinator = S2Coordinator.setup()
                    .loadLadderSettings(args)
                    .setRealtime(false)
                    .setParticipants(
                            S2Coordinator.createParticipant(Race.PROTOSS, absinthe),
                            S2Coordinator.createComputer(choiceRace, Difficulty.VERY_HARD)
                    )
                    .launchStarcraft()
                    .startGame(LocalMap.of(Paths.get("AutomationLE.SC2Map")));
        }

        while (s2Coordinator.update()) {

        }
        s2Coordinator.quit();
    }

}
