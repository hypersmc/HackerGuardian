package me.hackerguardian.main;

import com.sun.org.apache.xerces.internal.xs.StringList;
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

    public void setupCoreSystem(){
        String url = null;
        if (this.user.equals("changeme") && this.pass.equals("changeme")){
            Bukkit.getLogger().info("");
            Bukkit.getLogger().severe("---------- Core MySQL ----------");
            Bukkit.getLogger().severe(" Please setup MySQL in the config. When done reboot the server.");
            Bukkit.getLogger().severe(" Disabling plugin. Please reboot to reload config.");
            Bukkit.getLogger().severe("-----------------------------");
            Bukkit.getLogger().info("");
            Bukkit.getPluginManager().disablePlugin(Core.getInstance());
            return;
        }
        try {
            String driver = "com.mysql.jdbc.Driver";
            url = "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?user=" + this.user + "&password=" + this.pass + "?autoReconnect=true?useUnicode=yes";
            Class.forName(driver);
            db = DriverManager.getConnection(url, this.user, this.pass);
            formatCoreDatabase();
            Bukkit.getLogger().info("[" + Core.getInstance().getName() + "] Connection to MySQL database successful.");
        } catch (Exception e) {
            Bukkit.getLogger().severe("[" + Core.getInstance().getName() + "] Could not connect to the '" + this.database + "' Database");
            Bukkit.getLogger().info("Info: " + url);
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
    }
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
            db = DriverManager.getConnection(url, user, pass);
            Bukkit.getLogger().info("[" + Core.getInstance().getName() + "] Connection to MySQL database successful.");
        } catch (Exception e) {
            Bukkit.getLogger().severe("[" + Core.getInstance().getName() + "] Could not connect to the '" + database + "' Database");
            Bukkit.getServer().getLogger().severe("Info: " + url);
            e.printStackTrace();
        }
    }
    public void shutdowndatabase(){
        try {
            db.close();
            Bukkit.getLogger().info("[" + Core.getInstance().getName() + "] MysQL database connection closed.");
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[" + Core.getInstance().getName() + "] Failed to close connection.");
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
        //
        try {
            coreps = db.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.database + ".CorePlayerStats(`PlayerUUID` VARCHAR(64) NOT NULL, `LastKnownclient` VARCHAR(64) NULL, PRIMARY KEY (`PlayerUUID`));");
            core2 = db.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.database + ".Playerstats(`PlayerUUID` VARCHAR(64) NOT NULL, `Banned` VARCHAR(5) NULL, `Mutetimes` INT NULL, `Kicktimes` INT NULL, `Inbanwave` VARCHAR(5) NULL, `jointime` VARCHAR(64) NULL, PRIMARY KEY (`PlayerUUID`));");
            pip = db.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.database + ".PlayerIPTable(`PlayerUUID` VARCHAR(64) NOT NULL, `IP` VARCHAR(64) NULL, FOREIGN KEY (`PlayerUUID`) REFERENCES Playerstats(`PlayerUUID`));");
            Reports = db.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.database + ".Reports(`ID` INT(8) NOT NULL AUTO_INCREMENT, `ReportedUUID` VARCHAR(45) NULL, `ReportedByUUID` VARCHAR(45) NULL, `Reason` MEDIUMTEXT NULL, `Date` DATETIME NULL, PRIMARY KEY (`ID`));");
            Comments = db.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.database + ".Comments(`ID` INT(8) NOT NULL AUTO_INCREMENT, `ConnectedID` VARCHAR(45) NULL, `CommenterUUID` VARCHAR(45) NULL, `CommentText` MEDIUMTEXT NULL, `CommentDate` DATETIME NULL, PRIMARY KEY (`ID`));");
            Flags = db.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.database + ".Flags(`ID` INT(8) NOT NULL AUTO_INCREMENT, `ConnectedID` VARCHAR(45) NULL, `Flag` VARCHAR(200) NULL, `UUID` VARCHAR(60) NULL, `Date` DATETIME NULL, PRIMARY KEY (`ID`));");
            coreps.executeUpdate();
            core2.executeUpdate();
            pip.executeUpdate();
            Reports.executeUpdate();
            Comments.executeUpdate();
            Flags.executeUpdate();

            Bukkit.getLogger().info("[" + Core.getInstance().getName() + "] Successfully created/passed tables.");
        } catch (Exception e) {
            Bukkit.getLogger().severe("[" + Core.getInstance().getName() + "] Error creating Core system SQL tables.");
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
            Bukkit.getLogger().severe("[" + Core.getInstance().getName() + "] Error getting ip count!");
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
            Bukkit.getLogger().severe("[" + Core.getInstance().getName() + "] Error getting player status!");
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
            Bukkit.getLogger().severe("[" + Core.getInstance().getName() + "] Error getting player status!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }
    public void addPlayerIP(UUID playeruuid, String IP){
        PreparedStatement first = null;
        checkdbconnection();
        try {
            first = db.prepareStatement("INSERT INTO " + this.database + ".PlayerIPTable VALUES ('" + playeruuid + "','" + IP + "');");
            first.executeUpdate();
        }catch (Exception e){
            Bukkit.getLogger().severe("[" + Core.getInstance().getName() + "] Error adding/updating player status!");
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
            Bukkit.getLogger().severe("[" + Core.getInstance().getName() + "] Error adding/updating player status!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();

        }
    }
    public void setban(UUID playeruuid, String banvalue) {
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
                    first = db.prepareStatement("UPDATE " + this.database + ".Playerstats SET Banned='" + banvalue + "' WHERE PlayerUUID='" + playeruuid + "';");
                }
                first.executeUpdate();
                return;
            }
            first = db.prepareStatement("INSERT INTO " + this.database + ".Playerstats VALUES ('" + playeruuid + "','" + banvalue + "','0','0','false','notset');");
            first.executeUpdate();
        } catch (Exception e) {
            Bukkit.getLogger().severe("[" + Core.getInstance().getName() + "] Error adding/updating player status!");
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
            Bukkit.getLogger().severe("[" + Core.getInstance().getName() + "] Error getting player ban status!");
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
            Bukkit.getLogger().severe("[" + Core.getInstance().getName() + "] Error getting player mute times!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
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
            Bukkit.getLogger().severe("[" + Core.getInstance().getName() + "] Error getting player kick times!");
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
            Bukkit.getLogger().severe("[" + Core.getInstance().getName() + "] Error getting player bw status!");
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
            Bukkit.getLogger().severe("[" + Core.getInstance().getName() + "] Error adding/updating player status!");
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
            Bukkit.getLogger().severe("[" + Core.getInstance().getName() + "] Error getting player join time!");
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }
    //Reports section

}
