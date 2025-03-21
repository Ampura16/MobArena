package top.brmc.ampura16.mobarena.arenaitemmanager.kit;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 该类用于管理游戏职业包（Kit）的加载和存储
 */
public class KitManager {
    private final JavaPlugin plugin; // 插件实例
    private String pluginPrefix; // 插件前缀
    private final File kitFile; // 职业包配置文件
    private FileConfiguration kitConfig; // 职业包配置内容
    private Map<String, Kit> kits = new HashMap<>(); // 存储职业包的映射

    /**
     * 构造一个新的 KitManager 实例
     * @param plugin 插件实例
     * @param pluginPrefix 插件前缀
     */
    public KitManager(JavaPlugin plugin, String pluginPrefix) {
        this.plugin = plugin;
        this.pluginPrefix = pluginPrefix;
        this.kitFile = new File(plugin.getDataFolder(), "kits.yml");
        loadKits(); // 加载职业包
    }

    /**
     * 加载职业包的配置文件
     */
    private void loadKits() {
        // 检查文件是否存在
        if (!kitFile.exists()) {
            try {
                kitFile.getParentFile().mkdirs(); // 确保父目录存在
                kitFile.createNewFile();
                plugin.saveResource("kits.yml", false); // 复制默认配置文件
                Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.GOLD + " 已创建默认的 kits.yml 配置文件.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 加载配置文件内容
        kitConfig = YamlConfiguration.loadConfiguration(kitFile);

        // 加载每个职业的配置信息
        for (String key : kitConfig.getKeys(false)) {
            String displayName = color(kitConfig.getString(key + ".kit-display-name")); // 使用颜色转换
            String material = kitConfig.getString(key + ".kit-material");
            List<String> lore = kitConfig.getStringList(key + ".lore").stream()
                    .map(this::color) // 对每一行描述进行颜色转换
                    .collect(Collectors.toList());
            String shop = kitConfig.getString(key + ".kit-shop");
            List<String> inventory = kitConfig.getStringList(key + ".kit-inventory");

            // 检查材料是否有效
            if (material == null) {
                Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.RED + " " + key + ChatColor.RED + " 的材料名称未定义,跳过加载该职业.");
                continue; // 跳过无效材料的职业
            }

            Kit kit = new Kit(displayName, material, lore, shop, inventory);
            kits.put(key, kit);
            Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.DARK_GREEN + " 已加载职业: " + key);
        }
    }

    /**
     * 获取指定名称的职业包
     * @param name 职业包名称
     * @return 对应的 Kit 实例
     */
    public Kit getKit(String name) {
        return kits.get(name);
    }

    /**
     * 获取所有职业包
     * @return 包含所有职业包的映射
     */
    public Map<String, Kit> getAllKits() {
        return kits;
    }

    /**
     * 将消息中的颜色代码转换为实际颜色
     * @param message 要转换的消息
     * @return 转换后的消息
     */
    public String color(String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
