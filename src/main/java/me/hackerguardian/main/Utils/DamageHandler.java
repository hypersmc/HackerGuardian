package me.hackerguardian.main.Utils;

import me.hackerguardian.api.APICheck;
import me.hackerguardian.main.Checks.Check;
import me.hackerguardian.main.Checks.CheckResult;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.spigotmc.event.entity.EntityDismountEvent;
import me.hackerguardian.main.HackerGuardian;
import me.hackerguardian.main.MiniHandler;

import javax.naming.OperationNotSupportedException;

public class DamageHandler extends MiniHandler {
    public DamageHandler(HackerGuardian plugin) {
        super("Damage Handler", plugin);
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player)
            HackerGuardian.getInstance().EXEMPTHANDLER.addExemption((Player) event.getEntity(), 845, "damaged");
    }

    @EventHandler
    public void onEject(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            HackerGuardian.getInstance().EXEMPTHANDLER.addExemption(p, 500, "vehicle exempt");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) throws OperationNotSupportedException {

        if (event.getDamager() instanceof Player) {
            Player p = (Player) event.getDamager();
            for (Check c : this.getPlugin().All_Checks) {
                if (c.getEventCall().equals(event.getEventName())
                        || c.getSecondaryEventCall().equals(event.getEventName())) {
                    CheckResult result = c.performCheck(this.getPlugin().getUser(p), event);
                    String result2 = c.performCheck(this.getPlugin().getUser(p), event).getDesc();
                    if (!result.passed()) {
                        this.getPlugin().addSuspicion(p, result.getCheckName(), result2);
                    }
                }
            }
            for (APICheck c : this.getPlugin().All_Checks_API) {
                if (c.getEventCall().equals(event.getEventName())
                        || c.getSecondaryEventCall().equals(event.getEventName())) {
                    CheckResult result = c.performCheck(this.getPlugin().getUser(p), event);
                    String result2 = c.performCheck(this.getPlugin().getUser(p), event).getDesc();
                    if (!result.passed()) {
                        this.getPlugin().addSuspicion(p, result.getCheckName(), result2);
                    }
                }
            }
        }
    }
}
