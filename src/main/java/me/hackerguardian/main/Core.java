package me.hackerguardian.main;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.hackerguardian.api.APICheck;
import me.hackerguardian.main.Checks.*;
import me.hackerguardian.main.Checks.combat.*;
import me.hackerguardian.main.Checks.movement.*;
import me.hackerguardian.main.Checks.world.*;
import me.hackerguardian.main.ML.LVQNeuralNetwork;
import me.hackerguardian.main.ML.LVQNeuralNetworkPredictResult;
import me.hackerguardian.main.ML.LVQNeuralNetworkSummary;
import me.hackerguardian.main.ML.LabeledData;
import me.hackerguardian.main.ML.listener.PlayerAttackAngleLogger;
import me.hackerguardian.main.Tps.Tps;
import me.hackerguardian.main.Utils.*;
import me.hackerguardian.main.getters.Timerlist;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.bukkit.*;
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
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.util.Map.Entry;
import org.bukkit.util.Vector;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Core extends JavaPlugin implements Listener {
    //General content.
    public List<Check> All_Checks = new ArrayList<Check>();
    public List<APICheck> All_Checks_API = new ArrayList<APICheck>();
    public List<String> che = new ArrayList<String>();
    public static Map<String, Integer> ALL_CHECKS = new HashMap<String, Integer>();
    public static List<String> NO_PUNISH_CHECKS;
    private static Object antiLock = new Object();
    public ExemptHandler EXEMPTHANDLER = null;
    public DamageHandler DAMAGEHANDLER = null;
    public List<Player> nonotify = new ArrayList<Player>();
    public String SUSPICION_ALERT = this.getConfig().getString("Messages.SUSPICION_ALERT");
    private static Map<Player, HashMap<Long, String>> reports = new HashMap<Player, HashMap<Long, String>>();
    public static int FEATURE_COUNT = 4;
    Logger logger = getLogger();
    public static Plugin plugin;
    public String prefix = "&4&l[&r&l&8HackerGuardian&r&4&l]&r ";
    public String shortprefix = "&4&l[&r&l&8HG&r&4&l]&r ";
    private CommandManager commandManager;
    public static Core core = null;
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
     * @return will return the text argument with colors if (&) and color codes are present.
     */
    public String playertext(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     *
     * @param check
     * @return adds the check to a list.
     */
    private void registerCheck(Check check) {
        if (!All_Checks.contains(check))
            All_Checks.add(check);
    }

    @Override
    public void onEnable() {
        core = this;
        Bukkit.getPluginManager().registerEvents(this, this);
        Messenger messenger = Bukkit.getMessenger();
        messenger.registerIncomingPluginChannel(this, "minecraft:brand", new ProtocollibListener());
        getServer().getConsoleSender().sendMessage(playertext("&4&l") +  "$$$$\\ " + ChatColor.BOLD + ChatColor.DARK_GRAY + "$$\\   $$\\                     $$\\                            $$$$$$\\                                      $$\\ $$\\                     " + ChatColor.BOLD + ChatColor.DARK_RED + "$$$$\\ ");
        getServer().getConsoleSender().sendMessage(playertext("&4&l") +  "$$  _|" + ChatColor.BOLD + ChatColor.DARK_GRAY + "$$ |  $$ |                    $$ |                          $$  __$$\\                                     $$ |\\__|                    " + ChatColor.BOLD + ChatColor.DARK_RED + "\\_$$ |");
        getServer().getConsoleSender().sendMessage(playertext("&4&l") +  "$$ |  " + ChatColor.BOLD + ChatColor.DARK_GRAY + "$$ |  $$ | $$$$$$\\   $$$$$$$\\ $$ |  $$\\  $$$$$$\\   $$$$$$\\  $$ /  \\__|$$\\   $$\\  $$$$$$\\   $$$$$$\\   $$$$$$$ |$$\\  $$$$$$\\  $$$$$$$\\" + ChatColor.BOLD + ChatColor.DARK_RED + "    $$ |");
        getServer().getConsoleSender().sendMessage(playertext("&4&l") +  "$$ |  " + ChatColor.BOLD + ChatColor.DARK_GRAY + "$$$$$$$$ | \\____$$\\ $$  _____|$$ | $$  |$$  __$$\\ $$  __$$\\ $$ |$$$$\\ $$ |  $$ | \\____$$\\ $$  __$$\\ $$  __$$ |$$ | \\____$$\\ $$  __$$\\"  + ChatColor.BOLD + ChatColor.DARK_RED + "   $$ |");
        getServer().getConsoleSender().sendMessage(playertext("&4&l") +  "$$ |  " + ChatColor.BOLD + ChatColor.DARK_GRAY + "$$  __$$ | $$$$$$$ |$$ /      $$$$$$  / $$$$$$$$ |$$ |  \\__|$$ |\\_$$ |$$ |  $$ | $$$$$$$ |$$ |  \\__|$$ /  $$ |$$ | $$$$$$$ |$$ |  $$ |" + ChatColor.BOLD + ChatColor.DARK_RED + "  $$ |");
        getServer().getConsoleSender().sendMessage(playertext("&4&l") +  "$$ |  " + ChatColor.BOLD + ChatColor.DARK_GRAY + "$$ |  $$ |$$  __$$ |$$ |      $$  _$$<  $$   ____|$$ |      $$ |  $$ |$$ |  $$ |$$  __$$ |$$ |      $$ |  $$ |$$ |$$  __$$ |$$ |  $$ |" + ChatColor.BOLD + ChatColor.DARK_RED + "  $$ |");
        getServer().getConsoleSender().sendMessage(playertext("&4&l") +  "$$$$\\ " + ChatColor.BOLD + ChatColor.DARK_GRAY + "$$ |  $$ |\\$$$$$$$ |\\$$$$$$$\\ $$ | \\$$\\ \\$$$$$$$\\ $$ |      \\$$$$$$  |\\$$$$$$  |\\$$$$$$$ |$$ |      \\$$$$$$$ |$$ |\\$$$$$$$ |$$ |  $$ |" + ChatColor.BOLD + ChatColor.DARK_RED + "$$$$ |");
        getServer().getConsoleSender().sendMessage(playertext("&4&l") +  "\\____|" + ChatColor.BOLD + ChatColor.DARK_GRAY + "\\__|  \\__| \\_______| \\_______|\\__|  \\__| \\_______|\\__|       \\______/  \\______/  \\_______|\\__|       \\_______|\\__| \\_______|\\__|  \\__|" + ChatColor.BOLD + ChatColor.DARK_RED + "\\____|");
        getServer().getConsoleSender().sendMessage(playertext(prefix + "Registering Commands"));

        commandManager = new CommandManager(this, "hackerguardian");
        registerCommand();
        getServer().getConsoleSender().sendMessage(playertext(prefix + "Registering Timers"));
        Timerlist.Timerlist();
        getServer().getConsoleSender().sendMessage(playertext(prefix + "Registering checks"));
        DAMAGEHANDLER = new DamageHandler(this);
        EXEMPTHANDLER = new ExemptHandler(this);
        new MovementHandler(this);
        new BlockHandler(this);
        new PlayerLogger(this);
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Tps(), 100L, 1L);


        //ML
        try {
            FileUtil.createDirectoryIfAbsent(getDataFolder(), DIRNAME_CATEGORY);
            FileUtil.createDirectoryIfAbsent(getDataFolder(), DIRNAME_DUMPED_DATA);
            FileUtil.saveResourceIfAbsent(this, "config.yml", "config.yml");
        } catch (IOException e) {
            getServer().getConsoleSender().sendMessage(playertext(prefix + "Was not able to save resource files"));
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }



        rebuildNetworkWithDataset();
        attackAngleLogger = new PlayerAttackAngleLogger();
        getServer().getPluginManager().registerEvents(attackAngleLogger, this);

        MySQL sql = new MySQL();
        sql.setupCoreSystem();
        if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            ProtocolManager manager = ProtocolLibrary.getProtocolManager();
            getServer().getConsoleSender().sendMessage(playertext(" manager: " + manager));
            manager.addPacketListener(new PacketAdapter(this,
                    ListenerPriority.HIGH,
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
        NO_PUNISH_CHECKS = getConfig().getStringList("no-punish");
        List<String> checks = new ArrayList<String>();
        checks.add("Flight");
        checks.add("Speed");
        checks.add("WaterWalk");
        checks.add("Glide/SlowFall");
        checks.add("Spider");
        checks.add("FastClimb");
        checks.add("Boat Fly");
        checks.add("Kill Aura");
        checks.add("Multi Aura");
        checks.add("Reach");
        checks.add("Impossible Break");
        checks.add("Impossible Place");
        checks.add("Fast Place");
        checks.add("Fast Break");
        checks.add("XRay");
        checks.add("Anti-Cactus");
        checks.add("Anti-BerryBush");
        checks.add("MorePackets (Timer)");
        checks.add("MorePackets (Nuker)");
        checks.add("Criticals");
        checks.add("Step");
        checks.add("Timer1");
        for (String s : checks) {
            if (!getConfig().contains(s + "-punish-count")){
                ALL_CHECKS.put(s, 20000);
                getServer().getConsoleSender().sendMessage(playertext(prefix + "No valid number at '" + s + "-punish-count' in config. Punishment count will be disabled!"));
            }else {
                try {
                    ALL_CHECKS.put(s, getConfig().getInt(s + "-punish-count"));
                } catch (Exception e) {
                    ALL_CHECKS.put(s, 20000);
                    getServer().getConsoleSender().sendMessage(playertext(prefix + "No valid number at '" + s + "-punish-count' in config. Punishment count will be disabled!"));
                }
            }
        }
        Bukkit.getScheduler().runTaskLater(this, () -> {
            this.registerCheck(new SpeedCheck());
            this.registerCheck(new SmartFlightCheck());
            this.registerCheck(new SmartSpeedCheck());
            this.registerCheck(new BreakCheck());
            this.registerCheck(new PlaceCheck());
            this.registerCheck(new KillAuraCheck());
            this.registerCheck(new MultiAuraCheck());
            this.registerCheck(new BoatCheck());
            this.registerCheck(new WaterCheck());
            this.registerCheck(new HoverCheck());
            this.registerCheck(new FloatCheck());
            this.registerCheck(new ReachCheck());
            this.registerCheck(new EntitySpeedCheck());
            this.registerCheck(new XRayCheck());
            this.registerCheck(new AntiCactusBerryCheck());
            this.registerCheck(new CriticalCheck());
            this.registerCheck(new FlightFCheck());
            //this.registerCheck(new TimerCheck());
            getServer().getConsoleSender().sendMessage(playertext(prefix + "Successfully registered every check!"));
        }, 100L);
    }

    /*
    Bruges some core function til alt der omhandler join system
     */
    @EventHandler
    public void onjoin(PlayerJoinEvent event) {
        MySQL sql = new MySQL();
        //TODO Gør så denne function tjekker om de er i DB'en istede for at bruge "hasPlayedBefore()"
        if (!event.getPlayer().hasPlayedBefore()){
            sql.setplayerstatsban(event.getPlayer().getUniqueId(), "false", "false");
            event.getPlayer().kickPlayer("Timeout");
        }
        if (getConfig().getBoolean("Settings.logplayerip")){
            String ip = event.getPlayer().getAddress().getAddress().toString();
            sql.addPlayerIP(event.getPlayer().getUniqueId(), ip);
        }
        //TODO Få en function der tjekker om spillern er ip-bannet, bannet, eller temp bannet.
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        LocalDateTime playerlogin = LocalDateTime.now();
        sql.setJoinTime(event.getPlayer().getUniqueId(), dtf.format(playerlogin));

    }

    public int getVersion(final Player player) {
        return playerVersions.get(player.getAddress().getAddress());
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
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        getServer().getConsoleSender().sendMessage(playertext("&4&l") +  "$$$$\\ " + ChatColor.BOLD + ChatColor.DARK_GRAY + "$$\\   $$\\                     $$\\                            $$$$$$\\                                      $$\\ $$\\                     " + ChatColor.BOLD + ChatColor.DARK_RED + "$$$$\\ ");
        getServer().getConsoleSender().sendMessage(playertext("&4&l") +  "$$  _|" + ChatColor.BOLD + ChatColor.DARK_GRAY + "$$ |  $$ |                    $$ |                          $$  __$$\\                                     $$ |\\__|                    " + ChatColor.BOLD + ChatColor.DARK_RED + "\\_$$ |");
        getServer().getConsoleSender().sendMessage(playertext("&4&l") +  "$$ |  " + ChatColor.BOLD + ChatColor.DARK_GRAY + "$$ |  $$ | $$$$$$\\   $$$$$$$\\ $$ |  $$\\  $$$$$$\\   $$$$$$\\  $$ /  \\__|$$\\   $$\\  $$$$$$\\   $$$$$$\\   $$$$$$$ |$$\\  $$$$$$\\  $$$$$$$\\" + ChatColor.BOLD + ChatColor.DARK_RED + "    $$ |");
        getServer().getConsoleSender().sendMessage(playertext("&4&l") +  "$$ |  " + ChatColor.BOLD + ChatColor.DARK_GRAY + "$$$$$$$$ | \\____$$\\ $$  _____|$$ | $$  |$$  __$$\\ $$  __$$\\ $$ |$$$$\\ $$ |  $$ | \\____$$\\ $$  __$$\\ $$  __$$ |$$ | \\____$$\\ $$  __$$\\"  + ChatColor.BOLD + ChatColor.DARK_RED + "   $$ |");
        getServer().getConsoleSender().sendMessage(playertext("&4&l") +  "$$ |  " + ChatColor.BOLD + ChatColor.DARK_GRAY + "$$  __$$ | $$$$$$$ |$$ /      $$$$$$  / $$$$$$$$ |$$ |  \\__|$$ |\\_$$ |$$ |  $$ | $$$$$$$ |$$ |  \\__|$$ /  $$ |$$ | $$$$$$$ |$$ |  $$ |" + ChatColor.BOLD + ChatColor.DARK_RED + "  $$ |");
        getServer().getConsoleSender().sendMessage(playertext("&4&l") +  "$$ |  " + ChatColor.BOLD + ChatColor.DARK_GRAY + "$$ |  $$ |$$  __$$ |$$ |      $$  _$$<  $$   ____|$$ |      $$ |  $$ |$$ |  $$ |$$  __$$ |$$ |      $$ |  $$ |$$ |$$  __$$ |$$ |  $$ |" + ChatColor.BOLD + ChatColor.DARK_RED + "  $$ |");
        getServer().getConsoleSender().sendMessage(playertext("&4&l") +  "$$$$\\ " + ChatColor.BOLD + ChatColor.DARK_GRAY + "$$ |  $$ |\\$$$$$$$ |\\$$$$$$$\\ $$ | \\$$\\ \\$$$$$$$\\ $$ |      \\$$$$$$  |\\$$$$$$  |\\$$$$$$$ |$$ |      \\$$$$$$$ |$$ |\\$$$$$$$ |$$ |  $$ |" + ChatColor.BOLD + ChatColor.DARK_RED + "$$$$ |");
        getServer().getConsoleSender().sendMessage(playertext("&4&l") +  "\\____|" + ChatColor.BOLD + ChatColor.DARK_GRAY + "\\__|  \\__| \\_______| \\_______|\\__|  \\__| \\_______|\\__|       \\______/  \\______/  \\_______|\\__|       \\_______|\\__| \\_______|\\__|  \\__|" + ChatColor.BOLD + ChatColor.DARK_RED + "\\____|");
    }


    private void registerCommand(){
        commandManager.register("", (sender, params) -> {
            if (sender.hasPermission("hg.main") || sender.hasPermission("hg.*")) {
                sender.sendMessage(playertext("&8&l<&7&m-------------]&r&l&4Help&7&m[-------------&r&8&l>"));
                sender.sendMessage(playertext(shortprefix + "Hello " + sender.getName() + ". This server is running a very early stage of " + prefix));
                sender.sendMessage(playertext(shortprefix + "Please be aware of issues that can stright out break the plugin."));
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
                    sender.sendMessage(playertext(prefix + "Sorry but " + params[0] + " is not a number."));
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

        commandManager.register("checkbannedip", ((sender, params) -> {
            if (CommandValidate.notPlayer(sender)) return;
            MySQL sql = new MySQL();
            InetAddressValidator validator = InetAddressValidator.getInstance();
            if (params.length == 0) {
                sender.sendMessage(playertext(prefix + "Wrong parameters! /hg checkbannedip <ip>"));
                sender.sendMessage(playertext(shortprefix + "This will check how many players have been banned on the IP."));
                return;
            }
            if (params.length == 1) {
                if (sql.getbannedip(params[0]) == null){
                    sender.sendMessage(playertext(prefix + "Error trying to check banned ip! Please note an Administrator!"));
                    return;
                }
                if (validator.isValidInet4Address(params[0])) {
                    String ip = params[0];
                    sender.sendMessage(playertext(prefix + "There is: " + sql.getbannedip(ip) + " Players banned on this IP."));
                    return;
                } else if (validator.isValidInet6Address(params[0])){
                    String ip = params[0];
                    sender.sendMessage(playertext(prefix + "There is: "+ sql.getbannedip(ip) + " Players banned on this IP."));
                    return;
                }else {
                    sender.sendMessage(playertext(prefix + "Please use a valid IP address!"));
                    sender.sendMessage(playertext(shortprefix + "Example: IPv4 127.0.0.1, \n IPv6 2001:0db8:0001:0000:0000:0ab9:C0A8:0102"));
                }


            }

        }));
        commandManager.register("set", ((sender, params) -> {
            MySQL sql = new MySQL();
            if (params.length != 1) {
                sender.sendMessage(playertext(prefix + "Wrong parameters! /hg set <player-name>"));
            }else {
                Player p = getServer().getPlayer(params[0]);
                sql.setplayerstatsban(p.getUniqueId(), "false", "false");
            }
        }));
        commandManager.register("view", ((sender, params) -> {
            MySQL sql = new MySQL();
            Player p = getServer().getPlayer(params[0]);
            if (params.length != 1 && params.length != 2) {
                sender.sendMessage(playertext(prefix + "Wrong parameters! /hg view <player-name> [page 1-3]"));
                return;
            }
            if (!p.isOnline()){
                sender.sendMessage(playertext(prefix + ChatColor.RED + params[0] + ChatColor.RESET + " is not online!"));
                return;
            }
            if (!getServer().getOfflinePlayer(params[0]).hasPlayedBefore()){
                sender.sendMessage(playertext(prefix + ChatColor.RED + params[0] + ChatColor.RESET + " have never joined this server."));
                return;
            }
            if (params.length == 1){
                sender.sendMessage(playertext(prefix + "Statistics collected about '" + ChatColor.RED + params[0] + ChatColor.RESET + "'"));
                if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
                    sender.sendMessage(playertext(shortprefix + "Player client version: '" + ChatColor.RED + "protocollib is broken." + ChatColor.RESET + "'"));
                }else {
                    sender.sendMessage(playertext(shortprefix + "Player client version: '" + ChatColor.RED + "Unknown" + ChatColor.RESET + "'"));
                }
                sender.sendMessage(playertext(shortprefix + "Player last used client: '" + ChatColor.RED + sql.getuser(p.getUniqueId()) + ChatColor.RESET + "'"));
                sender.sendMessage(playertext(shortprefix + "Player last login: '" + ChatColor.RED + sql.getplayerjointime(p.getUniqueId()) + ChatColor.RESET + "'" ));
                sender.sendMessage(playertext(shortprefix + "Player average CPS: '" + ChatColor.RED + "SOON" + ChatColor.RESET + "'"));
                sender.sendMessage(playertext(shortprefix + "Player in banwave queue: '" + ChatColor.RED + sql.getplayerbwstatus(p.getUniqueId()) + ChatColor.RESET + "'"));
                sender.sendMessage(playertext(shortprefix + "------------- Page 1/4 -------------"));
                return;
            }

            if (params.length >= 2){
                if (!StringUtils.isNumeric(params[1])) {
                    sender.sendMessage(playertext(prefix + ChatColor.RED + params[1] + ChatColor.RESET + " is not a valid number."));
                    return;
                }
                int viewnumber = Integer.valueOf(params[1]);

                if (viewnumber == 1) {
                    sender.sendMessage(playertext(prefix + "Statistics collected about '" + ChatColor.RED + params[0] + ChatColor.RESET + "'"));
                    if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
                        sender.sendMessage(playertext(shortprefix + "Player client version: '" + ChatColor.RED + "protocollib is broken." /*getVersion(p) */ + ChatColor.RESET + "'"));
                    }else {
                        sender.sendMessage(playertext(shortprefix + "Player client version: '" + ChatColor.RED + "Unknown" + ChatColor.RESET + "'"));
                    }
                    sender.sendMessage(playertext(shortprefix + "Player last used client: '" + ChatColor.RED + sql.getuser(p.getUniqueId()) + ChatColor.RESET + "'"));
                    sender.sendMessage(playertext(shortprefix + "Player last login: '" + ChatColor.RED + sql.getplayerjointime(p.getUniqueId()) + ChatColor.RESET + "'" ));
                    sender.sendMessage(playertext(shortprefix + "Player average CPS: '" + ChatColor.RED + "SOON" + ChatColor.RESET + "'"));
                    sender.sendMessage(playertext(shortprefix + "Player in banwave queue: '" + ChatColor.RED + sql.getplayerbwstatus(p.getUniqueId()) + ChatColor.RESET + "'"));
                    sender.sendMessage(playertext(shortprefix + "------------- Page 1/4 -------------"));
                    return;
                }else if (viewnumber == 2){
                    sender.sendMessage(playertext(prefix + "Statistics collected about '" + ChatColor.RED + params[0] + ChatColor.RESET + "'"));
                    //sender.sendMessage(playertext(shortprefix + "Player average CPS: '" + ChatColor.RED + "ERROR" + ChatColor.RESET + "'"));
                    sender.sendMessage(playertext(shortprefix + "Is Player banned: '" + ChatColor.RED + sql.getplayerban(p.getUniqueId()) + ChatColor.RESET + "'"));
                    sender.sendMessage(playertext(shortprefix + "Is Player muted: '" + ChatColor.RED + sql.getisplayermuted(p.getUniqueId()) + ChatColor.RESET + "'"));
                    sender.sendMessage(playertext(shortprefix + "Player mute amount: '" + ChatColor.RED + sql.getplayermute(p.getUniqueId()) + ChatColor.RESET + "'"));
                    sender.sendMessage(playertext(shortprefix + "Player kick amount: '" + ChatColor.RED + sql.getplayerkick(p.getUniqueId()) + ChatColor.RESET + "'"));
                    if (getConfig().getBoolean("Settings.logplayerip")){
                        sender.sendMessage(playertext(shortprefix + "Player's last (1-" + this.getConfig().getInt("Settings.MaxIPListCount") + ") login IP's:"));
                        try {
                            sql.getPlayerIp(p.getUniqueId()).forEach((IP) -> sender.sendMessage("  - " + IP.toString().replaceAll("/", "")));
                        } catch (Exception e) {
                            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
                        }
                    }else {
                        sender.sendMessage(playertext(shortprefix + "Player's last (1 -" + this.getConfig().getInt("Settings.MaxIPListCount") + ") login IP's:"));
                        sender.sendMessage(playertext(shortprefix + "Due to safety reasons we keep this safe."));
                    }
                    sender.sendMessage(playertext(shortprefix + "------------- Page 2/4 -------------"));
                    return;
                }else if (viewnumber == 3) {
                    sender.sendMessage(playertext(prefix + "Statistics collected about '" + ChatColor.RED + params[0] + ChatColor.RESET + "'"));
                    sender.sendMessage(playertext(shortprefix + "Player's last (1 -" + this.getConfig().getInt("Settings.MaxReasonListCount") + ") system triggers:"));
                    try {
                        sql.getPlayerTriggers(p.getUniqueId()).forEach((Reason) -> sender.sendMessage("  - " + Reason.toString()));
                    } catch (Exception e) {
                        if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
                    }
                    sender.sendMessage(playertext(shortprefix + "------------- Page 3/4 -------------"));
                    return;
                }else if (viewnumber == 4) {
                    sender.sendMessage(playertext(prefix + "Statistics collected about '" + ChatColor.RED + params[0] + ChatColor.RESET + "'"));
                    sender.sendMessage(playertext(shortprefix + "Player's last (1 - " + this.getConfig().getInt("Settings.MaxHandlerListCount") + ") kick/mute/ban/ip-ban/temp-ban:"));
                    try {

                        sql.getPlayerhandler(p.getUniqueId()).forEach((Handler) -> sender.sendMessage(Handler.toString()));
                        sql.getPlayerhandlerReasons(p.getUniqueId()).forEach((Reason) -> sender.sendMessage( "  - " + Reason.toString()));
                    } catch(Exception e) {
                        if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
                    }
                    sender.sendMessage(playertext(shortprefix + "------------- Page 4/4 -------------"));
                    return;
                }else{
                    sender.sendMessage(playertext(prefix + ChatColor.RED + params[1] + ChatColor.RESET + " is not a valid list number."));
                    return;
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
                }*/
                if (reported.equals(reportedBy)) {
                    sender.sendMessage(playertext(prefix + "You can't report yourself!"));
                    return;
                }
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
        commandManager.register("tps", ((sender, params) -> {
            Player player = Bukkit.getPlayer(sender.getName());
            if (!(sender.hasPermission("*")) || !(sender.hasPermission("hg.kick"))) {
                if (params.length == 0){
                    sender.sendMessage(playertext(prefix + "Sorry but it seems you are missing the right privileges to run this command!"));
                    sender.sendMessage(playertext(shortprefix + "If you believe this is an error please report to an Administrator!"));
                    return;
                }else {
                    sender.sendMessage(playertext(prefix + "Sorry but it seems you are missing the right privileges to run this command!"));
                    sender.sendMessage(playertext(shortprefix + "If you believe this is an error please report to an Administrator!"));
                    return;
                }
            }
            if (params.length == 0) {
                sender.sendMessage(playertext(prefix + "Current TPS: " + Tps.getTPS()));

                return;
            }else {
                sender.sendMessage(playertext(prefix + "Current TPS: " + Tps.getTPS()));

                return;
            }
        }));
        commandManager.register("kick", ((sender, params) -> {
            MySQL sql = new MySQL();
            StringBuilder reason = new StringBuilder();
            for (int i = 1; i < params.length; i++){
                reason.append(params[i]);
                reason.append(" ");
            }
            if (!(sender.hasPermission("*")) || !(sender.hasPermission("hg.kick"))) {
                if (params.length != 0){
                    sender.sendMessage(playertext(prefix + "Sorry but it seems you are missing the right privileges to run this command!"));
                    sender.sendMessage(playertext(shortprefix + "If you believe this is an error please report to an Administrator!"));
                    return;
                } else {
                    sender.sendMessage(playertext(prefix + "Sorry but it seems you are missing the right privileges to run this command!"));
                    sender.sendMessage(playertext(shortprefix + "If you believe this is an error please report to an Administrator!"));
                    return;

                }
            }

            if (params.length == 0) {
                sender.sendMessage(playertext(prefix + "Wrong parameters! /hg kick <player> <reason>"));
                return;
            }
            if (params.length == 1) {
                sender.sendMessage(playertext(prefix + "Okay you wanna kick '" + ChatColor.RED + params[0] + ChatColor.RESET + "', but you will need to supply a reason."));
                return;
            }
            Player p1 = Bukkit.getPlayer(params[0]);
            if ((p1.hasPermission("*")) || (p1.hasPermission("hg.*"))) {
                sender.sendMessage(playertext(prefix + "Sorry but we are unable to ban an operator!"));
                sender.sendMessage(playertext(shortprefix + "If you believe this is an error please report to an Administrator!"));
                return;
            }
            if (params.length >= 2) {
                try {
                    Player p = Bukkit.getPlayer(params[0]);
                    String time = (new SimpleDateFormat("YYYY-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime());
                    if (sender instanceof Player) {
                        p.kickPlayer(playertext(shortprefix + "You where kicked for ") + ChatColor.RED + reason.toString() + ChatColor.RESET + "\n by " + ChatColor.RED + sender.getName() + ChatColor.RESET + " at " + ChatColor.RED + String.valueOf(time));
                        sender.sendMessage(playertext(prefix + "Kicked '" + ChatColor.RED + params[0] + ChatColor.RESET + "' for " + ChatColor.RED + reason.toString() + ChatColor.RESET + ""));
                    }else {
                        p.kickPlayer(playertext(shortprefix + "You where kicked for ") + ChatColor.RED + reason.toString() + ChatColor.RESET + "\n by " + ChatColor.RED + "Console" + ChatColor.RESET + " at " + ChatColor.RED + String.valueOf(time));
                        sender.sendMessage(playertext(prefix + "Kicked '" + ChatColor.RED + params[0] + ChatColor.RESET + "' for " + ChatColor.RED + reason.toString() + ChatColor.RESET + ""));
                    }
                    sql.addPlayerHandlerReasons(p.getUniqueId(), "Kick", reason.toString());
                    sql.addplayerkicks(p.getUniqueId(), 1);
                } catch (Exception e) {
                    if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();

                }
            }

        }));
        commandManager.register("unmute", ((sender, params) -> {
            StringBuilder reason = new StringBuilder();
            MySQL sql = new MySQL();
            for (int i = 2; i < params.length; i++){
                reason.append(params[i]);
                reason.append(" ");
            }
            if (!(sender.hasPermission("*")) || !(sender.hasPermission("hg.unmute"))) {
                if (params.length == 0){
                    sender.sendMessage(playertext(prefix + "Sorry but it seems you are missing the right privileges to run this command!"));
                    sender.sendMessage(playertext(shortprefix + "If you believe this is an error please report to an Administrator!"));
                    return;
                }else {
                    sender.sendMessage(playertext(prefix + "Sorry but it seems you are missing the right privileges to run this command!"));
                    sender.sendMessage(playertext(shortprefix + "If you believe this is an error please report to an Administrator!"));
                    return;
                }
            }


            if (params.length == 0){
                sender.sendMessage(playertext(prefix + "Wrong parameters! /hg unmute <player>"));
            }
            Player p = Bukkit.getPlayer(params[0]);
            if (params.length ==1) {
                /*if (p.hasPermission("*") || p.hasPermission("hg.mute")){
                    sender.sendMessage(playertext(prefix + "Sorry but we are unable to mute an operator!"));
                    sender.sendMessage(playertext(shortprefix + "If you believe this is an error please report to an Administrator!"));
                    return;
                }*/
                if (sql.getisplayermuted(p.getUniqueId()).equalsIgnoreCase("false")) {
                    sender.sendMessage(playertext(prefix + "Sorry but this player is not muted!"));
                    return;
                }else {
                    sql.removeplayermute(p.getUniqueId());
                    p.sendMessage(playertext(prefix + "You've been unmuted!"));
                    sender.sendMessage(playertext(prefix + "Successfully unmuted '" + ChatColor.RED + p.getName() + ChatColor.RESET + "'"));
                }
                return;
            }



        }));
        commandManager.register("mute", ((sender, params) -> {
            StringBuilder reason = new StringBuilder();
            MySQL sql = new MySQL();
            for (int i = 1; i < params.length; i++){
                reason.append(params[i]);
                reason.append(" ");
            }
            if (!(sender.hasPermission("*")) || !(sender.hasPermission("hg.mute"))) {
                if (params.length == 0){
                    sender.sendMessage(playertext(prefix + "Sorry but it seems you are missing the right privileges to run this command!"));
                    sender.sendMessage(playertext(shortprefix + "If you believe this is an error please report to an Administrator!"));
                    return;
                }else {
                    sender.sendMessage(playertext(prefix + "Sorry but it seems you are missing the right privileges to run this command!"));
                    sender.sendMessage(playertext(shortprefix + "If you believe this is an error please report to an Administrator!"));
                    return;
                }
            }


            if (params.length == 0){
                sender.sendMessage(playertext(prefix + "Wrong parameters! /hg mute <player> <reason>"));
                return;
            }


            if (params.length == 1){
                sender.sendMessage(playertext(prefix + "Okay you wanna mute '" + ChatColor.RED + params[0] + ChatColor.RESET + "', but you will need to supply a reason."));
                return;
            }
            Player p = Bukkit.getPlayer(params[0]);
            if (params.length >= 2) {
                /*if (p.hasPermission("*") || p.hasPermission("hg.mute")){
                    sender.sendMessage(playertext(prefix + "Sorry but we are unable to mute an operator!"));
                    sender.sendMessage(playertext(shortprefix + "If you believe this is an error please report to an Administrator!"));
                    return;
                }*/
                if (sql.getisplayermuted(p.getUniqueId()).equalsIgnoreCase("true")) {
                    sender.sendMessage(playertext(prefix + "Sorry but this player is already muted!"));
                    return;
                }else {
                    sql.addplayermute(p.getUniqueId(), 1);
                    sql.addPlayerHandlerReasons(p.getUniqueId(), "Mute", reason.toString());
                    p.sendMessage(playertext(prefix + "You where muted for: " + reason.toString()));
                    sender.sendMessage(playertext(prefix + "Successfully muted '" + ChatColor.RED + p.getName() + ChatColor.RESET + "'"));
                }
                return;
            }

        }));
        commandManager.register("ban", ((sender, params) -> {
            StringBuilder reason = new StringBuilder();
            for (int i = 1; i < params.length; i++){
                reason.append(params[i]);
                reason.append(" ");
            }
            if (!(sender.hasPermission("*")) || !(sender.hasPermission("hg.ban"))) {
                if (params.length == 0){
                    sender.sendMessage(playertext(prefix + "Sorry but it seems you are missing the right privileges to run this command!"));
                    sender.sendMessage(playertext(shortprefix + "If you believe this is an error please report to an Administrator!"));
                    return;
                }else {
                    sender.sendMessage(playertext(prefix + "Sorry but it seems you are missing the right privileges to run this command!"));
                    sender.sendMessage(playertext(shortprefix + "If you believe this is an error please report to an Administrator!"));
                    return;
                }
            }
            if (params.length == 0){
                sender.sendMessage(playertext(prefix + "Wrong parameters! /hg ban <player> <reason>"));
            }
        }));
        commandManager.register("ban-ip", ((sender, params) -> {
            StringBuilder reason = new StringBuilder();
            for (int i = 1; i < params.length; i++){
                reason.append(params[i]);
                reason.append(" ");
            }
            if (!(sender.hasPermission("*")) || !(sender.hasPermission("hg.ban-ip"))) {
                if (params.length == 0){
                    sender.sendMessage(playertext(prefix + "Sorry but it seems you are missing the right privileges to run this command!"));
                    sender.sendMessage(playertext(shortprefix + "If you believe this is an error please report to an Administrator!"));
                    return;
                }else {
                    sender.sendMessage(playertext(prefix + "Sorry but it seems you are missing the right privileges to run this command!"));
                    sender.sendMessage(playertext(shortprefix + "If you believe this is an error please report to an Administrator!"));
                    return;
                }
            }
            if (params.length == 0){
                sender.sendMessage(playertext(prefix + "Wrong parameters! /hg ban-ip <ip> <reason>"));
            }
        }));
        commandManager.register("temp-ban", ((sender, params) -> {
            StringBuilder reason = new StringBuilder();
            for (int i = 2; i < params.length; i++){
                reason.append(params[i]);
                reason.append(" ");
            }
            if (!(sender.hasPermission("*")) || !(sender.hasPermission("hg.temp-ban"))) {
                if (params.length == 0){
                    sender.sendMessage(playertext(prefix + "Sorry but it seems you are missing the right privileges to run this command!"));
                    sender.sendMessage(playertext(shortprefix + "If you believe this is an error please report to an Administrator!"));
                    return;
                }else {
                    sender.sendMessage(playertext(prefix + "Sorry but it seems you are missing the right privileges to run this command!"));
                    sender.sendMessage(playertext(shortprefix + "If you believe this is an error please report to an Administrator!"));
                    return;
                }
            }
            if (params.length == 0){
                sender.sendMessage(playertext(prefix + "Wrong parameters! /hg temp-ban <player> <time> <reason>"));
            }
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
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(params[1]);
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
                                        sender.sendMessage(playertext(shortprefix + "We found a total number of: " + ChatColor.RED + String.valueOf(reportsize) + ChatColor.RESET + " report in records."));
                                        //stuff
                                        if (reportsize >= 2) {
                                            sender.sendMessage(playertext(shortprefix + "You can do: " + ChatColor.RED + "/hg reports view " + params[1] + " 2" + ChatColor.RESET + " to view the next record."));
                                        }
                                    }
                                } catch (Exception e2) {
                                    if (Core.getInstance().getConfig().getBoolean("debug")) e2.printStackTrace();

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
                getServer().getConsoleSender().sendMessage(playertext(prefix + "Unable to dump vector and angles of player '" + player.getName() + "'"));
                if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
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
            try {
                sql.shutdowndatabase();
                sql.setupCoreSystem();
            } catch (Exception e) {
                sender.sendMessage(playertext(prefix + "An error has occured on reloading Database!"));
                sender.sendMessage(playertext(shortprefix + "Please note an Administrator!"));
                if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
            }
            reloadConfig();
            sender.sendMessage(playertext(prefix + "Reloaded configuration."));
            return;
        });

        commandManager.register("resetdb", ((sender, params) -> {
            if (CommandValidate.console(sender)) return;
            if (params.length == 0){
                sender.sendMessage(playertext(shortprefix + ChatColor.RED + "[WARNING] THIS WILL ERASE EVERYTHING IN THE DATABASE OF HACKERGUARDIAN!"));
                sender.sendMessage(playertext(prefix + "Please confirm the reset!"));
                sender.sendMessage(playertext(shortprefix + "/hg resetdb confirm"));
            }
            if (params.length == 1) {
                sender.sendMessage(playertext(prefix + "Trying to erase database!"));
            }


        }));

        /*commandManager.register("something", ((sender, params) -> {
            if (params.length == 0){
                sender.sendMessage(playertext(prefix + "Wrong parameters! /hg dtestt <player> <time>"));
                sender.sendMessage(playertext(shortprefix + "Time example: 1h30m"));
                return;
            }
            Player testplayer = getServer().getPlayer(params[0]);
            if (testplayer == null || !testplayer.isOnline()) {
                sender.sendMessage(prefix + "This player isn't online!");
                return;
            }
            long expire = UtilTime.parseDateDiff(params[1], true);
            if (expire == 0) {
                sender.sendMessage(prefix + "That's not a valid time! Example: 1d5h3m");
                return;
            }
            EXEMPTHANDLER.addExemption(testplayer, (int) (expire - System.currentTimeMillis()), "");
            for (Player s : getServer().getOnlinePlayers()) {
                s.sendMessage(playertext(prefix) + "test");
            }
        }));*/
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
                    + ChatColor.RESET + "' for " + ChatColor.RED + duration + ChatColor.RESET + " seconds"));
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
            try {
                List<Float> angleSequence = attackAngleLogger.getLoggedAngles(player);
                attackAngleLogger.unregisterPlayer(player);
                attackAngleLogger.clearLoggedAngles(player);
                double[] extractedFeatures = SLMaths.extractFeatures(angleSequence);
                consumer.accept(neuralNetwork.predict(extractedFeatures));
            } catch (Exception e) {
                player.sendMessage(playertext(prefix + "Failed to calculate! Please send this to an administrator!"));
                if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
            }
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
        private static boolean console(CommandSender sender) {
            if (sender instanceof Player)
                sender.sendMessage(Core.getInstance().playertext(getInstance().prefix + "This command can only be executed in console."));
            return (sender instanceof Player);
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
            getServer().getConsoleSender().sendMessage(playertext(prefix + "Unable to read dataset: 'category' is not a directory or an I/O error occurred!"));
            return;
        }
        if (categoryFiles.length == 0) {
            getServer().getConsoleSender().sendMessage(playertext(prefix + "No files in: 'category'"));
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
                if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
                getServer().getConsoleSender().sendMessage(playertext(prefix + "Unable to read dataset from '" + categoryFile.getName() + "'"));

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
                    if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
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
                getServer().getConsoleSender().sendMessage(playertext(prefix + "Unable to save sample for category '" + category + "'"));
                if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
                player.sendMessage(playertext(prefix + "Unable to save samples! This can be due to an I/O error."));
            }
        });
    }

    public String getCSADDRESS() {
        String ip = Bukkit.getServer().getIp();
        if (ip == null || ip.length() == 0) {
            ip = "127.0.0.1";
        }
        return ip + ":" + Bukkit.getServer().getPort();
    }
    public User getUser(Player p) {
        return new User(p);
    }

    private boolean updateDatabase(Player p, String detector, int counts, String description) {
        MySQL sql = new MySQL();
        synchronized (antiLock) {
            List<Long> remove = new ArrayList<Long>();
            Iterator<Long> i = reports.get(p).keySet().iterator();
            while (i.hasNext()) {
                Long l = i.next();
                if (System.currentTimeMillis() > l) {
                    if (!remove.contains(l)) {
                        remove.add(l);
                    }
                } else {

                }
            }
            for (Long r : remove) {
                if (reports.get(p).containsKey(r)) {
                    reports.get(p).remove(r);
                }
            }

            Boolean punishsusc = false;
            Map<String, Integer> pdata = playerdata.UC.get(p.getUniqueId());
            Integer susc = 0;
            String check = "";
            for (Entry<String, Integer> v : pdata.entrySet()) {
                susc += v.getValue();
                if (ALL_CHECKS.containsKey(v.getKey())) {
                    Integer cc = v.getValue();
                    Integer limit = ALL_CHECKS.get(v.getKey());
                    if (limit <= cc) {
                        check = v.getKey();
                        punishsusc = true;
                    }

                }
            }

            if (punishsusc && !NO_PUNISH_CHECKS.contains(check)) {
                this.EXEMPTHANDLER.addExemption(p, 5000, "Punishment Applied");

                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    String reportslist = "";
                    Map<String, Integer> od = new HashMap<String, Integer>();
                    try {
                        for (String s : reports.get(p).values()) {
                            Integer count = 1;
                            if (od.containsKey(s)) {
                                count = od.get(s) + 1;
                                od.remove(s);
                            }
                            od.put(s, count);
                        }
                    } catch (Exception e) {
                    }
                    for (Entry<String, Integer> e : od.entrySet()) {
                        reportslist = reportslist + e.getKey() + "(" + e.getValue() + "), ";
                    }
                    try {
                        reportslist = reportslist.substring(0, reportslist.length() - 2);
                    } catch (Exception e) {

                    }
                    p.setVelocity(new Vector(0, 0, 0));
                    String m = "hg kick [USERNAME] [SUSPICION] cheats.";
                    m = m.replaceAll("\\[VARIABLE_COLOR\\]", playertext(shortprefix + "&a"));
                    m = m.replaceAll("\\[DISPLAYNAME\\]", p.getDisplayName());
                    m = m.replaceAll("\\[USERNAME\\]", p.getName());
                    m = m.replaceAll("\\[NAME\\]", p.getName());
                    m = m.replaceAll("\\[UUID\\]", p.getUniqueId().toString());
                    m = m.replaceAll("\\[SUSPICION\\]", detector);
                    m = m.replaceAll("\\[OFFENSES\\]", reportslist);
                    final String me = m;
                    playerdata.UC.remove(p.getUniqueId());

                    Bukkit.getScheduler().runTask(this, () -> {
                        try{
                            Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), me);

                        } catch (Exception e) {
                            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
                        }

                    });
                    sql.addPlayerTriggers(p.getUniqueId(), detector.replace("'", "\\'"));

                });
                return true;
            } else {
                return false;
            }
        }

    }
    @EventHandler
    public void playerchat(PlayerChatEvent e) {
        Player p = e.getPlayer();
        MySQL sql = new MySQL();
        if (sql.getisplayermuted(p.getUniqueId()).equalsIgnoreCase("true")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        if (reports.containsKey(p))
            reports.remove(p);
        if (EXEMPTHANDLER.isExempt(p))
            EXEMPTHANDLER.removeExemption(p);
    }
    public boolean addSuspicion(Player p, String detector, String description) {
        MySQL sql = new MySQL();
        if (!reports.containsKey(p))
            reports.put(p, new HashMap<Long, String>());

        if (EXEMPTHANDLER.isExempt(p)) {
            return false;
        }
        this.getUser(p).updateLastOffense();

        int ping = 0;

        try {
            Object entityPlayer = p.getClass().getMethod("getHandle").invoke(p);
            ping = (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
        } catch (Exception e) {
            if (Core.getInstance().getConfig().getBoolean("debug")) e.printStackTrace();
        }
        if (ping > 0)
            ping = ping / 2;

        int Count = 0;
        reports.get(p).put((System.currentTimeMillis() + (System.currentTimeMillis() * 10)), detector);

        for (String v : reports.get(p).values()) {
            if (v.equalsIgnoreCase(detector)) {
                Count++;
            }
        }
        if (Count <= 2) {
            return false;
        }
        if (p.getVehicle() == null) {
            EXEMPTHANDLER.addExemptionBlock(p, 100);
            if (detector.equalsIgnoreCase("Anti-Cactus") || detector.equalsIgnoreCase("Anti-BerryBush")) {
                p.damage(0.5D);
            } else if (detector.equalsIgnoreCase("WaterWalk")) {
                p.teleport(p.getLocation().add(0, -0.5, 0));
            } else if (detector.equalsIgnoreCase("Criticals") || detector.equalsIgnoreCase("XRay") || detector.equalsIgnoreCase("Timer1")) {
            } else {
                p.teleport(this.getUser(p).LastRegularLocation());
            }
        }
        playerdata.OC++;
        if (!playerdata.CC.containsKey(detector)) {
            playerdata.CC.put(detector, 1);
        } else {
            playerdata.CC.put(detector, playerdata.CC.get(detector) + 1);
        }

        if (Tps.getTPS() <= Core.getInstance().getConfig().getLong("Settings.mintps") || ping >= 125) {
            return false;
        }
        Integer c = 1;
        if (playerdata.MS.containsKey(p.getName() + " - " + p.getUniqueId()))
            c = playerdata.MS.get(p.getName() + " - " + p.getUniqueId());

        c++;
        if (!playerdata.UC.containsKey(p.getUniqueId()))
            playerdata.UC.put(p.getUniqueId(), new HashMap<String, Integer>());

        UUID uuid = p.getUniqueId();
        if (!playerdata.UC.get(uuid).containsKey(detector)) {
            playerdata.UC.get(uuid).put(detector, 1);
        } else {
            playerdata.UC.get(uuid).put(detector, playerdata.UC.get(uuid).get(detector) + 1);
        }
        playerdata.MS.put(p.getName() + " - " + p.getUniqueId(), c);




        if (updateDatabase(p, detector, Count, description)) {
            return false;
        }

        String m = SUSPICION_ALERT;

        m = m.replaceAll("\\[VARIABLE_COLOR\\]", playertext(prefix));
        m = m.replaceAll("\\[DISPLAYNAME\\]", p.getDisplayName());
        m = m.replaceAll("\\[USERNAME\\]", p.getName());
        m = m.replaceAll("\\[NAME\\]", p.getName());
        m = m.replaceAll("\\[UUID\\]", p.getUniqueId().toString());
        m = m.replaceAll("\\[RESDESC\\]", description);
        m = m.replaceAll("\\[SUSPICION\\]", detector);
        m = m.replaceAll("\\[COUNT\\]", Count - 2 + "");
        m = m.replaceAll("\\[PING\\]", ping + "" );
        m = m.replaceAll("\\[TPS\\]", Tps.getNiceTPS() + "");
        m = m.replaceAll("\\[X\\]", UtilMath.trim(2, p.getLocation().getX()) + "");
        m = m.replaceAll("\\[Y\\]", UtilMath.trim(2, p.getLocation().getY()) + "");
        m = m.replaceAll("\\[Z\\]", UtilMath.trim(2, p.getLocation().getZ()) + "");
        m = m.replaceAll("\\[WORLD\\]", p.getWorld().getName());

        for (Player p2 : getServer().getOnlinePlayers()) {
            p2.sendMessage(m);
        }
        if (getConfig().getBoolean("Settings.Addoneachtriggercoung")) {
            sql.addPlayerTriggers(p.getUniqueId(), detector.replace("'", "\\'"));
        }

        return true;
    }
    public static void gc() {
        Object obj = new Object();
        WeakReference ref = new WeakReference<Object>(obj);
        obj = null;
        while (ref.get() != null) {
            System.gc();
        }
    }
}
