package me.hackerguardian.main.Checks;

import org.bukkit.event.Event;

import javax.naming.OperationNotSupportedException;

//This can't be used on the API.
public abstract class Check {
    public abstract String getName();

    public abstract String getEventCall();

    public abstract CheckResult performCheck(User u, Event e) throws OperationNotSupportedException;

    public CheckResult performCheck(User u) throws OperationNotSupportedException {
        return performCheck(u, null);
    }

    public String getSecondaryEventCall() {
        return "";
    }
}
