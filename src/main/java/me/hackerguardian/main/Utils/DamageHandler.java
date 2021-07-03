package me.hackerguardian.main.Utils;

import me.hackerguardian.main.Checks.Check;
import me.hackerguardian.main.Checks.CheckResult;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.spigotmc.event.entity.EntityDismountEvent;
import me.hackerguardian.main.Core;
import me.hackerguardian.main.MiniHandler;

public class DamageHandler extends MiniHandler {
    public DamageHandler(Core plugin) {
        super("Damage Handler", plugin);
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player)
            Core.getInstance().EXEMPTHANDLER.addExemption((Player) event.getEntity(), 845, "damaged");
    }

    @EventHandler
    public void onEject(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            Core.getInstance().EXEMPTHANDLER.addExemption(p, 500, "vehicle exempt");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {

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
        }
    }
}
