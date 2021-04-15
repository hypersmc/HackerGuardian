package me.hackerguardian.api;

import me.hackerguardian.main.AdvancedLicense;
import me.hackerguardian.main.CommandManager;
import me.hackerguardian.main.getters.Commandlist;
import me.hackerguardian.main.getters.Timerlist;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Core extends JavaPlugin implements Listener {
    ConsoleCommandSender console = Bukkit.getConsoleSender();
    Logger logger = getLogger();
    public static Plugin plugin;
    private String prefix = "&4&l[&r&l&8HackerGuardian&r&4&l]&r ";
    private CommandManager commandManager;
    public static Core getInstance(){
        return Core.getPlugin(Core.class);
    }
    public static Plugin getPlugin() {
        return plugin;
    }

    /**
     *
     * @param text
     * @return will return the text argument with colors if (&) and color codes are used.
     */
    private String playertext(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @Override
    public void onEnable() {
        logger.info( ChatColor.BLUE + "\n" +
                "╔╗╔╗╔══╗╔═╗╔╦╗╔═╗╔═╗╔══╗╔╦╗╔══╗╔═╗╔══╗╔══╗╔══╗╔═╦╗\n" +
                "║╚╝║║╔╗║║╔╝║╔╝║╦╝║╬║║╔═╣║║║║╔╗║║╬║╚╗╗║╚║║╝║╔╗║║║║║\n" +
                "║╔╗║║╠╣║║╚╗║╚╗║╩╗║╗╣║╚╗║║║║║╠╣║║╗╣╔╩╝║╔║║╗║╠╣║║║║║\n" +
                "╚╝╚╝╚╝╚╝╚═╝╚╩╝╚═╝╚╩╝╚══╝╚═╝╚╝╚╝╚╩╝╚══╝╚══╝╚╝╚╝╚╩═╝");
        logger.info("Registering Commands");
        commandManager = new CommandManager(this, "hackerguardian");
        Commandlist.getcommands();
        registerCommand();
        logger.info("Registering Timers");
        Timerlist.Timerlist();

        if(!new AdvancedLicense("HFDC-626Y-B1QR-981H", "https://zennodes.dk/ActivationKey/verify.php", this).setSecurityKey("TkHr6umrQ8OUPxeWt0ANuXa3zlTopUyF7nYUJaahbZMJAoRZOOzcLjCTG70zVJeDKavfJOC0ilD56Ll6MSV7PFVKkwaMpgwmnk4").register()) return;

    }

    @Override
    public void onDisable() {
        logger.info( ChatColor.BLUE + "\n" +
                "╔╗╔╗╔══╗╔═╗╔╦╗╔═╗╔═╗╔══╗╔╦╗╔══╗╔═╗╔══╗╔══╗╔══╗╔═╦╗\n" +
                "║╚╝║║╔╗║║╔╝║╔╝║╦╝║╬║║╔═╣║║║║╔╗║║╬║╚╗╗║╚║║╝║╔╗║║║║║\n" +
                "║╔╗║║╠╣║║╚╗║╚╗║╩╗║╗╣║╚╗║║║║║╠╣║║╗╣╔╩╝║╔║║╗║╠╣║║║║║\n" +
                "╚╝╚╝╚╝╚╝╚═╝╚╩╝╚═╝╚╩╝╚══╝╚═╝╚╝╚╝╚╩╝╚══╝╚══╝╚╝╚╝╚╩═╝");
    }

    private void registerCommand(){
        commandManager.register("", (sender, params) -> {
            if (sender.hasPermission("hg.main") || sender.hasPermission("hg.*")) {
                sender.sendMessage(playertext("&8&l<&7&m-------------]&r&l&4Help&7&m[-------------&r&8&l>"));
                sender.sendMessage(playertext(prefix + "Command list:"));
            }else {
                sender.sendMessage("Unknown command. Type \"/help\" for help.\n");
            }

        });
        commandManager.register("help", (sender, params) -> {
            if (params.length != 1) {
                sender.sendMessage(playertext("&8&l<&7&m-------------]&r&l&4Help&7&m[-------------&r&8&l>"));
                sender.sendMessage(playertext(prefix + "Command list:"));

                sender.sendMessage(playertext("&8&l<&7&m---------]&r&l&4Menu 1/4&7&m[---------&r&8&l>"));
            }
            if (params.length == 1){
                if (!StringUtils.isNumeric(params[0])) {
                    sender.sendMessage(playertext("nope!"));
                    return;
                }
                int helpnumber = Integer.valueOf(params[0]);
                if (helpnumber == 1){
                    sender.sendMessage(playertext("1"));
                }else if (helpnumber == 2){
                    sender.sendMessage(playertext("2"));
                }
            }
        });
    }
}
