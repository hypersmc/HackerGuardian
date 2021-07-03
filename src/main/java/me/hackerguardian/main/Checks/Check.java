package me.hackerguardian.main.Checks;

import org.bukkit.event.Event;

public abstract class Check {
    public abstract String getName();

    public abstract String getEventCall();

    public abstract CheckResult performCheck(User u, Event e);

    public CheckResult performCheck(User u) {
        return performCheck(u, null);
    }

    public String getSecondaryEventCall() {
        return "";
    }
}
