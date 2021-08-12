package me.hackerguardian.main;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.UnsupportedEncodingException;

public class ProtocollibListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(String channel, Player p, byte[] msg) {
        try {
            MySQL sql = new MySQL();
            sql.setUser(p.getUniqueId(), new String(msg, "UTF-8").substring(1));
        } catch (UnsupportedEncodingException e) {
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
    }
}