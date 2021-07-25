package me.hackerguardian.main;

import com.sun.org.apache.xerces.internal.xs.StringList;
import me.hackerguardian.main.Checks.combat.CriticalCheck;
import me.hackerguardian.main.Checks.combat.KillAuraCheck;
import me.hackerguardian.main.Checks.combat.MultiAuraCheck;
import me.hackerguardian.main.Checks.combat.ReachCheck;
import me.hackerguardian.main.Checks.movement.*;
import me.hackerguardian.main.Checks.world.AntiCactusBerryCheck;
import me.hackerguardian.main.Checks.world.BreakCheck;
import me.hackerguardian.main.Checks.world.PlaceCheck;
import me.hackerguardian.main.Checks.world.XRayCheck;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MySQL {

    public static Connection db = null;
    private String host = Core.getInstance().getConfig().getString("SQLHost");
    private String port = Core.getInstance().getConfig().getString("SQLPort");
    private String database = Core.getInstance().getConfig().getString("SQLDatabaseName");
    private String user = Core.getInstance().getConfig().getString("SQLUsername");
    private String pass = Core.getInstance().getConfig().getString("SQLPassword");
    //TODO Add ny liste så man kan se hvad spillern sidst er blivet "kicket" for af checks.
    public void setupCoreSystem(){
        String url = null;
        if (this.user.equals("changeme") && this.pass.equals("changeme")){
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "");
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "---------- Core MySQL ----------");
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + " Please setup MySQL in the config. When done reboot the server.");
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + " Disabling plugin. Please reboot to reload config.");
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "-----------------------------");
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "");
            Bukkit.getPluginManager().disablePlugin(Core.getInstance());
            return;
        }
        try {
            String driver = "com.mysql.jdbc.Driver";
            url = "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?user=" + this.user + "&password=" + this.pass + "?autoReconnect=true?useUnicode=yes";
            Class.forName(driver);
            String finalUrl = url;
            Bukkit.getScheduler().runTaskAsynchronously(Core.getInstance(), () -> {
                try {
                    db = DriverManager.getConnection(finalUrl, this.user, this.pass);
                    formatCoreDatabase();

                } catch (SQLException e) {
                    Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Could not connect to the '" + this.database + "' Database");
                    if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
                }
            });

            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Connection to MySQL database successful.");
        } catch (Exception e) {
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Could not connect to the '" + this.database + "' Database");
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Info: " + url);
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
    }
    /*
    Website basted
     */
    public static void checkWebsiteContent(){
        String host = Core.getInstance().getConfig().getString("SQLHost");
        String port = Core.getInstance().getConfig().getString("SQLPort");
        String database = Core.getInstance().getConfig().getString("SQLDatabaseName");
        String user = Core.getInstance().getConfig().getString("SQLUsername");
        String pass = Core.getInstance().getConfig().getString("SQLPassword");
        String url = null;
    }
    /*
    END
     */
    public static void checkdbconnection() {
        String host = Core.getInstance().getConfig().getString("SQLHost");
        String port = Core.getInstance().getConfig().getString("SQLPort");
        String database = Core.getInstance().getConfig().getString("SQLDatabaseName");
        String user = Core.getInstance().getConfig().getString("SQLUsername");
        String pass = Core.getInstance().getConfig().getString("SQLPassword");
        String url = null;
        try {
            String driver = "com.mysql.jdbc.Driver";
            url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?user=" + user + "&password=" + pass + "?autoReconnect=true?useUnicode=yes";
            Class.forName(driver);

            String finalUrl = url;
            Bukkit.getScheduler().runTaskAsynchronously(Core.getInstance(), () -> {
                try {
                    db = DriverManager.getConnection(finalUrl, user, pass);

                } catch (SQLException e) {
                    Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Could not connect to the '" + database + "' Database");
                    if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
                }
            });

        } catch (Exception e) {
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Could not connect to the '" + database + "' Database");
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Info: " + url);
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
    }
    public void shutdowndatabase(){
        try {
            db.close();
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "MysQL database connection closed.");
        } catch (SQLException e) {
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Failed to close connection.");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
    }
    public void formatCoreDatabase(){
        PreparedStatement coreps = null;
        PreparedStatement core2 = null;
        PreparedStatement pip = null;
        PreparedStatement Reports = null;
        PreparedStatement Comments = null;
        PreparedStatement Flags = null;
        PreparedStatement triggers = null;
        PreparedStatement othereasons = null;
        //
        try {
            coreps = db.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.database + ".CorePlayerStats(`PlayerUUID` VARCHAR(64) NOT NULL, `LastKnownclient` VARCHAR(64) NULL, PRIMARY KEY (`PlayerUUID`));");
            core2 = db.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.database + ".Playerstats(`PlayerUUID` VARCHAR(64) NOT NULL, `Banned` VARCHAR(5) NULL, `Mutetimes` INT NULL, `Kicktimes` INT NULL, `Inbanwave` VARCHAR(5) NULL, `Ismuted` VARCHAR(5) NULL, `jointime` VARCHAR(64) NULL, PRIMARY KEY (`PlayerUUID`));");
            pip = db.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.database + ".PlayerIPTable(`PlayerUUID` VARCHAR(64) NOT NULL, `IP` VARCHAR(64) NULL, FOREIGN KEY (`PlayerUUID`) REFERENCES Playerstats(`PlayerUUID`));");
            Reports = db.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.database + ".Reports(`ID` INT(8) NOT NULL AUTO_INCREMENT, `ReportedUUID` VARCHAR(45) NULL, `ReportedByUUID` VARCHAR(45) NULL, `Reason` MEDIUMTEXT NULL, `Date` DATETIME NULL, PRIMARY KEY (`ID`));");
            Comments = db.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.database + ".Comments(`ID` INT(8) NOT NULL AUTO_INCREMENT, `ConnectedID` VARCHAR(45) NULL, `CommenterUUID` VARCHAR(45) NULL, `CommentText` MEDIUMTEXT NULL, `CommentDate` DATETIME NULL, PRIMARY KEY (`ID`));");
            Flags = db.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.database + ".Flags(`ID` INT(8) NOT NULL AUTO_INCREMENT, `ConnectedID` VARCHAR(45) NULL, `Flag` VARCHAR(200) NULL, `UUID` VARCHAR(60) NULL, `Date` DATETIME NULL, PRIMARY KEY (`ID`));");
            triggers = db.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.database + ".Triggers(`PlayerUUID` VARCHAR(64) NOT NULL, `Reason` VARCHAR(64) NOT NULL, FOREIGN KEY (`PlayerUUID`) REFERENCES Playerstats(`PlayerUUID`));");
            othereasons = db.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.database + ".OtherReasons(`PlayerUUID` VARCHAR(64) NOT NULL, `Handler` VARCHAR(64) NOT NULL, `Reason` VARCHAR(64) NOT NULL, FOREIGN KEY (`PlayerUUID`) REFERENCES Playerstats(`PlayerUUID`));");
            coreps.executeUpdate();
            core2.executeUpdate();
            pip.executeUpdate();
            Reports.executeUpdate();
            Comments.executeUpdate();
            Flags.executeUpdate();
            triggers.executeUpdate();
            othereasons.executeUpdate();
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Successfully created/passed tables.");
        } catch (Exception e) {
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error creating Core system SQL tables.");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
    }

    public Object getbannedip(String ip) {
        PreparedStatement bannedCount = null;
        ResultSet firesult = null;
        checkdbconnection();
        try {
            bannedCount = db.prepareStatement("SELECT COUNT(Playerstats.Banned) AS Banned FROM " + this.database + ".Playerstats INNER JOIN PlayerIPTable ON PlayerIPTable.PlayerUUID = Playerstats.PlayerUUID WHERE PlayerIPTable.IP = '/" + ip + "' && Playerstats.Banned='true'");
            firesult = bannedCount.executeQuery();
            if (firesult.next()) {
                return firesult.getString(1);
            }

            //return firesult.getInt(1);
        } catch (Exception e) {
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error getting ip count!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }

    public List<String> getPlayerTriggers(UUID playeruuid) {
        PreparedStatement first = null;
        PreparedStatement second = null;
        ResultSet firesult = null;
        checkdbconnection();
        try {
            second = db.prepareStatement("SELECT * FROM " + this.database + ".Triggers WHERE PlayerUUID='" + playeruuid + "' ORDER BY 'Reason' DESC LIMIT " + Core.getInstance().getConfig().getInt("Settings.MaxReasonListCount") + ";");
            firesult = second.executeQuery();
            List<String> stringArray = new ArrayList<String>();
            stringArray.clear();
            while (firesult.next()) {
                stringArray.add(firesult.getString("Reason"));
            }
            return stringArray;
        } catch (Exception e) {
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error getting player triggers!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }

    public List<String> getPlayerhandlerReasons(UUID playeruuid) {
        PreparedStatement first = null;
        PreparedStatement second = null;
        ResultSet firesult = null;
        checkdbconnection();
        try {
            second = db.prepareStatement("SELECT * FROM " + this.database + ".OtherReasons WHERE PlayerUUID='" + playeruuid + "' ORDER BY 'Reason' DESC LIMIT " + Core.getInstance().getConfig().getInt("Settings.MaxHandlerListCount") + ";");
            firesult = second.executeQuery();
            List<String> stringArray = new ArrayList<String>();
            stringArray.clear();
            while (firesult.next()) {
                stringArray.add(firesult.getString("Reason"));
            }
            return stringArray;
        } catch (Exception e) {
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error getting player triggers!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }
    public List<String> getPlayerhandler(UUID playeruuid) {
        PreparedStatement first = null;
        PreparedStatement second = null;
        ResultSet firesult = null;
        checkdbconnection();
        try {
            second = db.prepareStatement("SELECT * FROM " + this.database + ".OtherReasons WHERE PlayerUUID='" + playeruuid + "' ORDER BY 'Handler' DESC LIMIT " + Core.getInstance().getConfig().getInt("Settings.MaxHandlerListCount") + ";");
            firesult = second.executeQuery();
            List<String> stringArray = new ArrayList<String>();
            stringArray.clear();
            while (firesult.next()) {
                stringArray.add(firesult.getString("Handler"));
            }
            return stringArray;
        } catch (Exception e) {
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error getting player triggers!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }


    public List<String> getPlayerIp(UUID playeruuid){
        PreparedStatement first = null;
        PreparedStatement second = null;
        ResultSet firesult = null;
        checkdbconnection();
        try {
            second = db.prepareStatement("SELECT * FROM " + this.database + ".PlayerIPTable WHERE PlayerUUID='" + playeruuid + "' ORDER BY 'IP' DESC LIMIT " + Core.getInstance().getConfig().getInt("Settings.MaxIPListCount") +";");
            firesult = second.executeQuery();
            List<String> stringArray = new ArrayList<String>();
            stringArray.clear();
            while (firesult.next()){
                stringArray.add(firesult.getString("IP"));
            }
            return stringArray;
        }catch (Exception e){
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error getting player IP!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }
    public String getuser(UUID playeruuid){
        PreparedStatement first = null;
        PreparedStatement second = null;
        ResultSet firesult = null;
        checkdbconnection();
        try {
            second = db.prepareStatement("SELECT * FROM " + this.database + ".CorePlayerStats WHERE PlayerUUID='" + playeruuid + "';");
            firesult = second.executeQuery();
            while (firesult.next()){
                String s = firesult.getString("LastKnownclient");
                if (s != null && !s.isEmpty()) return s;
            }
        } catch (Exception e){
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error getting player!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }
    public void addPlayerTriggers(UUID playeruuid, String reason) {
        PreparedStatement first = null;
        checkdbconnection();
        try {
            first = db.prepareStatement("INSERT INTO " + this.database + ".Triggers VALUES ('" + playeruuid + "','" + reason + "');");
            first.executeUpdate();
        } catch (Exception e) {
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error adding player trigger reason!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
    }
    public void addPlayerHandlerReasons(UUID playeruuid,String handler, String reason) {
        PreparedStatement first = null;
        checkdbconnection();
        try{
            first = db.prepareStatement("INSERT INTO " + this.database + ".OtherReasons VALUES ('" + playeruuid + "','" + handler + "','" + reason + "');");
            first.executeUpdate();
        }catch (Exception e) {
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error adding player handler reason!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
    }
    public void addPlayerIP(UUID playeruuid, String IP){
        PreparedStatement first = null;
        checkdbconnection();
        try {
            first = db.prepareStatement("INSERT INTO " + this.database + ".PlayerIPTable VALUES ('" + playeruuid + "','" + IP + "');");
            first.executeUpdate();
        }catch (Exception e){
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error adding player IP!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();

        }
    }
    public void setUser(UUID playeruuid, String clientname){
        PreparedStatement first = null;
        PreparedStatement second = null;
        ResultSet firesult = null;
        checkdbconnection();
        try {
            second = db.prepareStatement("SELECT * FROM " + this.database + ".CorePlayerStats WHERE PlayerUUID='" + playeruuid + "';");
            firesult = second.executeQuery();
            while (firesult.next()){
                String s = firesult.getString("PlayerUUID");
                if (s != null && !s.isEmpty()){
                    first = db.prepareStatement("UPDATE " + this.database + ".CorePlayerStats SET LastKnownclient='" + clientname + "' WHERE PlayerUUID='" + playeruuid + "';");
                }
                first.executeUpdate();
                return;
            }
            first = db.prepareStatement("INSERT INTO " + this.database + ".CorePlayerStats VALUES ('" + playeruuid + "','" + clientname + "');");
            first.executeUpdate();
        } catch (Exception e){
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error setting user!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();

        }
    }
    public void setplayerstatsban(UUID playeruuid, String banvalue, String mutevalue) {
        PreparedStatement first = null;
        PreparedStatement second = null;
        ResultSet firesult = null;
        checkdbconnection();
        try {
            second = db.prepareStatement("SELECT * FROM " + this.database + ".Playerstats WHERE PlayerUUID='" + playeruuid + "';");
            firesult = second.executeQuery();
            while (firesult.next()) {
                String s = firesult.getString("PlayerUUID");
                if (s != null && !s.isEmpty()) {
                    first = db.prepareStatement("UPDATE " + this.database + ".Playerstats SET Ismuted='" + mutevalue + "', Banned='" + banvalue + "' WHERE PlayerUUID='" + playeruuid + "';");
                }
                first.executeUpdate();
                return;
            }
            first = db.prepareStatement("INSERT INTO " + this.database + ".Playerstats VALUES ('" + playeruuid + "','" + banvalue + "','0','0','false','false','notset');");
            first.executeUpdate();
        } catch (Exception e) {
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error adding player ban!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();

        }
    }

    //Playerstatus
    public String getplayerban(UUID playeruuid){
        PreparedStatement first = null;
        PreparedStatement second = null;
        ResultSet firesult = null;
        checkdbconnection();
        try {
            second = db.prepareStatement("SELECT * FROM " + this.database + ".Playerstats WHERE PlayerUUID='" + playeruuid + "';");
            firesult = second.executeQuery();
            while (firesult.next()){
                String s = firesult.getString("Banned");
                if (s != null && !s.isEmpty()) return s;
            }
        }catch (Exception e){
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error getting player ban status!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }
    public String getisplayermuted(UUID playeruuid) {
        PreparedStatement first = null;
        PreparedStatement second = null;
        ResultSet firesult = null;
        checkdbconnection();
        try {
            second = db.prepareStatement("SELECT * FROM " + this.database + ".Playerstats WHERE PlayerUUID='" + playeruuid + "';");
            firesult = second.executeQuery();
            while (firesult.next()){
                String s = firesult.getString("Ismuted");
                if (s != null && !s.isEmpty()) return s;
            }
        }catch (Exception e){
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error getting player bw status!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }

    public String getplayermute(UUID playeruuid){
        PreparedStatement first = null;
        PreparedStatement second = null;
        ResultSet firesult = null;
        checkdbconnection();
        try {
            second = db.prepareStatement("SELECT * FROM " + this.database + ".Playerstats WHERE PlayerUUID='" + playeruuid + "';");
            firesult = second.executeQuery();
            while (firesult.next()){
                String s = firesult.getString("Mutetimes");
                if (s != null && !s.isEmpty()) return s;
            }
        }catch (Exception e){
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error getting player mute times!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }
    public void removeplayermute(UUID playeruuid) {
        PreparedStatement first = null;
        PreparedStatement second = null;
        PreparedStatement third = null;
        ResultSet firesult = null;
        checkdbconnection();
        try{
            first = db.prepareStatement("SELECT Ismuted FROM " + this.database + ".Playerstats WHERE PlayerUUID= '" + playeruuid + "';");
            firesult = first.executeQuery();
            while (firesult.next()) {
                third = db.prepareStatement("UPDATE " + this.database + ".Playerstats SET Ismuted='false' WHERE PlayerUUID='" + playeruuid + "';");
            }
            third.executeUpdate();
        }catch (Exception e){
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error setting player muted!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
    }
    public void addplayermute(UUID playeruuid, Integer number){
        //addnumber
        PreparedStatement first = null;
        PreparedStatement second = null;
        PreparedStatement third = null;
        //add true
        PreparedStatement fourth = null;
        ResultSet firesult = null;
        checkdbconnection();
        try{
            first = db.prepareStatement("SELECT Mutetimes FROM " + this.database + ".Playerstats WHERE PlayerUUID= '" + playeruuid + "';");
            firesult = first.executeQuery();
            while (firesult.next()){
                Integer i = firesult.getInt("Mutetimes");
                if (i != null) {
                    i += 1;
                    third = db.prepareStatement("UPDATE " + this.database + ".Playerstats SET Mutetimes='" + i + "' WHERE PlayerUUID='" + playeruuid + "';");
                }
                fourth = db.prepareStatement("UPDATE " + this.database + ".Playerstats SET Ismuted='true' WHERE PlayerUUID='" + playeruuid + "';");
                fourth.executeUpdate();
                third.executeUpdate();
                return;
            }
        }catch (Exception e){
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error adding player mute times!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }

    }

    public void addplayerkicks(UUID playeruuid, Integer number) {
        PreparedStatement first = null;
        PreparedStatement second = null;
        PreparedStatement third = null;
        ResultSet firesult = null;
        checkdbconnection();
        try {
            first = db.prepareStatement("SELECT Kicktimes FROM " + this.database + ".Playerstats WHERE PlayerUUID= '" + playeruuid + "';");
            firesult = first.executeQuery();
            while (firesult.next()){
                Integer i = firesult.getInt("Kicktimes");
                if (i != null) {
                    i += 1;
                    third = db.prepareStatement("UPDATE " + this.database + ".Playerstats SET kicktimes='" + i + "' WHERE PlayerUUID='" + playeruuid + "';");
                }
                third.executeUpdate();
                return;
            }
        }catch (Exception e){
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error getting player kick times!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return;
    }
    public String getplayerkick(UUID playeruuid){
        PreparedStatement first = null;
        PreparedStatement second = null;
        ResultSet firesult = null;
        checkdbconnection();
        try {
            second = db.prepareStatement("SELECT * FROM " + this.database + ".Playerstats WHERE PlayerUUID='" + playeruuid + "';");
            firesult = second.executeQuery();
            while (firesult.next()){
                String s = firesult.getString("Kicktimes");
                if (s != null && !s.isEmpty()) return s;
            }
        }catch (Exception e){
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error getting player kick times!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }
    public String getplayerbwstatus(UUID playeruuid){
        PreparedStatement first = null;
        PreparedStatement second = null;
        ResultSet firesult = null;
        checkdbconnection();
        try {
            second = db.prepareStatement("SELECT * FROM " + this.database + ".Playerstats WHERE PlayerUUID='" + playeruuid + "';");
            firesult = second.executeQuery();
            while (firesult.next()){
                String s = firesult.getString("Inbanwave");
                if (s != null && !s.isEmpty()) return s;
            }
        }catch (Exception e){
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error getting player bw status!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }
    //End

    public void setJoinTime(UUID playeruuid, String jointime){
        PreparedStatement first = null;
        PreparedStatement second = null;
        ResultSet firesult = null;
        checkdbconnection();
        try {
            second = db.prepareStatement("SELECT * FROM " + this.database + ".Playerstats WHERE PlayerUUID='" + playeruuid + "';");
            firesult = second.executeQuery();
            while (firesult.next()){
                String s = firesult.getString("PlayerUUID");
                if (s != null && !s.isEmpty()){
                    first = db.prepareStatement("UPDATE " + this.database + ".Playerstats SET jointime='" + jointime + "' WHERE PlayerUUID='" + playeruuid + "';");
                }
                first.executeUpdate();
                return;
            }
        } catch (Exception e){
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error setting player jointime!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();

        }
    }
    public String getplayerjointime(UUID playeruuid){
        PreparedStatement first = null;
        PreparedStatement second = null;
        ResultSet firesult = null;
        checkdbconnection();
        try {
            second = db.prepareStatement("SELECT * FROM " + this.database + ".Playerstats WHERE PlayerUUID='" + playeruuid + "';");
            firesult = second.executeQuery();
            while (firesult.next()){
                String s = firesult.getString("jointime");
                if (s != null && !s.isEmpty()) return s;
            }
        }catch (Exception e){
            Core.getInstance().getServer().getConsoleSender().sendMessage(Core.getInstance().playertext(Core.getInstance().prefix) + "Error getting player join time!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }
    //Reports section

}
