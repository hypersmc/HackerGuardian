package me.hackerguardian.main.Checks.movement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.hackerguardian.main.Checks.Check;
import me.hackerguardian.main.Checks.CheckResult;
import me.hackerguardian.main.Checks.PlayerLogger;
import me.hackerguardian.main.Checks.User;
import me.hackerguardian.main.Utils.UtilBlock;
import me.hackerguardian.main.Utils.UtilMath;
import me.hackerguardian.main.Utils.UtilPlayer;
import me.hackerguardian.main.Utils.UtilTime;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;


public class FlightFCheck extends Check {

    private static Map<Player, Integer> uvl = new HashMap<Player, Integer>();
    private static Map<Player, Double> ls = new HashMap<Player, Double>();

    @Override
    public String getName() {
        return "Flight";
    }

    @Override
    public String getEventCall() {
        return "PlayerMoveEvent";
    }


    @Override
    public CheckResult performCheck(User u, Event e) {

        PlayerMoveEvent ev = (PlayerMoveEvent) e;
        Location to = ev.getTo();
        Location from = ev.getFrom();

        if (from == null)
            return new CheckResult("Flight", true, "not enough data");

        if (u.isBouncing()) {
            return new CheckResult("Flight", true, "bouncing");
        }
        double movementSpeed = UtilMath.getHorizontalDistance(to, from);
        if (!ls.containsKey(u.getPlayer())) {
            ls.put(u.getPlayer(), movementSpeed);
            return new CheckResult("Flight", true, "not enough data");
        }
        double LastSpeed = ls.get(u.getPlayer());
        double prediction = (LastSpeed * 0.91f) + 0.025D;
        int vl = 0;
        if (uvl.containsKey(u.getPlayer())) {
            vl = uvl.get(u.getPlayer());
        }
        double diff = Math.abs(movementSpeed - prediction);
        List<Material> nearby = UtilBlock.getSurroundingMat(u.getBlock(), true);
        if (u.getVehicle() == null && !u.getPlayer().isOnGround() && !UtilPlayer.isFlying(u.getPlayer())
                && !nearby.contains(Material.LADDER) && !nearby.contains(Material.SCAFFOLDING)
                && !nearby.contains(Material.VINE) && !UtilPlayer.isSwimming(u.getPlayer()) && to.getY() >= from.getY()
                && UtilTime.elapsed(PlayerLogger.getLogger().getLastVelocity(u.getPlayer()), 1000L)
                && !nearby.contains(Material.WATER) && !nearby.contains(Material.LAVA)) {
            if (diff > 0.025) {
                if (vl++ > 10) {
                    return new CheckResult("Flight", false, "Type F");
                }
            } else {
                vl -= vl > 0 ? 1 : 0;
            }
        } else {
            vl = 0;
        }
        uvl.put(u.getPlayer(), vl);

        return new CheckResult("Flight", true, "pass");
    }

}