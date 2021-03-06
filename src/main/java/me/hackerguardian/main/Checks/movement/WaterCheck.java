package me.hackerguardian.main.Checks.movement;

import me.hackerguardian.main.Checks.Check;
import me.hackerguardian.main.Checks.CheckResult;
import me.hackerguardian.main.Checks.User;
import me.hackerguardian.main.Utils.UtilBlock;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.HashMap;
import java.util.Map;

public class WaterCheck extends Check {

    @Override
    public String getName() {
        return "JesusCheck";
    }

    @Override
    public String getEventCall() {
        return "PlayerMoveEvent";
    }

    private static Map<Player, Long> lastcheck = new HashMap<Player, Long>();
    private static Map<Player, Integer> count = new HashMap<Player, Integer>();

    @Override
    public CheckResult performCheck(User u, Event ev) {
        Player p = u.getPlayer();
        if (p.isFlying() || p.isInsideVehicle()) {
            return new CheckResult("WaterWalk", true, "flying above water");
        }
        if (!lastcheck.containsKey(p)) {
            lastcheck.put(p, 0L);
            count.put(p, 0);
        }
        long math = (System.currentTimeMillis() - lastcheck.get(p));
        if (math > 450) {
            int oc = count.get(p);
            boolean waterwalk = true;
            if (p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.WATER) {
                return new CheckResult("WaterWalk", true, "player isn't above water");
            }
            if (p.getLocation().getBlock().isLiquid() || p.isSwimming()) {
                waterwalk = false;
                count.put(p, 0);
                return new CheckResult("WaterWalk", true, "player is swimming");
            }
            for (Material m : UtilBlock.getSurroundingMat(p.getLocation().getBlock(), true)) {
                if (m != Material.WATER && m != Material.AIR) {
                    waterwalk = false;
                }
            }
            if (waterwalk) {
                oc++;
                count.put(p, oc);
            } else {
                count.put(p, 0);
            }
            lastcheck.put(p, System.currentTimeMillis());
            if (oc > 12) {
                return new CheckResult("WaterWalk", false, "floating above, not swimming");
            }
        }

        return new CheckResult("WaterWalk", true, "pass");

    }

}