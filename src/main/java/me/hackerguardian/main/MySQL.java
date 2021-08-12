package me.hackerguardian.main;

import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MySQL {

    public static Connection db = null;
    private String host = HackerGuardian.getInstance().getConfig().getString("SQLHost");
    private String port = HackerGuardian.getInstance().getConfig().getString("SQLPort");
    private String database = HackerGuardian.getInstance().getConfig().getString("SQLDatabaseName");
    private String user = HackerGuardian.getInstance().getConfig().getString("SQLUsername");
    private String pass = HackerGuardian.getInstance().getConfig().getString("SQLPassword");
    //TODO Add ny liste så man kan se hvad spillern sidst er blivet "kicket" for af checks.
    
    public void setupCoreSystem(){
        String url = null;
        if (this.user.equals("changeme") && this.pass.equals("changeme")){
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "");
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "---------- Core MySQL ----------");
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + " Please setup MySQL in the config. When done reboot the server.");
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + " Disabling plugin. Please reboot to reload config.");
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "-----------------------------");
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "");
            Bukkit.getPluginManager().disablePlugin(HackerGuardian.getInstance());
            return;
        }
        try {
            String driver = "com.mysql.jdbc.Driver";
            url = "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?user=" + this.user + "&password=" + this.pass + "?autoReconnect=true?useUnicode=yes";
            Class.forName(driver);
            String finalUrl = url;
            Bukkit.getScheduler().runTaskAsynchronously(HackerGuardian.getInstance(), () -> {
                try {
                    db = DriverManager.getConnection(finalUrl, this.user, this.pass);
                    formatCoreDatabase();

                } catch (SQLException e) {
                    HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Could not connect to the '" + this.database + "' Database");
                    if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
                }
            });

            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Connection to MySQL database successful.");
        } catch (Exception e) {
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Could not connect to the '" + this.database + "' Database");
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Info: " + url);
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
    }
    /*
    Website basted
     */
    
    public static void checkWebsiteContent(){
        String host = HackerGuardian.getInstance().getConfig().getString("SQLHost");
        String port = HackerGuardian.getInstance().getConfig().getString("SQLPort");
        String database = HackerGuardian.getInstance().getConfig().getString("SQLDatabaseName");
        String user = HackerGuardian.getInstance().getConfig().getString("SQLUsername");
        String pass = HackerGuardian.getInstance().getConfig().getString("SQLPassword");
        String url = null;
    }
    /*
    END
     */
    
    public static void checkdbconnection() {
        String host = HackerGuardian.getInstance().getConfig().getString("SQLHost");
        String port = HackerGuardian.getInstance().getConfig().getString("SQLPort");
        String database = HackerGuardian.getInstance().getConfig().getString("SQLDatabaseName");
        String user = HackerGuardian.getInstance().getConfig().getString("SQLUsername");
        String pass = HackerGuardian.getInstance().getConfig().getString("SQLPassword");
        String url = null;
        try {
            String driver = "com.mysql.jdbc.Driver";
            url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?user=" + user + "&password=" + pass + "?autoReconnect=true?useUnicode=yes";
            Class.forName(driver);

            String finalUrl = url;
            Bukkit.getScheduler().runTaskAsynchronously(HackerGuardian.getInstance(), () -> {
                try {
                    db = DriverManager.getConnection(finalUrl, user, pass);

                } catch (SQLException e) {
                    HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Could not connect to the '" + database + "' Database");
                    if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
                }
            });

        } catch (Exception e) {
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Could not connect to the '" + database + "' Database");
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Info: " + url);
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
    }
    
    public void shutdowndatabase(){
        try {
            db.close();
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "MysQL database connection closed.");
        } catch (SQLException e) {
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Failed to close connection.");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
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
            coreps.close();
            core2.close();
            pip.close();
            Reports.close();
            Comments.close();
            Flags.close();
            triggers.close();
            othereasons.close();
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Successfully created/passed tables.");
        } catch (Exception e) {
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error creating Core system SQL tables.");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
    }
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
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
            bannedCount.close();
            firesult.close();
            //return firesult.getInt(1);
        } catch (Exception e) {
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error getting ip count!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
    public List<String> getPlayerTriggers(UUID playeruuid) {
        PreparedStatement second = null;
        ResultSet firesult = null;
        checkdbconnection();
        try {
            second = db.prepareStatement("SELECT * FROM " + this.database + ".Triggers WHERE PlayerUUID='" + playeruuid + "' ORDER BY 'Reason' DESC LIMIT " + HackerGuardian.getInstance().getConfig().getInt("Settings.MaxReasonListCount") + ";");
            firesult = second.executeQuery();
            List<String> stringArray = new ArrayList<String>();
            stringArray.clear();
            while (firesult.next()) {
                stringArray.add(firesult.getString("Reason"));
            }
            second.close();
            firesult.close();
            return stringArray;
        } catch (Exception e) {
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error getting player triggers!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
    public List<String> getPlayerhandlerReasons(UUID playeruuid) {
        PreparedStatement second = null;
        ResultSet firesult = null;
        checkdbconnection();
        try {
            second = db.prepareStatement("SELECT * FROM " + this.database + ".OtherReasons WHERE PlayerUUID='" + playeruuid + "' ORDER BY 'Reason' DESC LIMIT " + HackerGuardian.getInstance().getConfig().getInt("Settings.MaxHandlerListCount") + ";");
            firesult = second.executeQuery();
            List<String> stringArray = new ArrayList<String>();
            stringArray.clear();
            while (firesult.next()) {
                stringArray.add(firesult.getString("Handler") + ": " + firesult.getString("Reason"));

            }
            second.close();
            firesult.close();
            return stringArray;
        } catch (Exception e) {
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error getting player triggers!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }
    /*public List<String> getPlayerhandler(UUID playeruuid) {
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
    }*/

    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
    public List<String> getPlayerIp(UUID playeruuid){
        PreparedStatement second = null;
        ResultSet firesult = null;
        checkdbconnection();
        try {
            second = db.prepareStatement("SELECT * FROM " + this.database + ".PlayerIPTable WHERE PlayerUUID='" + playeruuid + "' ORDER BY 'IP' DESC LIMIT " + HackerGuardian.getInstance().getConfig().getInt("Settings.MaxIPListCount") +";");
            firesult = second.executeQuery();
            List<String> stringArray = new ArrayList<String>();
            stringArray.clear();
            while (firesult.next()){
                stringArray.add(firesult.getString("IP"));
            }
            second.close();
            firesult.close();
            return stringArray;
        }catch (Exception e){
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error getting player IP!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
    public String getuser(UUID playeruuid){
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
            second.close();
            firesult.close();
        } catch (Exception e){
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error getting player!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
    public void addPlayerTriggers(UUID playeruuid, String reason) {
        PreparedStatement first = null;
        checkdbconnection();
        try {
            first = db.prepareStatement("INSERT INTO " + this.database + ".Triggers VALUES ('" + playeruuid + "','" + reason + "');");
            first.executeUpdate();
            first.close();
        } catch (Exception e) {
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error adding player trigger reason!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
    }
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
    public void addPlayerHandlerReasons(UUID playeruuid,String handler, String reason) {
        PreparedStatement first = null;
        checkdbconnection();
        try{
            first = db.prepareStatement("INSERT INTO " + this.database + ".OtherReasons VALUES ('" + playeruuid + "','" + handler + "','" + reason + "');");
            first.executeUpdate();
            first.close();
        }catch (Exception e) {
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error adding player handler reason!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
    }
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
    public void addPlayerIP(UUID playeruuid, String IP){
        PreparedStatement first = null;
        checkdbconnection();
        try {
            first = db.prepareStatement("INSERT INTO " + this.database + ".PlayerIPTable VALUES ('" + playeruuid + "','" + IP + "');");
            first.executeUpdate();
            first.close();
        }catch (Exception e){
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error adding player IP!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();

        }
    }
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
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
            first.close();
            second.close();
            firesult.close();
        } catch (Exception e){
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error setting user!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();

        }
    }
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
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
            first.close();
            second.close();
            firesult.close();
        } catch (Exception e) {
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error adding player ban!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();

        }
    }

    //Playerstatus
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
    public String getplayerban(UUID playeruuid){
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
            second.close();
            firesult.close();
        }catch (Exception e){
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error getting player ban status!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return "null";
    }
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
    public void setPlayerBanfalse(UUID playeruuid) {
        PreparedStatement first = null;
        checkdbconnection();
        try {
            first = db.prepareStatement("UPDATE " + this.database + ".Playerstats SET Banned='false' WHERE PlayerUUID='" + playeruuid + "';");
            first.executeUpdate();
            first.close();
        }catch (Exception e){
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error setting player ban status!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
    }
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
    public void setPlayerBantrue(UUID playeruuid) {
        PreparedStatement first = null;
        checkdbconnection();
        try {
            first = db.prepareStatement("UPDATE " + this.database + ".Playerstats SET Banned='true' WHERE PlayerUUID='" + playeruuid + "';");
            first.executeUpdate();
            first.close();
        }catch (Exception e){
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error setting player ban status!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
    }
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
    public String getisplayermuted(UUID playeruuid) {
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
            second.close();
            firesult.close();
        }catch (Exception e){
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error getting player bw status!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
    public String getplayermute(UUID playeruuid){
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
            second.close();
            firesult.close();
        }catch (Exception e){
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error getting player mute times!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
    public void removeplayermute(UUID playeruuid) {
        PreparedStatement first = null;
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
            first.close();
            third.close();
            firesult.close();
        }catch (Exception e){
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error setting player muted!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
    }
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]", "lgtm [java/non-null-boxed-variable]"})
    public void addplayermute(UUID playeruuid, Integer number){
        //addnumber
        PreparedStatement first = null;
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
            first.close();
            third.close();
            fourth.close();
            firesult.close();
        }catch (Exception e){
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error adding player mute times!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }

    }
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]", "lgtm [java/non-null-boxed-variable]"})
    public void addplayerkicks(UUID playeruuid, Integer number) {
        PreparedStatement first = null;
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
            first.close();
            third.close();
            firesult.close();
        }catch (Exception e){
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error getting player kick times!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return;
    }
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
    public String getplayerkick(UUID playeruuid){
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
            second.close();
            firesult.close();
        }catch (Exception e){
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error getting player kick times!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
    public String getplayerbwstatus(UUID playeruuid){
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
            second.close();
            firesult.close();
        }catch (Exception e){
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error getting player bw status!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        return null;
    }
    //End
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
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
            first.close();
            second.close();
            firesult.close();
        } catch (Exception e){
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error setting player jointime!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();

        }
    }
    @SuppressWarnings({"lgtm [java/concatenated-sql-query]"})
    public String getplayerjointime(UUID playeruuid){
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
            second.close();
            firesult.close();
        }catch (Exception e){
            HackerGuardian.getInstance().getServer().getConsoleSender().sendMessage(HackerGuardian.getInstance().playertext(HackerGuardian.getInstance().prefix) + "Error getting player join time!");
            if (HackerGuardian.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }

        return null;
    }
    //Reports section

}
