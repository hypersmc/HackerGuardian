package me.hackerguardian.main.Checks.world;


import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.hackerguardian.main.Checks.Check;
import me.hackerguardian.main.Checks.CheckResult;
import me.hackerguardian.main.Checks.User;
import me.hackerguardian.main.HackerGuardian;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import javax.naming.OperationNotSupportedException;
import java.util.*;

public class TimerCheck extends Check {
    private Map<UUID, List<Long>> times = new HashMap<>();
    boolean added = false;
    boolean check = false;
    double finals;

    @Override
    public String getName() {
        return "TimerCheck1";
    }

    @Override
    public String getEventCall() {
        return "PlayerMoveEvent";
    }

    @Override
    public CheckResult performCheck(User u, Event e) throws OperationNotSupportedException {
        Player player = u.getPlayer();
        if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) throw new OperationNotSupportedException("ProtocolLib is not enabled or installed!");
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        PacketAdapter adapter = new PacketAdapter(HackerGuardian.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.POSITION) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (player != null) {
                    check = false;
                    List<Long> ts = times.getOrDefault(player.getUniqueId(), new ArrayList<>());
                    Iterator<Long> it = ts.iterator();
                    double avg = 0;
                    long last = System.currentTimeMillis();
                    while (it.hasNext()) {
                        long l = it.next();
                        if (System.currentTimeMillis() - l > 10000)
                            it.remove();
                        else {
                            avg += last - l;
                            last = l;
                        }
                    }
                    avg /= ts.size();
                    ts.add(0, System.currentTimeMillis());
                    times.put(player.getUniqueId(), ts);
                    if (avg >= 55) {
                        return;
                    }
                    finals = avg;
                    check = true;
                }
            }
            @Override
            public void onPacketSending(PacketEvent event) {
            }
        };

        if (check == true){
            check = false;
            check = false;
            check = false;
            return new CheckResult("Timer1", false, "MorePackets (Timer) with avg: " + finals);
        }
        if (!added) {
            added = true;
            manager.addPacketListener(adapter);
        }
        check = false;
        System.out.println(finals);
        if (finals > 55) {
            return new CheckResult("Timer1", false, "MorePackets (Timer) with avg: " + finals);
        }

        return new CheckResult("Timer1", true, "pass");
    }
}
