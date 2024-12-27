package top.blug.mobarena.mobarena;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class MonstersConfig {

    private final Main plugin; // 存储对 Main 的引用
    private FileConfiguration monstersConfig;
    private File monstersFile;

    // 构造函数，接受 Main 的实例
    public MonstersConfig(Main plugin) {
        this.plugin = plugin;
    }

    // 加载怪物配置
    public void loadMonstersConfig() {
        monstersFile = new File(plugin.getDataFolder(), "monsters.yml");
        if (!monstersFile.exists()) {
            monstersFile.getParentFile().mkdirs();
            plugin.saveResource("monsters.yml", false); // 从 JAR 资源保存
        }

        monstersConfig = new YamlConfiguration();
        try {
            monstersConfig.load(monstersFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    // 获取怪物配置的方法
    public FileConfiguration getMonstersConfig() {
        return monstersConfig;
    }
}