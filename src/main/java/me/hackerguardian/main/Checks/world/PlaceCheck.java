package me.hackerguardian.main.Checks.world;
import java.util.HashMap;
import java.util.Map;

import me.hackerguardian.main.Checks.Check;
import me.hackerguardian.main.Checks.CheckResult;
import me.hackerguardian.main.Checks.User;
import me.hackerguardian.main.Utils.UtilMath;
import me.hackerguardian.main.Utils.UtilPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlaceCheck extends Check {

    @Override
    public String getName() {
        return "Place Check";
    }

    private static Map<Player, Map<Long, Integer>> PlaceCount = new HashMap<Player, Map<Long, Integer>>();

    @Override
    public String getEventCall() {
        return "BlockPlaceEvent";
    }

    @Override
    public CheckResult performCheck(User u, Event e) {
        BlockPlaceEvent event = (BlockPlaceEvent) e;
        Player p = u.getPlayer();
        int Count = 1;
        if (PlaceCount.containsKey(p)) {
            try {
                if (PlaceCount.get(p).keySet().iterator().next() > System.currentTimeMillis()) {
                    Count += PlaceCount.get(p).values().iterator().next();
                } else {
                    PlaceCount.get(p).clear();
                }
            } catch (Exception excep) {
                PlaceCount.get(p).clear();
            }
        }
        if (event.getBlockPlaced().getType() == Material.SCAFFOLDING) {
            return new CheckResult("Impossible Place", true, "Scaffolding Ignored");
        }
        if (!PlaceCount.containsKey(p)) {
            PlaceCount.put(p, new HashMap<Long, Integer>());
        }
        Map<Long, Integer> R = new HashMap<Long, Integer>();
        if (PlaceCount.get(p).size() == 0) {
            R.put(System.currentTimeMillis() + 1000, Count);
        } else {
            R.put(PlaceCount.get(p).keySet().iterator().next(), Count);
        }
        PlaceCount.put(p, R);
        if (Count > 12 && !UtilPlayer.hasEfficiency(p)) {
            event.setCancelled(true);
            return new CheckResult("Fast Place", false, "placed at a rate of 12+bps");
        }
        Location placed = event.getBlockPlaced().getLocation();
        Boolean call = false;
        try {
            if (placed.distance(p.getTargetBlockExact(15).getLocation()) > 4.7) {
                call = true;
            }
        } catch (Exception ef) {
            call = false;
        }
        if (call) {
            return new CheckResult("Impossible Place", false,
                    "Placed " + UtilMath.trim(1, placed.distance(p.getTargetBlockExact(15).getLocation()))
                            + " blocks away from crosshair");
        } else {
            return new CheckResult("Impossible Place", true, "Pass");
        }
    }

}