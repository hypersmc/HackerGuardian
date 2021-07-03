package me.hackerguardian.main.Checks;

import me.hackerguardian.main.Core;
import me.hackerguardian.main.MiniHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockHandler extends MiniHandler {
    public BlockHandler(Core plugin) {
        super("Block Handler", plugin);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
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
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        for (Check c : this.getPlugin().All_Checks) {
            if (c.getEventCall().equals(event.getEventName())
                    || c.getSecondaryEventCall().equals(event.getEventName())) {
                try {
                    CheckResult result = c.performCheck(this.getPlugin().getUser(event.getPlayer()), event);
                    String result2 = c.performCheck(this.getPlugin().getUser(event.getPlayer()), event).getDesc();
                    if (!result.passed()) {
                        this.getPlugin().addSuspicion(event.getPlayer(), result.getCheckName(), result2);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
