package top.brmc.ampura16.mobarena;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import top.brmc.ampura16.mobarena.arena.MAArenaMobSpawner;
import top.brmc.ampura16.mobarena.arena.MAArenaScreenBossBar;
import top.brmc.ampura16.mobarena.arenaitemmanager.coinshop.MAShopCategory;
import top.brmc.ampura16.mobarena.arenaitemmanager.kit.KitManager;
import top.brmc.ampura16.mobarena.arenaitemmanager.kit.KitSelectGUI;
import top.brmc.ampura16.mobarena.command.MACommand;
import top.brmc.ampura16.mobarena.command.MATabCompleter;
import top.brmc.ampura16.mobarena.configs.FoldersConfig;
import top.brmc.ampura16.mobarena.configs.MobConfig;
import top.brmc.ampura16.mobarena.configs.READMEConfig;
import top.brmc.ampura16.mobarena.events.*;
import top.brmc.ampura16.mobarena.placeholder.MAPlaceholder;
import top.brmc.ampura16.mobarena.prearena.*;
import top.brmc.ampura16.mobarena.scoreboard.MAScoreboard;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * 插件主类
 */
public final class Main extends JavaPlugin {

    private Main plugin;
    private String pluginPrefix;
    private FoldersConfig foldersConfig;
    private FileConfiguration arenasConfig;
    private FileConfiguration kitsConfig; // kits.yml 的配置
    private List<Arena> arenaList; // 存储所有地图的列表
    private Arena arena;
    private MapManager mapManager; // 存储玩家与地图的对应关系
    private PlayerGameStatus playerGameStatus;
    private MAQueueUtils queueUtils;
    private final HashMap<String, MobConfig> mobsConfigurations = new HashMap<>(); // 存储怪物配置
    private NamespacedKey kitItemKey;
    private KitManager kitManager;
    private KitSelectGUI kitSelectGUI;
    private MAScoreboard maScoreboard;
    private FileConfiguration scoreboardConfig; // scoreboard.yml 的配置
    private MAArenaRoundTask roundTask; // 回合任务
    private FileConfiguration shopConfig; // shop.yml 的配置

    /**
     * 插件启动时调用的方法。
     */
    @Override
    public void onEnable() {
        if (!checkDependencyPlugins()) {
            return;
        }

        initializeReadMeConfig();
        saveDefaultConfig(); // 先保存默认配置
        pluginPrefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("plugin-prefix", "&6MobArena")); // 初始化 pluginPrefix
        foldersConfig = new FoldersConfig(this, pluginPrefix); // 初始化 foldersConfig
        loadConfigurations(); // 加载配置
        foldersConfig.createMobsFolder(); // 创建Mobs文件夹和默认配置
        loadMobsConfigurations(); // 加载怪物配置
        shopConfig = foldersConfig.createShopConfig(); // 创建shop.yml配置文件
        scoreboardConfig = foldersConfig.createScoreboardConfig(); // 创建或加载 scoreboard.yml 配置
        kitsConfig = foldersConfig.createKitsConfig(); // 创建或加载 kits.yml 配置
        kitManager = new KitManager(this, pluginPrefix); // 初始化 KitManager
        MAArenaScreenBossBar screenBossBar = new MAArenaScreenBossBar(this);
        registerMAPlaceholderAPI(); // 注册 PAPI 变量
        setupComponents(); // 设置组件
        setupCommand(); // 设置命令和补全器
        setupListeners(); // 设置监听器
        printLoadMessage(); // 打印加载信息
    }

    /**
     * 初始化 2个README 配置.
     */
    private void initializeReadMeConfig() {
        READMEConfig readmeConfig = new READMEConfig(this);

        readmeConfig.createReadMeFile(getDataFolder()); // 创建根目录的 README.txt
        readmeConfig.createMobsConfigReadMeFile(new File(getDataFolder(), "Mobs")); // 创建 Mobs 文件夹的 README.txt
    }

    /**
     * 依赖 checkPluginController 检查前置插件加载情况.
     *
     * @return 如果所有硬前置插件已加载,返回 true;否则返回 false.
     */
    private boolean checkDependencyPlugins() {
        boolean MythicMobsLoaded = checkPluginController("MythicMobs");
        boolean PlaceholderAPILoaded = checkPluginController("PlaceholderAPI");
        boolean VaultLoaded = checkPluginController("Vault");

        // 如果所有插件都加载成功,返回 true,否则返回 false.
        return MythicMobsLoaded && PlaceholderAPILoaded && VaultLoaded;
    }

    /**
     * 检查指定插件是否已加载,并输出相应的日志信息.
     *
     * @param pluginName 要检查的插件名称
     * @return 如果插件已加载,返回 true,否则返回 false.
     */
    private boolean checkPluginController(String pluginName) {
        if (Bukkit.getPluginManager().getPlugin(pluginName) == null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MobArena-启动项检查] " + ChatColor.GOLD + "未找到依赖插件: " + ChatColor.GREEN + pluginName);
            return false; // 返回 false 表示插件未加载
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[MobArena-启动项检查] 已加载依赖插件: " + ChatColor.GREEN + pluginName);
            return true; // 返回 true 表示插件已加载
        }
    }

    /**
     * 加载插件的配置文件。
     */
    private void loadConfigurations() {
        arenasConfig = foldersConfig.createArenasConfig(); // 创建 arenas.yml 配置
        arenaList = Arena.loadArenasFromConfig(plugin, arenasConfig, pluginPrefix, maScoreboard); // 传递 maScoreboard
        Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.DARK_GREEN + " 加载的竞技场: " + arenasConfig.getKeys(false)); // 输出所有加载的竞技场
    }

    /**
     * 设置组件，包括玩家状态、地图管理器等。
     */
    private void setupComponents() {
        playerGameStatus = new PlayerGameStatus();
        kitSelectGUI = new KitSelectGUI(this, kitManager, kitItemKey); // 初始化 KitSelectGUI
        maScoreboard = new MAScoreboard(this);
        mapManager = new MapManager(
                this,
                foldersConfig.getArenasFile(),
                arenasConfig,
                pluginPrefix,
                playerGameStatus,
                kitManager,
                kitSelectGUI,
                maScoreboard
        );
        queueUtils = new MAQueueUtils(this);
        kitItemKey = new NamespacedKey(this, "kitItemKey"); // 初始化

        // arenaList 在 createArenasConfig 方法中已经加载,此时可以直接使用 arenaList 进行相关操作
        this.roundTask = createRoundTask(); // 初始化回合任务
    }

    /**
     * 创建回合任务
     */
    private MAArenaRoundTask createRoundTask() {
        // 确保 arenasConfig 和 arenaList 是有效的
        if (arenasConfig != null && arenaList != null && !arenaList.isEmpty()) {
            Arena firstArena = arenaList.get(0); // 获取第一个竞技场
            // 创建 MAArenaMobSpawner 实例
            MAArenaMobSpawner mobSpawner = new MAArenaMobSpawner(this, pluginPrefix, firstArena, arenasConfig, roundTask);
            // 创建 MAArenaRoundTask 实例
            MAArenaRoundTask roundTask = new MAArenaRoundTask(
                    this,
                    mobSpawner,
                    firstArena,
                    firstArena.getRounds().size(),
                    maScoreboard);
            // 设置 mobSpawner 的 roundTask
            mobSpawner.setRoundTask(roundTask);
            return roundTask; // 返回创建的回合任务
        }
        return null; // 如果没有有效的竞技场，返回 null
    }

    /**
     * 设置命令类和补全器
     */
    private void setupCommand() {
        MACommand maCommand = new MACommand(this, pluginPrefix);
        getCommand("mobarena").setExecutor(maCommand);
        getCommand("mobarena").setTabCompleter(new MATabCompleter(new File(getDataFolder(), "Mobs"), mapManager));
    }

    /**
     * 设置插件的命令和事件监听器.
     */
    private void setupListeners() {
        // 实例化并注册事件监听器
        getServer().getPluginManager().registerEvents(new SelectMapListener(this, mapManager.getArenaList(), pluginPrefix), this);
        getServer().getPluginManager().registerEvents(new MAQueueListener(this, mapManager, queueUtils, kitItemKey), this);
        getServer().getPluginManager().registerEvents(new MAArenaJoinEventListener(maScoreboard), this);
        // 修改 MAArenaLeaveEventListener 的注册方式
        MAArenaLeaveEventListener leaveEventListener = new MAArenaLeaveEventListener(this);
        getServer().getPluginManager().registerEvents(leaveEventListener, this);
        // 实例化 arenaStartListener
        MAArenaStartEventListener arenaStartListener = new MAArenaStartEventListener(this, arenasConfig, getConfig(), maScoreboard);
        getServer().getPluginManager().registerEvents(arenaStartListener, this); // 注册 MAAArenaStartListener
        getServer().getPluginManager().registerEvents(new MAArenaRoundUpdateEventListener(this, arenasConfig), this);
        getServer().getPluginManager().registerEvents(new MAArenaMobDeathListener(roundTask, mapManager, maScoreboard), this);
        getServer().getPluginManager().registerEvents(new MAArenaEndEventListener(this, getConfig()), this);
        getServer().getPluginManager().registerEvents(new MAShopCategory(), this);
        getServer().getPluginManager().registerEvents(new MAAnotherListeners(this), this);
        // kitSelectGUI 的再次初始化
        kitSelectGUI = new KitSelectGUI(this, kitManager, kitItemKey); // 传递 kitItemKey
        getServer().getPluginManager().registerEvents(kitSelectGUI, this);
    }

    /**
     * 插件禁用时调用的方法.
     */
    @Override
    public void onDisable() {
        printUnloadMessage(); // 打印卸载信息
        // 插件关闭时的逻辑
    }

    /**
     * 打印插件加载信息到控制台.
     */
    private void printLoadMessage() {
        Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.AQUA + " ==============================");
        Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.AQUA + " " + ChatColor.AQUA + getPluginPrefix() + ChatColor.GREEN + " 插件已加载.");
        Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.AQUA + " Powered by Ampura16 " + ChatColor.GOLD + " @ BlockLand Studio");
        Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.AQUA + " ==============================");
    }

    /**
     * 打印插件卸载信息到控制台.
     */
    private void printUnloadMessage() {
        Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.AQUA + " ==============================");
        Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.AQUA + ChatColor.AQUA + getPluginPrefix() + ChatColor.GREEN + " 插件已卸载.");
        Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.AQUA + " Powered by Ampura16 " + ChatColor.GOLD + " @ BlockLand Studio");
        Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.AQUA + " ==============================");
    }

    /**
     * 加载怪物配置的方法.
     */
    private void loadMobsConfigurations() {
        File mobsFolder = new File(getDataFolder(), "Mobs");
        if (!mobsFolder.exists()) {
            mobsFolder.mkdirs(); // 创建文件夹，如果不存在
        }

        mobsConfigurations.clear(); // 清空之前的配置

        // 遍历文件夹中的所有YAML文件
        for (File file : mobsFolder.listFiles()) {
            if (file.getName().endsWith(".yml")) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file); // 加载配置
                String mobKey = file.getName().replace(".yml", ""); // 获取文件名作为怪物的键
                MobConfig mobConfig = new MobConfig(config, mobKey); // 创建 MobConfig 实例
                mobsConfigurations.put(mobConfig.getName(), mobConfig); // 以怪物名称为键存储
            }
        }
    }

    /**
     * 重载所有配置.
     * 此方法将在 MACommand 中的 reload 子命令中调用.
     */
    public void reloadConfigurations() {
        reloadConfig(); // 重新加载 config.yml
        // 重载 arenas.yml 配置
        arenasConfig = foldersConfig.createArenasConfig();
        arenaList = Arena.loadArenasFromConfig(plugin, arenasConfig, pluginPrefix, maScoreboard); // 重新加载地图
        // 动态更新所有 Arena 的等待位置和开始位置
        for (Arena arena : arenaList) Arena.updateArenaWaitingLocation(arena, arenasConfig);
        shopConfig = foldersConfig.createShopConfig(); // 重新加载 shop.yml
        loadMobsConfigurations(); // 重载怪物配置
        kitsConfig = foldersConfig.createKitsConfig(); // 重新加载职业配置
        scoreboardConfig = foldersConfig.createScoreboardConfig(); // 重新加载 scoreboard.yml
    }

    /**
     * 注册 MA Placeholder API.
     */
    private void registerMAPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MAPlaceholder(this).register();
            Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.DARK_GREEN + " MAPlaceholder 注册成功.");
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[MobArena] " + ChatColor.YELLOW + "未找到 PlaceholderAPI 插件,MAPlaceholder 注册失败.");
        }
    }

    /**
     * 获取 MapManager 实例.
     *
     * @return MapManager 实例
     */
    public MapManager getMapManager() {
        return mapManager;
    }

    /**
     * 获取 PlayerGameStatus 实例.
     *
     * @return PlayerGameStatus 实例
     */
    public PlayerGameStatus getPlayerGameStatus() {
        return playerGameStatus;
    }

    /**
     * 获取 arenas.yml 的配置.
     *
     * @return arenasConfig 配置文件
     */
    public FileConfiguration getArenasConfig() {
        return arenasConfig;
    }

    /**
     * 获取插件前缀.
     *
     * @return 插件前缀
     */
    public String getPluginPrefix() {
        return pluginPrefix;
    }

    /**
     * 获取队列工具类实例.
     *
     * @return MAQueueUtils 实例
     */
    public MAQueueUtils getQueueUtils() {
        return queueUtils;
    }

    /**
     * 获取 kits.yml 的配置.
     *
     * @return kitsConfig 配置文件
     */
    public FileConfiguration getKitsConfig() {
        return kitsConfig;
    }

    /**
     * 获取 KitManager 实例.
     *
     * @return KitManager 实例
     */
    public KitManager getKitManager() {
        return kitManager;
    }

    /**
     * 获取当前的 scoreboard.yml 配置文件内容.
     *
     * @return 返回 scoreboard.yml 的 FileConfiguration 对象，包含从文件中加载的配置。
     */
    public FileConfiguration getScoreboardConfig() {
        return scoreboardConfig;
    }

    /**
     * 获取 MAScoreboard 实例.
     *
     * @return MAScoreboard 实例
     */
    public MAScoreboard getMAScoreboard() {
        return maScoreboard;
    }
}
