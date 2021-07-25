package me.hackerguardian.main.Tps.Modules;

import me.hackerguardian.main.Tps.Module;
import me.hackerguardian.main.Tps.ModuleResult;
import org.bukkit.event.Event;

public class LiquidModule extends Module {
    @Override
    public String getName() {
        return "LiquidModule";
    }

    @Override
    public String getEventCall() {
        return null;
    }

    @Override
    public ModuleResult performCheck(Event e) {
        return null;
    }
}
