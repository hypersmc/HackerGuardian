package me.hackerguardian.main.Checks.movement;
import me.hackerguardian.main.Checks.Check;
import me.hackerguardian.main.Checks.CheckResult;
import me.hackerguardian.main.Checks.User;
import me.hackerguardian.main.HackerGuardian;
import me.hackerguardian.main.Utils.JVelocity;
import me.hackerguardian.main.Utils.UtilBlock;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;
public class SmartSpeedCheck extends Check {

    @Override
    public String getName() {
        return "SmartSpeedCheck";
    }

    @Override
    public String getEventCall() {
        return "PlayerMoveEvent";
    }

    @Override
    public CheckResult performCheck(User u, Event e) {
        PlayerMoveEvent event = (PlayerMoveEvent) e;
        Player p = u.getPlayer();
        JVelocity jv = new JVelocity(event.getFrom(), event.getTo());
        double x = jv.xoffset();
        double y = jv.yoffset();
        double z = jv.zoffset();

        double mxz = 0.672;
        double my = 0.76;

        double lxz = -0.672;

        if (p.isInsideVehicle() || p.isFlying() || p.isGliding() || p.getGameMode() == GameMode.CREATIVE
                || p.getGameMode() == GameMode.SPECTATOR || HackerGuardian.getInstance().getUser(p).isBouncing())
            return new CheckResult("SmartSpeed", true, "pass");

        if (p.hasPotionEffect(PotionEffectType.JUMP)) {
            my += 0.10 * p.getPotionEffect(PotionEffectType.JUMP).getAmplifier();
        }

        if (p.hasPotionEffect(PotionEffectType.SPEED)) {
            mxz += 0.60 * p.getPotionEffect(PotionEffectType.SPEED).getAmplifier();
            lxz += -0.60 * p.getPotionEffect(PotionEffectType.SPEED).getAmplifier();
        }

        if (p.getLocation().getBlock().getType().toString().contains("STAIRS")
                || p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().toString().contains("STAIRS")) {
            my += 0.75;
        }

        if (p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().toString().contains("ICE")
                || p.getLocation().getBlock().getRelative(0, -2, 0).getType().toString().contains("ICE")) {
            mxz += 0.375;
            lxz -= 0.375;
            my += 0.25;
        }

        if (HackerGuardian.getInstance().EXEMPTHANDLER.isExempt(p)) {
            return new CheckResult("SmartSpeed", true, "pass");
        }

        boolean speed = false;
        if (x < 0 && Math.abs(x) > Math.abs(lxz)) {
            speed = true;
        }
        if (z < 0 && Math.abs(z) > Math.abs(lxz)) {
            speed = true;
        }
        if (x > mxz) {
            speed = true;
        }
        if (y > my) {
            if (event.getTo().add(0, -0.173, 0).getBlock().getType() != Material.AIR) {
                return new CheckResult("Step", false,
                        "y increased too fast up a block (" + y + "), max possible: " + my);
            }
            return new CheckResult("Flight", false, "y increased too fast (" + y + "), max possible: " + my);
        }
        if (z > mxz) {
            speed = true;
        }

        if (speed) {
            if (UtilBlock.getSurroundingIgnoreAir(p.getLocation().getBlock(), true).size() == 0) {
                return new CheckResult("Flight", false, "impossible move, no fall");
            } else {
                return new CheckResult("Speed", false, "movement speed to high");
            }
        }

        return new CheckResult("Speed/Flight", true, "pass");
    }

}