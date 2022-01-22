package com.tistory.workshop6349;

import com.github.ocraft.s2client.bot.S2Coordinator;
import com.github.ocraft.s2client.protocol.game.AiBuild;
import com.github.ocraft.s2client.protocol.game.Difficulty;
import com.github.ocraft.s2client.protocol.game.LocalMap;
import com.github.ocraft.s2client.protocol.game.Race;

import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
		public static boolean ladder = false;
	    public static void main(String[] args) {
			Absinthe bot = new Absinthe();
	        S2Coordinator s2Coordinator;
	        if (ladder) {
	        	System.out.println("Starting ladder game");
		        s2Coordinator = S2Coordinator.setup()
		                .loadLadderSettings(args)
		                .setParticipants(S2Coordinator.createParticipant(Race.PROTOSS, bot))
		                .connectToLadder()
		                .joinGame();
	        }
			else {
	        	System.out.println("Starting regular game");
	        	System.out.println("Enter the race you would like the human to play");
	        	Scanner input = new Scanner(System.in);
	        	Race choice = Race.NO_RACE;
	        	while (choice == Race.NO_RACE) {
	        		String current = input.nextLine();
					switch (current.toLowerCase()) {
						case "terran" -> choice = Race.TERRAN;
						case "protoss" -> choice = Race.PROTOSS;
						case "zerg" -> choice = Race.ZERG;
						case "random" -> choice = Race.RANDOM;
					}
	        	}
	        	input.close();
		        s2Coordinator = S2Coordinator.setup()
		                .loadSettings(args)
						.setRealtime(false)
						.setNeedsSupportDir(true)
						.setRawAffectsSelection(true)
						.setTimeoutMS(600 * 1000)
		                .setParticipants(
		                        S2Coordinator.createParticipant(Race.PROTOSS, bot),
		                        S2Coordinator.createComputer(choice, Difficulty.VERY_HARD, AiBuild.MACRO))
		                .launchStarcraft()
		                .startGame(LocalMap.of(Paths.get("Ladder2019Season1/AutomatonLE.SC2Map")));
	        }
	        while (s2Coordinator.update()) {
	        }
	        s2Coordinator.quit();
	    }

	}