package me.hackerguardian.main;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.hackerguardian.main.ML.LVQNeuralNetwork;
import me.hackerguardian.main.ML.LVQNeuralNetworkPredictResult;
import me.hackerguardian.main.ML.LVQNeuralNetworkSummary;
import me.hackerguardian.main.ML.LabeledData;
import me.hackerguardian.main.ML.listener.PlayerAttackAngleLogger;
import me.hackerguardian.main.Utils.FileUtil;
import me.hackerguardian.main.Utils.SLMaths;
import me.hackerguardian.main.getters.Commandlist;
import me.hackerguardian.main.getters.Timerlist;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class Core extends JavaPlugin implements Listener {
    //General content.
    public static int FEATURE_COUNT = 4;
    Logger logger = getLogger();
    public static Plugin plugin;
    public String prefix = "&4&l[&r&l&8HackerGuardian&r&4&l]&r ";
    public String shortprefix = "&4&l[&r&l&8HG&r&4&l]&r ";
    private CommandManager commandManager;
    public static Core getInstance(){
        return Core.getPlugin(Core.class);
    }
    public static Plugin getPlugin() {
        return plugin;
    }
    private final Map<InetSocketAddress, Integer> playerVersions = new ConcurrentHashMap<InetSocketAddress, Integer>();
    HashMap<String, String[]> commentConfirm = (HashMap)new HashMap<>();

    //Machine learning
    public static String DIRNAME_CATEGORY = "category";
    public static String DIRNAME_DUMPED_DATA = "dumped_data";
    public volatile LVQNeuralNetwork neuralNetwork;
    private PlayerAttackAngleLogger attackAngleLogger;
    public static Map<String, Integer> categoryNameMap = new HashMap<>();
    /**
     *
     * @param text
     * @return will return the text argument with colors if (&) and color codes are used.
     */
    public String playertext(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Messenger messenger = Bukkit.getMessenger();
        messenger.registerIncomingPluginChannel(this, "minecraft:brand", new BrandPluginMessageListener());
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

        //ML
        try {
            FileUtil.createDirectoryIfAbsent(getDataFolder(), DIRNAME_CATEGORY);
            FileUtil.createDirectoryIfAbsent(getDataFolder(), DIRNAME_DUMPED_DATA);
            FileUtil.saveResourceIfAbsent(this, "config.yml", "config.yml");
        } catch (IOException e) {
            getLogger().severe("Was not able to save resource files");
            e.printStackTrace();
        }


        rebuildNetworkWithDataset();
        attackAngleLogger = new PlayerAttackAngleLogger();
        getServer().getPluginManager().registerEvents(attackAngleLogger, this);

        if(!new AdvancedLicense("HFDC-626Y-B1QR-981H", "https://zennodes.dk/ActivationKey/verify.php", this).setSecurityKey("TkHr6umrQ8OUPxeWt0ANuXa3zlTopUyF7nYUJaahbZMJAoRZOOzcLjCTG70zVJeDKavfJOC0ilD56Ll6MSV7PFVKkwaMpgwmnk4").register()) return;
        MySQL sql = new MySQL();
        sql.setupCoreSystem();
        if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this,
                    ListenerPriority.NORMAL,
                    PacketType.Handshake.Client.SET_PROTOCOL, PacketType.Login.Server.DISCONNECT) {

                @Override
                public void onPacketReceiving(final PacketEvent event) {
                    final PacketContainer packet = event.getPacket();

                    if (event.getPacketType() == PacketType.Handshake.Client.SET_PROTOCOL) {
                        if (packet.getProtocols().read(0) == PacketType.Protocol.LOGIN) {
                            playerVersions.put(event.getPlayer().getAddress(), packet.getIntegers().read(0));
                        }
                    } else {
                        playerVersions.remove(event.getPlayer().getAddress());
                    }
                }
            });
        }
    }
    @EventHandler
    public void onjoin(PlayerJoinEvent event) {
        MySQL sql = new MySQL();
        if (!event.getPlayer().hasPlayedBefore()){
            sql.setban(event.getPlayer().getUniqueId(), "false");
            event.getPlayer().kickPlayer("Timeout");
        }
        if (getConfig().getBoolean("Settings.logplayerip")){
            String ip = event.getPlayer().getAddress().toString();
            sql.addPlayerIP(event.getPlayer().getUniqueId(), ip);
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        LocalDateTime playerlogin = LocalDateTime.now();
        sql.setJoinTime(event.getPlayer().getUniqueId(), dtf.format(playerlogin));

    }

    public int getVersion(final Player player) {
        return playerVersions.get(player.getAddress());
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        playerVersions.remove(event.getPlayer().getAddress());
    }
    @Override
    public void onDisable() {
        MySQL sql = new MySQL();
        Connection conn = MySQL.db;
        if (conn == null) return;
        try {
            if (conn.isValid(5) || conn != null) sql.shutdowndatabase();
        } catch (SQLException e) {
            if (getConfig().getBoolean("debug")) e.printStackTrace();
        }
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
                int count = 0;
                while (count < commandManager.registeredCommands.size()){
                    count += 1;
                    commandManager.registeredCommands.entrySet().stream().forEach(e -> sender.sendMessage("/hg " + e.getKey()));
                    return;
                }
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
        commandManager.register("set", ((sender, params) -> {
            MySQL sql = new MySQL();
            if (params.length != 1) {
                sender.sendMessage(playertext(prefix + "Wrong parameters! /hg set <player-name>"));
            }else {
                Player p = getServer().getPlayer(params[0]);
                sql.setban(p.getUniqueId(), "false");
            }
        }));
        commandManager.register("view", ((sender, params) -> {
            MySQL sql = new MySQL();
            if (params.length != 1 && params.length != 2) {
                sender.sendMessage(playertext(prefix + "Wrong parameters! /hg view <player-name> [page 1-3]"));
                return;
            }
            if (!getServer().getOfflinePlayer(params[0]).hasPlayedBefore()){
                sender.sendMessage(playertext(prefix + ChatColor.RED + params[0] + ChatColor.RESET + " have never joined this server."));
                return;
            }
            if (params.length == 1){
                Player p = getServer().getPlayer(params[0]);
                sender.sendMessage(playertext(prefix + "Statistics collected about '" + ChatColor.RED + params[0] + ChatColor.RESET + "'"));
                if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
                    sender.sendMessage(playertext(shortprefix + "Player client version: '" + ChatColor.RED + getVersion(p) + ChatColor.RESET + "'"));
                }else {
                    sender.sendMessage(playertext(shortprefix + "Player client version: '" + ChatColor.RED + "Unknown" + ChatColor.RESET + "'"));
                }
                sender.sendMessage(playertext(shortprefix + "Player last used client: '" + ChatColor.RED + sql.getuser(p.getUniqueId()) + ChatColor.RESET + "'"));
                sender.sendMessage(playertext(shortprefix + "Player last login: '" + ChatColor.RED + sql.getplayerjointime(p.getUniqueId()) + ChatColor.RESET + "'" ));
                sender.sendMessage(playertext(shortprefix + "Player average CPS: '" + ChatColor.RED + "ERROR" + ChatColor.RESET + "'"));
                sender.sendMessage(playertext(shortprefix + "Player in banwave queue: '" + ChatColor.RED + sql.getplayerbwstatus(p.getUniqueId()) + ChatColor.RESET + "'"));
                sender.sendMessage(playertext(shortprefix + "------------- Page 1/3 -------------"));

            }

            if (params.length >= 2){
                if (!StringUtils.isNumeric(params[1])) {
                    sender.sendMessage(playertext(prefix + ChatColor.RED + params[1] + ChatColor.RESET + " is not a valid number."));
                    return;
                }
                int viewnumber = Integer.valueOf(params[1]);

                if (viewnumber == 1) {
                    Player p = getServer().getPlayer(params[0]);
                    sender.sendMessage(playertext(prefix + "Statistics collected about '" + ChatColor.RED + params[0] + ChatColor.RESET + "'"));
                    if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
                        sender.sendMessage(playertext(shortprefix + "Player client version: '" + ChatColor.RED + getVersion(p) + ChatColor.RESET + "'"));
                    }else {
                        sender.sendMessage(playertext(shortprefix + "Player client version: '" + ChatColor.RED + "Unknown" + ChatColor.RESET + "'"));
                    }
                    sender.sendMessage(playertext(shortprefix + "Player last used client: '" + ChatColor.RED + sql.getuser(p.getUniqueId()) + ChatColor.RESET + "'"));
                    sender.sendMessage(playertext(shortprefix + "Player last login: '" + ChatColor.RED + sql.getplayerjointime(p.getUniqueId()) + ChatColor.RESET + "'" ));
                    sender.sendMessage(playertext(shortprefix + "Player average CPS: '" + ChatColor.RED + "ERROR" + ChatColor.RESET + "'"));
                    sender.sendMessage(playertext(shortprefix + "Player in banwave queue: '" + ChatColor.RED + sql.getplayerbwstatus(p.getUniqueId()) + ChatColor.RESET + "'"));
                    sender.sendMessage(playertext(shortprefix + "------------- Page 1/3 -------------"));
                }else if (viewnumber == 2){
                    Player p = getServer().getPlayer(params[0]);
                    sender.sendMessage(playertext(prefix + "Statistics collected about '" + ChatColor.RED + params[0] + ChatColor.RESET + "'"));
                    //sender.sendMessage(playertext(shortprefix + "Player average CPS: '" + ChatColor.RED + "ERROR" + ChatColor.RESET + "'"));
                    sender.sendMessage(playertext(shortprefix + "Is Player banned: '" + ChatColor.RED + sql.getplayerban(p.getUniqueId()) + ChatColor.RESET + "'"));
                    sender.sendMessage(playertext(shortprefix + "Player mute amount: '" + ChatColor.RED + sql.getplayermute(p.getUniqueId()) + ChatColor.RESET + "'"));
                    sender.sendMessage(playertext(shortprefix + "Player kick amount: '" + ChatColor.RED + sql.getplayerkick(p.getUniqueId()) + ChatColor.RESET + "'"));
                    if (getConfig().getBoolean("Settings.logplayerip")){
                        sender.sendMessage(playertext(shortprefix + "Player's last (1-3) login IP's:"));
                        sql.getPlayerIp(p.getUniqueId()).forEach((IP) -> sender.sendMessage("  - " + IP.toString().replaceAll("/", "")));
                    }else {
                        sender.sendMessage(playertext(shortprefix + "Due to safety reasons we keep this safe."));
                    }
                    sender.sendMessage(playertext(shortprefix + "------------- Page 2/3 -------------"));
                }else if (viewnumber == 3) {
                    Player p = getServer().getPlayer(params[0]);
                    sender.sendMessage(playertext(prefix + "Statistics collected about '" + ChatColor.RED + params[0] + ChatColor.RESET + "'"));

                    sender.sendMessage(playertext(shortprefix + "------------- Page 3/3 -------------"));

                }else{
                    sender.sendMessage(playertext(prefix + ChatColor.RED + params[1] + ChatColor.RESET + " is not a valid list number."));

                }
            }

        }));
        commandManager.register("report", ((sender, params) -> {
            if (CommandValidate.notPlayer(sender)) return;

            MySQL sql = new MySQL();
            StringBuilder reason = new StringBuilder();
            for (int i = 1; i < params.length; i++){
                reason.append(params[i]);
                reason.append(" ");
            }
            if (sender.hasPermission("hg.report")){
                if (params.length == 0){
                    sender.sendMessage(playertext(prefix + "Wrong parameters! /hg report <user> <reason>"));
                    return;
                }
                if (params.length == 1) {
                    sender.sendMessage(playertext(prefix + "It looks like you'd like to report " + params[0] + ". You need to provide a reason."));
                    sender.sendMessage(playertext(prefix + "Wrong parameters! /hg report <user> <reason>"));
                    return;
                }
                String timeStamp = (new SimpleDateFormat("YYYYMMMddHHmmss")).format(Calendar.getInstance().getTime());
                UUID reportedBy = Bukkit.getPlayer(sender.getName()).getUniqueId();
                UUID reported = null;
                Player playerarg = Bukkit.getPlayer(params[0]);
                UUID id;
                OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(params[0]);
                boolean type = false;
                if (playerarg == null){
                    if (oPlayer == null){
                        sender.sendMessage(playertext(prefix + "This player doesn't exist. Please try again."));
                        return;
                    }
                    if (!oPlayer.hasPlayedBefore()){
                        sender.sendMessage(playertext(prefix + "This player have never joined this server."));
                        return;
                    }
                }else {
                    type = true;
                }
                if (type) {
                    reported = Bukkit.getPlayer(params[0]).getUniqueId();
                }else {
                    reported = Bukkit.getOfflinePlayer(params[0]).getUniqueId();
                }
                int maxReports = 10;
                /*if (maxReports != -1 ) { //&& sql.
                    sender.sendMessage(playertext(prefix + "You cannot make more than 10 reports."));
                    return;
                }
                if (reported.equals(reportedBy)) {
                    sender.sendMessage(playertext(prefix + "You can't report yourself!"));
                    return;
                }*/
                //sql.
                if (maxReports > -1) {
                    sender.sendMessage(playertext(prefix + "You have &c" + String.valueOf(maxReports) + " &rreports remaining. (max " + maxReports + ")")); // - sql.
                    String time = (new SimpleDateFormat("YYYY-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime());
                    sender.sendMessage(playertext(shortprefix + "Thanks for your report."));
                    sender.sendMessage(playertext(shortprefix + "Your report:"));
                    sender.sendMessage(playertext(shortprefix + "  - Your name: '" + ChatColor.RED + sender.getName() + ChatColor.RESET + "'"));
                    sender.sendMessage(playertext(shortprefix + "  - Player you reported: '" + ChatColor.RED + params[0] + ChatColor.RESET + "'"));
                    sender.sendMessage(playertext(shortprefix + "  - Date (YYYY-MM-DD) : '" + ChatColor.RED + String.valueOf(time) + ChatColor.RESET + "'"));
                    sender.sendMessage(playertext(shortprefix + "  - Reason: "));
                    sender.sendMessage(playertext("> " + ChatColor.RED + reason.toString()));
                    sender.sendMessage(playertext(shortprefix + "Staff should view your report within " + getConfig().getInt("Settings.ReportViewTime")));
                    for (Player loopplayer : Bukkit.getServer().getOnlinePlayers()) {
                        if (loopplayer.hasPermission("hg.reports.notify")) {
                            loopplayer.sendMessage("");
                            loopplayer.sendMessage(playertext(prefix + "Player: " + ChatColor.RED + params[0] + ChatColor.RESET + " has just been reported."));
                            //String[][] report = sql.get;
                            //int length = report.length;
                            loopplayer.sendMessage(playertext(prefix + "Type /hg reports view " + String.valueOf(params[0]) + ", for more info."));
                            loopplayer.sendMessage("");
                        }
                    }
                    return;
                }
                return;

            }
            sender.sendMessage("Unknown command. Type \"/help\" for help.\n");
            return;
        }));

        commandManager.register("reports", ((sender, params) -> {
            if (CommandValidate.notPlayer(sender)) return;

            MySQL sql = new MySQL();
            StringBuilder reason = new StringBuilder();
            for (int i = 1; i < params.length; i++){
                reason.append(params[i]);
                reason.append(" ");
            }
            if (sender.hasPermission("hg.reports")){
                if (params.length == 0){
                    sender.sendMessage(playertext(prefix + "Wrong parameters! /hg reports help"));
                    return;
                }

                if (params[0].equalsIgnoreCase("help")){

                }
                if (params[0].equalsIgnoreCase("view")){
                    if (sender.hasPermission("hg.reports.view")){
                        if (params.length == 1) {
                            sender.sendMessage(playertext(prefix + "To view reports you can use Player name OR ID."));
                            sender.sendMessage(playertext(shortprefix + "To view by Name: " + ChatColor.RED + "/hg reports view <player> [Page]"));
                            sender.sendMessage(playertext(shortprefix + "To view by ID: " + ChatColor.RED + "/hg reports view <ID>"));
                            return;
                        }
                        if (params.length >= 2) {
                            Player player = Bukkit.getPlayer(params[1]);
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(params[0]);
                            UUID reported = null;
                            boolean type = false;
                            int arg = 0;
                            try {
                                arg = Integer.parseInt(params[1]);
                                //int lastid = sql.;
                                int lastid = 1;
                                if (lastid == -1) {
                                    sender.sendMessage(playertext(prefix + "Sorry but an error has occurred and we cannot retrieve latest ID"));
                                }
                                if (arg > lastid){
                                    sender.sendMessage(playertext(prefix + "The typed ID does not exist in the system. Max ID: " + String.valueOf(lastid)));
                                    return;
                                }
                                try {
                                    return;
                                } catch (NullPointerException e) {

                                }
                            } catch (NumberFormatException e) {
                                if (player == null) {
                                    if (offlinePlayer == null) {
                                        sender.sendMessage(playertext(prefix + "This player doesn't exist. Please try again."));
                                        return;
                                    }
                                    if (!offlinePlayer.hasPlayedBefore()) {
                                        sender.sendMessage(playertext(prefix + "This player have never joined this server."));
                                        return;
                                    }
                                }else{
                                    type = true;
                                }
                                if (type) {
                                    reported = Bukkit.getPlayer(params[1]).getUniqueId();
                                }else {
                                    reported = Bukkit.getOfflinePlayer(params[1]).getUniqueId();
                                }
                                /*String[][] report = sql.g;
                                if (report == null || report.length == 0) {
                                    sender.sendMessage(playertext(prefix + "There currently isn't any reports on this player."));
                                    return;
                                }*/
                                //int reportsize = report.length;
                                int reportsize = 3;
                                try {
                                    if (params.length == 2) {
                                        sender.sendMessage(playertext(shortprefix + "Showing record 1 for " + ChatColor.RED + params[1]));
                                        sender.sendMessage(playertext(shortprefix + "We found a total number of: " + ChatColor.RED + String.valueOf(reportsize) + ChatColor.RESET + "report in records."));
                                        //stuff
                                        if (reportsize >= 2) {
                                            sender.sendMessage(playertext(shortprefix + "You can do: " + ChatColor.RED + "/hg reports view " + params[1] + " 2" + ChatColor.RESET + " to view the next record."));
                                        }
                                    }
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            }
                        }
                    }
                }

            }
        }));

        commandManager.register("start", ((sender, params) -> {
            if (CommandValidate.notPlayer(sender)) return;

            Player player = (Player) sender;
            attackAngleLogger.registerPlayer(player); // start logging angles
            sender.sendMessage(playertext(prefix + "Started logging angles for '" + ChatColor.RED + player.getName() + ChatColor.RESET + "'"));

        }));

        commandManager.register("stop", (sender, params) -> {
            if (CommandValidate.notPlayer(sender)) return;

            Player player = (Player) sender;
            if (!attackAngleLogger.getRegisteredPlayers().contains(player.getName())) {
                sender.sendMessage(playertext(shortprefix + "You haven't started logging angles for '" + ChatColor.RED + player.getName() + ChatColor.WHITE + "'"));
                return;
            }

            attackAngleLogger.unregisterPlayer(player); // stop logging angles
            sender.sendMessage(playertext(shortprefix + "Stopped logging angles for '" + ChatColor.RED + player.getName() + ChatColor.WHITE + "'"));

            try {
                List<Float> angleSequence = attackAngleLogger.getLoggedAngles(player);
                double[] extractedFeatures = SLMaths.extractFeatures(angleSequence);

                String saveFileName = Core.DIRNAME_DUMPED_DATA + File.separator + System.currentTimeMillis() + ".yml";
                File saveFile = new File(getDataFolder(), saveFileName);
                if (!saveFile.createNewFile())
                    throw new IOException();

                FileConfiguration saveFileYaml = new YamlConfiguration();
                saveFileYaml.set("feature", extractedFeatures);
                saveFileYaml.set("raw_angles", angleSequence);
                saveFileYaml.save(saveFile);
                sender.sendMessage(playertext(shortprefix + "Data have been saved to '" + ChatColor.RED + saveFileName
                        + ChatColor.RESET + "'" + ChatColor.DARK_RED + " (" + ChatColor.RESET + angleSequence.size() + " samples" + ChatColor.DARK_RED + ")"));

                attackAngleLogger.clearLoggedAngles(player);
            } catch (IOException e) {
                getLogger().severe("Unable to dump vector and angles of player '" + player.getName() + "'");
                e.printStackTrace();
                sender.sendMessage(ChatColor.RED + "Failed to save logged angles due to an I/O error");
            }
        });

        commandManager.register("info", (sender, params) -> {
            LVQNeuralNetworkSummary summary = neuralNetwork.getSummaryStatistics();
            sender.sendMessage(playertext(shortprefix + "Neural network layer statistics: "));
            sender.sendMessage(playertext(shortprefix + "  Dataset size: " + ChatColor.RED + summary.getInputCount()));
            sender.sendMessage(playertext(shortprefix + "  Output layer: " + ChatColor.RED + summary.getOutputCount() + ChatColor.RESET + " neuron(s)"));
            sender.sendMessage(playertext(shortprefix + "Neural network learning statistics:"));
            sender.sendMessage(playertext(shortprefix + "  Epoch: " + ChatColor.RED + summary.getEpoch()));
            sender.sendMessage(playertext(shortprefix + "  Current step size: " + ChatColor.RED + summary.getCurrentStepSize()));
            sender.sendMessage(playertext(shortprefix + "Category statistics:"));
            sender.sendMessage(playertext(shortprefix + "  Loaded: " + ChatColor.RED + categoryNameMap.size()));
            sender.sendMessage(playertext(shortprefix + "  Mappings:"));
            categoryNameMap.forEach((cat, id) -> sender.sendMessage(playertext("  - " + ChatColor.RED + "[" + ChatColor.RESET + id  + ChatColor.RED + "] " + ChatColor.RESET + cat)));
        });

        commandManager.register("train", (sender, params) -> {
            if (CommandValidate.notPlayer(sender))
                return;

            if (params.length != 1) {
                sender.sendMessage(playertext(prefix + "Wrong parameters! /hg train <category-name>"));
                return;
            }

            int duration_to_generate_a_vector = getConfig().getInt("sampler.duration_to_generate_a_vector");
            int vector_per_category = getConfig().getInt("sampler.vector_per_category");
            trainNetwork((Player) sender, params[0], duration_to_generate_a_vector, vector_per_category);
        });

        commandManager.register("reload", (sender, params) -> {
            MySQL sql = new MySQL();
            sql.shutdowndatabase();
            sql.setupCoreSystem();
            reloadConfig();
            sender.sendMessage(playertext(prefix + "Reloaded configuration."));
            return;
        });

        commandManager.register("rebuild", ((sender, params) -> {
            rebuildNetworkWithDataset();
            sender.sendMessage(playertext(prefix + "Rebuilding neural network."));
        }));

        commandManager.register("Banwavetest", (sender, params) -> {
            if (params.length != 1 && params.length != 2) {
                sender.sendMessage(playertext(prefix + "This shouldn't really trigger AT ALL. Please report to system administrator."));
                return;
            }
            Player testplayer = getServer().getPlayer(params[0]);
            if (testplayer == null){
                sender.sendMessage(playertext(prefix + "Player'" + ChatColor.RED + params[0] + ChatColor.RESET + "' Was not found! Banwavetest failed."));
                return;
            }
            if (params.length == 2)
                if (!StringUtils.isNumeric(params[1])) {
                    sender.sendMessage(playertext(prefix + ChatColor.RED + params[1] + ChatColor.RESET + " is not a valid number."));
                    return;
                }
            int duration = params.length == 1 ? getConfig().getInt("test.default_duration") : Integer.valueOf(params[1]);
            sender.sendMessage(playertext(shortprefix + "Attempting to sample motion of '" + ChatColor.RED + params[0]
                    + ChatColor.RESET + " for " + ChatColor.RED + duration + ChatColor.RESET + " seconds"));
            sender.sendMessage(playertext(shortprefix + ChatColor.RED + "[WARNING] " + ChatColor.RESET
                    + "Be aware of using this command. If used by users and not the system itself IT will add the ban count to system (if chances are that they are hacking)"));
            sender.sendMessage(playertext(shortprefix + "The system doesn't check if this command is self triggered or player triggered."));
            classifyPlayer(testplayer, duration , result -> {
                double likelihood = SLMaths.round(result.getLikelihood() * 100, 2, RoundingMode.HALF_UP);
                sender.sendMessage(playertext(shortprefix + "Neural network classification result:"));
                sender.sendMessage(playertext(shortprefix + "  Best matched: " + ChatColor.RED + getCategoryNameFromID(result.getCategory())));
                sender.sendMessage(playertext(shortprefix + "  Difference: " + ChatColor.RED + result.getDifference()));
                sender.sendMessage(playertext(shortprefix + "  Likelihood: " + ChatColor.RED + likelihood + ChatColor.RESET + "%"));
                if (!getCategoryNameFromID(result.getCategory()).contains("Legit".toLowerCase())) {
                    if (getConfig().getBoolean("Settings.autoaddtobanwave")){
                        sender.sendMessage(playertext(prefix + "Recommended action: "));
                        if (getCategoryNameFromID(result.getCategory()).contains("wurst".toLowerCase())){
                            sender.sendMessage(ChatColor.GREEN + "yeet");
                        } else if (getCategoryNameFromID(result.getCategory()).contains("impact".toLowerCase())){
                            sender.sendMessage(ChatColor.GREEN + "yeet1");
                        }else if (getCategoryNameFromID(result.getCategory()).contains("future".toLowerCase())){
                            sender.sendMessage(ChatColor.GREEN + "yeet2");
                        }else if (getCategoryNameFromID(result.getCategory()).contains("forgehax".toLowerCase())){
                            sender.sendMessage(ChatColor.GREEN + "yeet3");
                        }else if (getCategoryNameFromID(result.getCategory()).contains("wwe".toLowerCase())){
                            sender.sendMessage(ChatColor.GREEN + "yeet4");
                        }else if (getCategoryNameFromID(result.getCategory()).contains("kami".toLowerCase())){
                            sender.sendMessage(ChatColor.GREEN + "yeet5");
                        }else if (getCategoryNameFromID(result.getCategory()).contains("kamib".toLowerCase())){
                            sender.sendMessage(ChatColor.GREEN + "yeet6");
                        }else if (getCategoryNameFromID(result.getCategory()).contains("lbounce".toLowerCase())){
                            sender.sendMessage(ChatColor.GREEN + "yeet7");
                        }else if (getCategoryNameFromID(result.getCategory()).contains("skillcli".toLowerCase())){
                            sender.sendMessage(ChatColor.GREEN + "yeet8");
                        }else if (getCategoryNameFromID(result.getCategory()).contains("aristois".toLowerCase())){
                            sender.sendMessage(ChatColor.GREEN + "yeet9");
                        }else if (getCategoryNameFromID(result.getCategory()).contains("ares".toLowerCase())){
                            sender.sendMessage(ChatColor.GREEN + "yeet10");
                        }else if (getCategoryNameFromID(result.getCategory()).contains("wolfram".toLowerCase())){
                            sender.sendMessage(ChatColor.GREEN + "yeet11");
                        }
                    }else {
                        sender.sendMessage("test");
                        /*Main.Systemban += 1;
                        Main.ban.add(params[0]);
                        this.getConfig().set("ban.players", Main.ban);
                        this.saveConfig();
                        this.registerConfig();*/
                    }
                }
            });

        });

        commandManager.register("test", (sender, params) -> {
            if (params.length != 1 && params.length != 2) {
                sender.sendMessage(ChatColor.RED + "Wrong parameters! /hg test <player-name> [seconds]");
                return;
            }

            Player testPlayer = getServer().getPlayer(params[0]);

            if (testPlayer == null) {
                sender.sendMessage(playertext(prefix + "Unable to find the player '" + ChatColor.RED + params[0] + ChatColor.RESET + "'"));
                return;
            }

            if (params.length == 2)
                if (!StringUtils.isNumeric(params[1])) {
                    sender.sendMessage(playertext(prefix + ChatColor.RED + params[1] + ChatColor.RESET + " is not a valid number."));
                    return;
                }

            int duration = params.length == 1 ? getConfig().getInt("test.default_duration") : Integer.valueOf(params[1]);

            sender.sendMessage(playertext(shortprefix + "Attempting to sample motion of '" + ChatColor.RED + params[0]
                    + ChatColor.RESET + " for " + ChatColor.RED + duration + ChatColor.RESET + " seconds"));
            classifyPlayer(testPlayer, duration , result -> {
                double likelihood = SLMaths.round(result.getLikelihood() * 100, 2, RoundingMode.HALF_UP);
                sender.sendMessage(playertext(shortprefix + "Neural network classification result:"));
                sender.sendMessage(playertext(shortprefix + "  Best matched: " + ChatColor.RED + getCategoryNameFromID(result.getCategory())));
                sender.sendMessage(playertext(shortprefix + "  Difference: " + ChatColor.RED + result.getDifference()));
                sender.sendMessage(playertext(shortprefix + "  Likelihood: " + ChatColor.RED + likelihood + ChatColor.RESET + "%"));

            });
        });

        commandManager.register("mob", (sender, params) -> {
            if (CommandValidate.notPlayer(sender))
                return;

            Player p = (Player) sender;
            Location spawnLoc = p.getLocation().add(p.getEyeLocation().getDirection().multiply(5));
            spawnLoc.setY(spawnLoc.getY() + 4);
            Zombie zombie = (Zombie) p.getWorld().spawnEntity(spawnLoc, EntityType.ZOMBIE);
            zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0D);
            zombie.setHealth(100.0D);
            p.sendMessage(playertext(prefix + "An test mob have been placed."));
            p.sendMessage(playertext(prefix + ChatColor.RED + "[Warning]" + ChatColor.RESET + " Results may vary depending if mob or player is used to train the Neural network."));
        });

        commandManager.register("_printnn", (sender, params) -> {
            neuralNetwork.printStats(getLogger());
            sender.sendMessage(playertext(prefix + "Check console!"));
        });
    }

    public void classifyPlayer(Player player, int duration, Consumer<LVQNeuralNetworkPredictResult> consumer) {
        if (attackAngleLogger.getRegisteredPlayers().contains(player.getName())) {
            player.sendMessage(playertext(prefix + ChatColor.RED + player.getName() + ChatColor.RESET + " is already in a sampling process. Please stop sampling first."));
            return;
        }
        attackAngleLogger.registerPlayer(player);
        getServer().getScheduler().runTaskLater(this, () -> {
            player.sendMessage(playertext(prefix + "Calculating!"));
            List<Float> angleSequence = attackAngleLogger.getLoggedAngles(player);
            attackAngleLogger.unregisterPlayer(player);
            attackAngleLogger.clearLoggedAngles(player);
            double[] extractedFeatures = SLMaths.extractFeatures(angleSequence);
            consumer.accept(neuralNetwork.predict(extractedFeatures));
        }, duration * 20L );
    }
    private void registerCategory(String name) {
        categoryNameMap.put(name, categoryNameMap.size());
    }
    public String getCategoryNameFromID(int id) {
        for (Map.Entry<String, Integer> entry : categoryNameMap.entrySet())
            if (entry.getValue() == id)
                return entry.getKey();
        return null;
    }
    private String getuuid(OfflinePlayer p) {
        if (p.hasPlayedBefore()) return "";
        return p.getUniqueId().toString();
    }
    public void unregisterAllCategories() {
        categoryNameMap.clear();
    }

    private static final class CommandValidate {
        private static boolean notPlayer(CommandSender sender) {
            if (!(sender instanceof Player))
                sender.sendMessage(Core.getInstance().playertext(getInstance().prefix + "This command can only be executed by a player."));
            return !(sender instanceof Player);
        }
    }
    private void rebuildNetworkWithDataset() {
        double step_size = getConfig().getDouble("LVQNN_parameters.step_size");
        double step_dec_rate = getConfig().getDouble("LVQNN_parameters.step_dec_rate");
        double min_step_size = getConfig().getDouble("LVQNN_parameters.min_step_Size");
        neuralNetwork = new LVQNeuralNetwork(FEATURE_COUNT, step_size, step_dec_rate, min_step_size);
        unregisterAllCategories();
        File[] categoryFiles = new File(getDataFolder(), "category").listFiles();
        if (categoryFiles == null){
            getLogger().severe("Unable to read dataset: 'category' is not a directory or an I/O error occurred!");
            return;
        }
        if (categoryFiles.length == 0) {
            getLogger().info("No files in: 'category'");
            return;
        }
        for (File categoryFile : categoryFiles)
            try {
                FileConfiguration categoryFileYaml = new YamlConfiguration();
                categoryFileYaml.load(categoryFile);
                List<List<Double>> categorySamples = (List<List<Double>>) categoryFileYaml.getList("samples");
                String categoryName = FilenameUtils.removeExtension(categoryFile.getName());
                if (!categoryNameMap.containsKey(categoryName))
                    registerCategory(categoryName);
                int categoryID = categoryNameMap.get(categoryName);
                for (List<Double> samples : categorySamples)
                    neuralNetwork.addData(new LabeledData(categoryID, samples.stream().mapToDouble(e -> e).toArray()));
            } catch (InvalidConfigurationException | IOException e) {
                e.printStackTrace();
                getLogger().severe("Unable to read dataset from '" + categoryFile.getName() + "'");

            }
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            int epoch = getConfig().getInt("LVQNN_train.epoch");
            neuralNetwork.normalize();
            neuralNetwork.initializeOutputLayer();

            synchronized (neuralNetwork) {
                for (int i = 0; i <= epoch - 1; i++)
                    neuralNetwork.train();
            }
        });
    }
    public void trainNetwork(Player player, String category, int duration_to_generate_a_vector, int vector_per_category) {
        if (attackAngleLogger.getRegisteredPlayers().contains(player.getName())){
            player.sendMessage(playertext(shortprefix + "Player is already in sampling process. Please stop it and try again."));
            return;
        }
        player.sendMessage(playertext(shortprefix + "Attempt to sample player's motion. " + ChatColor.DARK_RED + "(" + ChatColor.RESET
                + duration_to_generate_a_vector + " ms for a vector, "
                + vector_per_category + " vectors are needed in total" + ChatColor.DARK_RED + ")"));
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            double[][] vectors = new double[vector_per_category][Core.FEATURE_COUNT];
            attackAngleLogger.registerPlayer(player);

            for (int i = 1; i <= vector_per_category; i++) {
                player.sendMessage(playertext(shortprefix + "Sampling player's motion " + ChatColor.DARK_RED + "(" + ChatColor.RESET + i + "/" + vector_per_category + ChatColor.DARK_RED + ")"));
                try {
                    Thread.sleep(duration_to_generate_a_vector);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }

                List<Float> angleSequence = attackAngleLogger.getLoggedAngles(player);
                if (angleSequence == null)
                    break;
                vectors[i - 1] = SLMaths.extractFeatures(angleSequence);
                attackAngleLogger.clearLoggedAngles(player);
            }
            attackAngleLogger.unregisterPlayer(player);
            player.sendMessage(playertext(shortprefix + "Finished sampling player's motion. Saving samples..."));
            player.sendMessage(playertext(shortprefix + ChatColor.BOLD + ChatColor.RED + "WARNING" + ChatColor.RESET + " Remember more samplings may be needed for more accurate results!"));
            try {
                File saveFile = new File(getDataFolder(), Core.DIRNAME_CATEGORY + File.separator + category + ".yml");
                FileConfiguration saveFileYaml = new YamlConfiguration();

                List<Double[]> samplesSection = new ArrayList<>();
                if (!saveFile.createNewFile()) {
                    saveFileYaml.load(saveFile);
                    samplesSection.addAll((Collection<? extends Double[]>) saveFileYaml.getList("samples"));
                    player.sendMessage(playertext(shortprefix + "Category '" + ChatColor.RED + category + ChatColor.RESET
                            + "' already exists. Appending samples to the category..."));

                }
                for (double[] vector : vectors)
                    samplesSection.add(ArrayUtils.toObject(vector));
                saveFileYaml.set("samples", samplesSection);
                saveFileYaml.save(saveFile);
                player.sendMessage(playertext(shortprefix + "Samples saved."));
                player.sendMessage(playertext(prefix + "Rebuilding neural network with new dataset!"));
                this.rebuildNetworkWithDataset();

            } catch (IOException | InvalidConfigurationException e) {
                getLogger().severe("Unable to save sample for category '" + category + "'");
                e.printStackTrace();
                player.sendMessage(playertext(prefix + "Unable to save samples! This can be due to an I/O error."));
            }
        });
    }
}
