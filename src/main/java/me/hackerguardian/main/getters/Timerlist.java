package me.hackerguardian.main.getters;

import me.hackerguardian.main.Core;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class Timerlist {

    public static void Timerlist(){
        //signalTimer();
        //signalTimer2();
        //signalTimer3();
        //signalTimer4();
        //signalTimer5();
    }
    /*public static void signalTimer(){
        new BukkitRunnable(){
            public void run(){
                Bukkit.broadcastMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "[HackerGuardian announcement]");
                Bukkit.broadcastMessage("" + "I have banned " + ChatColor.RED  + ChatColor.BOLD + Main.getInstance().getSystemban() + ChatColor.RESET + " Players in the last 5 days." );
                Bukkit.broadcastMessage("" + "Staff have banned an additional " + ChatColor.RED + ChatColor.BOLD + Main.getInstance().getAdminban() + ChatColor.RESET + " Players in the last 5 days!");
                Bukkit.broadcastMessage("Banwaves happens every few hours!");
            } //20 = one second
        }.runTaskTimer(Core.getInstance(), 20 * 2400, 20 * 2400); //.runTaskTimer(Core.getInstance(), 20 * 2400, 20 * 2400);
    }
    public static void signalTimer2(){
        new BukkitRunnable(){
            public void run(){
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "banwave ban");
                Bukkit.getLogger().info("Banwave has just finished.");
            } //20 = one second
        }.runTaskTimer(Core.getInstance(), 20 * 3900, 20 * 3900);
    }
    public static void signalTimer3(){
        new BukkitRunnable(){
            public void run(){
                if (Bukkit.getServer().getOnlinePlayers().size() > 0) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "slr test " + selectplayer.randomPlayer().getName() + " 10");
                    Bukkit.getLogger().info("Started testing random selected player for hacking!");
                }else {
                    Bukkit.getLogger().info("Can't test anyone as of the server is empty.");
                }
            } //20 = one second
        }.runTaskTimer(Core.getInstance(), 20 * 3900, 20 * 3900);
    }
    public static void signalTimer4(){
        new BukkitRunnable(){
            public void run(){
                Bukkit.getLogger().info("Resetting Admin and System bans");
            }
        }.runTaskTimer(Core.getInstance(), 20 * 432000, 20 * 432000);
    }
    public static void signalTimer5(){
        new BukkitRunnable(){
            public void run(){
                Bukkit.broadcastMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "[HackerGuardian announcement]");
                Bukkit.broadcastMessage("" + "Need to report a player? Use:" );
                Bukkit.broadcastMessage("" + "/hg report player reason");
            }
        }.runTaskTimer(Core.getInstance(), 20 * 1950, 20 * 1950);
    }*/
}
