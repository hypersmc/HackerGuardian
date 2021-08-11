package me.hackerguardian.api;

import me.hackerguardian.main.HackerGuardian;
import org.bukkit.plugin.java.JavaPlugin;

public interface APIRegCheck {

    /**
     *
     * @param check adds the check to a list. Just remember to do this action in onEnable()
     * @see JavaPlugin#onEnable()
     */
    default void registerCheck(APICheck check) {
        if (!HackerGuardian.getInstance().All_Checks_API.contains(check))
            HackerGuardian.getInstance().All_Checks_API.add(check);
    }
}
