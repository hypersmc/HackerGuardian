package me.hackerguardian.main;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getServer().getLogger().info( "\n" + "╔╗╔╗╔══╗╔═╗╔╦╗╔═╗╔═╗╔══╗╔╦╗╔══╗╔═╗╔══╗╔══╗╔══╗╔═╦╗\n" +
                "║╚╝║║╔╗║║╔╝║╔╝║╦╝║╬║║╔═╣║║║║╔╗║║╬║╚╗╗║╚║║╝║╔╗║║║║║\n" +
                "║╔╗║║╠╣║║╚╗║╚╗║╩╗║╗╣║╚╗║║║║║╠╣║║╗╣╔╩╝║╔║║╗║╠╣║║║║║\n" +
                "╚╝╚╝╚╝╚╝╚═╝╚╩╝╚═╝╚╩╝╚══╝╚═╝╚╝╚╝╚╩╝╚══╝╚══╝╚╝╚╝╚╩═╝");


        if(!new AdvancedLicense("HFDC-626Y-B1QR-981H", "https://zennodes.dk/ActivationKey/verify.php", this).setSecurityKey("TkHr6umrQ8OUPxeWt0ANuXa3zlTopUyF7nYUJaahbZMJAoRZOOzcLjCTG70zVJeDKavfJOC0ilD56Ll6MSV7PFVKkwaMpgwmnk4").register()) return;

    }

}
