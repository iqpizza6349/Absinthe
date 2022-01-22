package com.tistory.workshop6349.economy;

import com.github.ocraft.s2client.protocol.unit.Alliance;
import com.tistory.workshop6349.Constants;
import com.tistory.workshop6349.Vector2d;
import com.tistory.workshop6349.game.AbsintheUnit;
import com.tistory.workshop6349.game.Game;
import com.tistory.workshop6349.game.GameInfoCache;

import java.util.ArrayList;

public class Base {

    public Vector2d location;
    public ArrayList<AbsintheUnit> gases;
    public ArrayList<AbsintheUnit> minerals;

    public AbsintheUnit walking_drone = null;
    public AbsintheUnit queen = null;
    public AbsintheUnit command_structure = null;

    public AbsintheUnit ling = null;

    public long last_seen_frame = (long) (Constants.FPS * -60);

    public Base(Vector2d l) {
        location = l;
        minerals =  new ArrayList<>();
        gases =  new ArrayList<>();
    }

    public void update() {

        minerals =  new ArrayList<>();
        gases =  new ArrayList<>();
        if (has_walking_drone() && !walking_drone.alive()) walking_drone = null;
        if ((has_friendly_command_structure() || has_enemy_command_structure()) && !command_structure.alive()) command_structure = null;
        if (has_queen() && !queen.alive()) queen = null;
        if (has_friendly_command_structure() || has_enemy_command_structure()) {
            for (AbsintheUnit u: GameInfoCache.get_units(Alliance.NEUTRAL)) {
                if (u.distance(location) < 10) {
                    if (u.minerals() > 0) minerals.add(u);
                    if (u.gas() > 0) gases.add(u);
                }
            }
        }

        if (Game.is_visible(location)) last_seen_frame = Game.get_frame();
    }

    public void set_walking_drone(AbsintheUnit drone) {
        walking_drone = drone;
    }

    public void set_command_structure(AbsintheUnit p) {
        command_structure = p;
    }

    public void set_queen(AbsintheUnit p) {
        queen = p;
    }

    public boolean has_queen() {
        return queen != null;
    }

    public boolean has_friendly_command_structure() {
        return command_structure != null && command_structure.friendly();
    }

    public boolean has_enemy_command_structure() {
        return command_structure != null && !command_structure.friendly();
    }

    public boolean has_command_structure() {
        return command_structure != null;
    }

    public boolean has_walking_drone() {
        return walking_drone != null;
    }

    public boolean has_ling() {
        return ling != null && ling.alive();
    }

}
