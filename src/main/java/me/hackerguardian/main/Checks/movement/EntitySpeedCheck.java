package me.hackerguardian.main.Checks.movement;

import me.hackerguardian.main.Checks.Check;
import me.hackerguardian.main.Checks.CheckResult;
import me.hackerguardian.main.Checks.User;
import me.hackerguardian.main.Utils.JVelocity;
import me.hackerguardian.main.Utils.UtilMath;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;

public class EntitySpeedCheck extends Check {

    @Override
    public String getName() {
        return "EntitySpeedCheck";
    }

    @Override
    public String getEventCall() {
        return "PlayerMoveEvent";
    }

    @Override
    public CheckResult performCheck(User u, Event e) {
        PlayerMoveEvent event = (PlayerMoveEvent) e;
        Player p = u.getPlayer();

        if (!p.isInsideVehicle()) {
            return new CheckResult("EntitySpeedCheck", true, "pass");
        }

        Entity v = p.getVehicle();
        if (v.getType() == EntityType.MINECART) {
            return new CheckResult("EntitySpeedCheck", true, "pass");
        }

        JVelocity jv = new JVelocity(event.getFrom(), event.getTo());
        double limit = 5.75;
        if (jv.offset() > limit) {
            return new CheckResult("Entity Speed", false,
                    "vehicle moved at " + UtilMath.trim(2, jv.offset()) + " speed, max possible is " + limit);
        }

        return new CheckResult("EntitySpeedCheck", true, "pass");
    }

}