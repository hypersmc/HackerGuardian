package me.hackerguardian.main.Checks.movement;
import me.hackerguardian.main.Checks.Check;
import me.hackerguardian.main.Checks.CheckResult;
import me.hackerguardian.main.Checks.User;
import me.hackerguardian.main.HackerGuardian;
import me.hackerguardian.main.Utils.UtilBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
public class BoatCheck extends Check {

    @Override
    public String getName() {
        return "BoatFlyCheck";
    }

    @Override
    public String getEventCall() {
        return "PlayerMoveEvent";
    }

    @Override
    public CheckResult performCheck(User u, Event e) {
        if (u.getBlock().isLiquid() || u.getBlock().getType().toString().toLowerCase().contains("carpet"))
            return new CheckResult("Boat Fly", true, "pass");

        if (u.InVehicle() && u.getVehicle().getType() == EntityType.BOAT) {
            PlayerMoveEvent ev = (PlayerMoveEvent) e;
            if (u.getVehicleBlock().getType() == Material.AIR && !UtilBlock.onBlock(u.getVehicle().getLocation())
                    && UtilBlock.getSurroundingIgnoreAir(u.getVehicleBlock(), true).size() == 0) {
                Location LastSafe = u.LastNormalBoatLoc();
                if (ev.getTo().getY() < ev.getFrom().getY()) {
                    return new CheckResult("Boat Fly", true, "pass");
                }
                if (LastSafe != null) {
                    HackerGuardian.getInstance().EXEMPTHANDLER.addExemptionBlock(u.getPlayer(), 5);
                    Entity v = u.getVehicle();
                    v.teleport(LastSafe);
                } else {
                    u.eject();
                }
                return new CheckResult("Boat Fly", false, "player is around no blocks");
            }
        }
        return new CheckResult("Boat Fly", true, "pass");
    }

}