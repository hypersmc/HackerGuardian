package me.hackerguardian.main;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

public class MiniHandler implements Listener {

    protected String _moduleName = "Default";

    protected Core _plugin;

    public MiniHandler(String moduleName, Core plugin) {
        this._moduleName = moduleName;
        this._plugin = plugin;
        onEnable();

        registerEvents(this);
    }

    public void runASync(Runnable r) {
        Bukkit.getScheduler().runTaskAsynchronously(this._plugin, r);
    }

    public PluginManager getPluginManager() {
        return this._plugin.getServer().getPluginManager();
    }

    public BukkitScheduler getScheduler() {
        return this._plugin.getServer().getScheduler();
    }

    public Core getPlugin() {
        return this._plugin;
    }

    public Server getServer() {
        return this._plugin.getServer();
    }

    public void registerEvents(Listener listener) {
        this._plugin.getServer().getPluginManager().registerEvents(listener, this._plugin);
    }

    public final void onEnable() {
    }

    public final void onDisable() {
    }


    public final String getName() {
        return this._moduleName;
    }

    protected void log(String message) {
        Bukkit.getConsoleSender().sendMessage(message);
    }
}
