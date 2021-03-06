package me.hackerguardian.main.Checks.movement;
import java.util.HashMap;
import java.util.Map;


import me.hackerguardian.main.Checks.Check;
import me.hackerguardian.main.Checks.CheckResult;
import me.hackerguardian.main.Checks.User;
import me.hackerguardian.main.HackerGuardian;
import me.hackerguardian.main.Utils.UtilBlock;
import me.hackerguardian.main.Utils.UtilPlayer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
public class HoverCheck extends Check {

    @Override
    public String getName() {
        return "HoverFlyCheck";
    }

    @Override
    public String getEventCall() {
        return "PlayerMoveEvent";
    }

    private static Map<Player, Map<Integer, Double>> HoverTicks = new HashMap<Player, Map<Integer, Double>>();

    @Override
    public CheckResult performCheck(User u, Event e) {
        Player p = u.getPlayer();
        int Count = 0;
        if (HoverTicks.containsKey(p)) {
            Count = HoverTicks.get(p).keySet().iterator().next();
            if (!UtilBlock.onBlock(p) && p.getLocation().getY() == HoverTicks.get(p).values().iterator().next()) {
                Count++;
                for (Block b : UtilBlock.getSurrounding(p.getLocation().getBlock(), true)) {
                    if (b.getType() != Material.AIR && p.getLocation().distance(b.getLocation()) < 1.25) {
                        Count = 0;
                    }
                }
            }
        }
        if (Count > 2 && !p.isInsideVehicle() && !UtilPlayer.isFlying(p)
                && !p.hasPotionEffect(PotionEffectType.LEVITATION)) {
            Map<Integer, Double> R = new HashMap<Integer, Double>();
            R.put(0, p.getLocation().getY());
            HoverTicks.put(p, R);
            HackerGuardian.getInstance().EXEMPTHANDLER.addExemptionBlock(p, 20);
            return new CheckResult("Flight", false, "hover");
        } else {
            Map<Integer, Double> R = new HashMap<Integer, Double>();
            if (Count == 2)
                Count = 1;
            R.put(Count, p.getLocation().getY());
            HoverTicks.put(p, R);
            return new CheckResult("Flight", true, "pass");
        }
    }

}