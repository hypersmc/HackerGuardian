package me.hackerguardian.api;

import me.hackerguardian.main.Checks.CheckResult;
import me.hackerguardian.main.Checks.User;
import org.bukkit.event.Event;

public abstract class APICheck {
    /**
     *
     * @return name of your check. Example: BoatFlyCheck. Advanced or simple names are accepted.
     */
    public abstract String getName();

    /**
     *
     * @return With use of either: PlayerMoveEvent, EntityDamageByEntityEvent, BlockBreakEvent, BlockPlaceEvent
     */
    public abstract String getEventCall();

    /**
     *
     * @param u u is the user. Use "Player p = u.getPlayer();" to get the player with p.
     * @param e e is the event. This can be all sorts of events you wanna check or do.
     */
    public abstract CheckResult performCheck(User u, Event e);

    public CheckResult performCheck(User u) {
        return performCheck(u, null);
    }
    /**
     *
     * @return Can be used to check more then 1 event at a time. Same in use in getEventCall()
     * @see APICheck#getEventCall()
     */
    public String getSecondaryEventCall() {
        return "";
    }

}
