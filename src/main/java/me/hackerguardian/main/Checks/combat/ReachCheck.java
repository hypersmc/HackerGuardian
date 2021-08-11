package me.hackerguardian.main.Checks.combat;
import me.hackerguardian.main.Checks.Check;
import me.hackerguardian.main.Checks.CheckResult;
import me.hackerguardian.main.Checks.User;
import me.hackerguardian.main.HackerGuardian;
import me.hackerguardian.main.Tps.Tps;
import me.hackerguardian.main.Utils.UtilMath;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
public class ReachCheck extends Check {

    @Override
    public String getName() {
        return "ReachCheck";
    }

    @Override
    public String getEventCall() {
        return "EntityDamageByEntityEvent";
    }

    @Override
    public CheckResult performCheck(User u, Event e) {
        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;
        double range = event.getEntity().getLocation().distance(event.getDamager().getLocation());
        String rf = range + "";
        try {
            range = Double.parseDouble(rf.substring(0, 4));
        } catch (Exception exception) {

        }
        HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage("range " + UtilMath.trim(2, range));
        if ((range > 3.48 && !u.getPlayer().isSprinting()) || (Tps.getTPS() < 10) || range > 4.98 && u.getPlayer().isSprinting()) {
            return new CheckResult("Combat Reach", false, "hit at a range of " + UtilMath.trim(2, range));
        }
        return new CheckResult("Combat Reach", true, "pass");
    }
}