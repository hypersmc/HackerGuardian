package me.hackerguardian.main.Tps.Modules;

import me.hackerguardian.main.Tps.Module;
import me.hackerguardian.main.Tps.ModuleResult;
import org.bukkit.event.Event;

public class RedstoneModule extends Module {
    @Override
    public String getName() {
        return "RedstoneModule";
    }

    @Override
    public String getEventCall() {
        return "BlockRedstoneEvent";
    }

    @Override
    public ModuleResult performCheck(Event e) {
        return null;
    }
}
