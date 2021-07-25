package me.hackerguardian.main.Tps;

import me.hackerguardian.main.Checks.CheckResult;
import me.hackerguardian.main.Checks.User;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

public abstract class Module implements Listener {
    public abstract String getName();
    public abstract String getEventCall();
    public abstract ModuleResult performCheck(Event e);

    public ModuleResult performCheck() {
        return performCheck(null);
    }
}
