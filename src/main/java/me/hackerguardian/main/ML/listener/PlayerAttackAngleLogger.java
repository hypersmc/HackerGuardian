package me.hackerguardian.main.ML.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.*;

public class PlayerAttackAngleLogger implements Listener {

    private Set<String> registeredPlayers = new HashSet<>();

    private Map<String, List<Float>> loggedAngles = new HashMap<>();

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event){
        if (!(event.getDamager() instanceof Player))
            return;
        if (!registeredPlayers.contains(event.getDamager().getName()))
            return;

        Player player = (Player) event.getDamager();
        Entity entity = event.getEntity();
        org.bukkit.util.Vector playerLookDir = player.getEyeLocation().getDirection();
        org.bukkit.util.Vector playerEyeLoc = player.getEyeLocation().toVector();
        org.bukkit.util.Vector entityLoc = entity.getLocation().toVector();
        Vector playerEntityVec = entityLoc.subtract(playerEyeLoc);
        float angle = playerLookDir.angle(playerEntityVec);

        if (!loggedAngles.containsKey(player.getName()))
            loggedAngles.put(player.getName(), new ArrayList<>());
        loggedAngles.get(player.getName()).add(angle);
    }

    public void registerPlayer(Player player) {
        registeredPlayers.add(player.getName());
    }
    public void unregisterPlayer(Player player) {
        registeredPlayers.remove(player.getName());
    }
    public Set<String> getRegisteredPlayers() {
        return registeredPlayers;
    }
    public List<Float> getLoggedAngles(Player player) {
        return loggedAngles.get(player.getName());
    }
    public void clearLoggedAngles(Player player) {
        loggedAngles.remove(player.getName());
    }
}
