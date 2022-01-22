package com.tistory.workshop6349;

import com.tistory.workshop6349.game.Game;

import java.util.HashSet;
import java.util.Set;

public class Chat {

    private static final Set<String> sent = new HashSet<>();

    public static void sendMessage(String message) {
        if (!sent.contains(message)) {
            sent.add(message);
            Game.chat(message);
        }
    }
}
