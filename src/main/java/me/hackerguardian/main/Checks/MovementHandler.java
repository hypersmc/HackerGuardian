package me.hackerguardian.main.Checks;

import me.hackerguardian.api.APICheck;
import me.hackerguardian.main.HackerGuardian;
import me.hackerguardian.main.MiniHandler;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRiptideEvent;

import javax.naming.OperationNotSupportedException;

public class MovementHandler extends MiniHandler {
    public MovementHandler(HackerGuardian plugin) {
        super("Movement Handler", plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) throws OperationNotSupportedException {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE
                || event.getPlayer().getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        if (!this.getPlugin().EXEMPTHANDLER.isExempt(event.getPlayer())) {
            for (Check c : this.getPlugin().All_Checks) {
                if (c.getEventCall().equals(event.getEventName())
                        || c.getSecondaryEventCall().equals(event.getEventName())) {
                    CheckResult result = c.performCheck(this.getPlugin().getUser(event.getPlayer()), event);
                    String result2 = c.performCheck(this.getPlugin().getUser(event.getPlayer()), event).getDesc();
                    if (!result.passed()) {
                        this.getPlugin().addSuspicion(event.getPlayer(), result.getCheckName(), result2);
                    }
                }
            }
            for (APICheck c : this.getPlugin().All_Checks_API) {
                if (c.getEventCall().equals(event.getEventName())
                        || c.getSecondaryEventCall().equals(event.getEventName())) {
                    CheckResult result = c.performCheck(this.getPlugin().getUser(event.getPlayer()), event);
                    String result2 = c.performCheck(this.getPlugin().getUser(event.getPlayer()), event).getDesc();
                    if (!result.passed()) {
                        this.getPlugin().addSuspicion(event.getPlayer(), result.getCheckName(), result2);
                    }
                }
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void riptide(PlayerRiptideEvent event) {
        this.getPlugin().EXEMPTHANDLER.addExemption(event.getPlayer(), 2000, "Riptide");
    }
}
