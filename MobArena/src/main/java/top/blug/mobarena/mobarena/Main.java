package top.blug.mobarena.mobarena;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import top.blug.mobarena.mobarena.arena.*;
import top.blug.mobarena.mobarena.command.MACommand;
import top.blug.mobarena.mobarena.command.MATabCompleter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class Main extends JavaPlugin {

    private String pluginPrefix;
    private MonstersConfig monstersConfig;
    private MACommand maCommand;
    private File arenasFile;
    private FileConfiguration arenasConfig;
    private List<Arena> arenaList; // 存储所有地图的列表
    private HashMap<UUID, Arena> playerArenaMap; // 存储玩家与地图的对应关系
    private MapManager mapManager;
    private MAQueueUtils queueUtils;

    @Override
    public void onEnable() {
        loadConfigurations(); // 封装配置加载过程
        printLoadMessage();
        setupComponents();
        setupCommandsAndListeners();
    }

    private void loadConfigurations() {
        saveDefaultConfig();
        pluginPrefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("plugin-prefix", "&2TowerDefense"));

        createArenasConfig();
    }

    private void setupComponents() {
        monstersConfig = new MonstersConfig(this);
        monstersConfig.loadMonstersConfig();

        playerArenaMap = new HashMap<>();
        mapManager = new MapManager(this, arenasFile, arenasConfig, pluginPrefix);
        queueUtils = new MAQueueUtils(this);
    }

    private void setupCommandsAndListeners() {
        maCommand = new MACommand(this);
        getCommand("mobarena").setExecutor(maCommand);
        getCommand("mobarena").setTabCompleter(new MATabCompleter());
        getServer().getPluginManager().registerEvents(new SelectMapListener(this, mapManager.getArenaList(), pluginPrefix), this);
        getServer().getPluginManager().registerEvents(new MAQueueListener(this, mapManager, queueUtils), this);
        getServer().getPluginManager().registerEvents(new MAAnotherListeners(this), this);
    }


    @Override
    public void onDisable() {
        printUnloadMessage();
        // 插件关闭时的逻辑
    }

    private void printLoadMessage() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "==============================");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + pluginPrefix + ChatColor.GREEN + " 插件已加载.");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Powered by Ampura16 " + ChatColor.GOLD + " @ BlockLand Studio");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "==============================");
    }

    private void printUnloadMessage() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "==============================");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + pluginPrefix + ChatColor.GREEN + " 插件已卸载.");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Powered by Ampura16 " + ChatColor.GOLD + " @ BlockLand Studio");
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "==============================");
    }

    // 创建 arenas.yml 配置文件
    private void createArenasConfig() {
        arenasFile = new File(getDataFolder(), "arenas.yml");
        if (!arenasFile.exists()) {
            try {
                arenasFile.getParentFile().mkdirs();
                arenasFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("无法创建 arenas.yml 文件.");
            }
        }
        arenasConfig = YamlConfiguration.loadConfiguration(arenasFile);
        arenaList = Arena.loadArenasFromConfig(arenasConfig);
    }

    // 获取 MapManager 实例
    public MapManager getMapManager() {
        return mapManager;
    }

    public File getArenasFile() {
        return arenasFile;
    }

    public FileConfiguration getArenasConfig() {
        return arenasConfig;
    }

    public String getPluginPrefix() {
        return pluginPrefix;
    }

    public MAQueueUtils getQueueUtils() {
        return queueUtils;
    }

}
