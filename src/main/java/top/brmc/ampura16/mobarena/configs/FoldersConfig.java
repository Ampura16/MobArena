package top.brmc.ampura16.mobarena.configs;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * 插件文件夹配置管理类
 */
public class FoldersConfig {

    private final JavaPlugin plugin;
    private final String pluginPrefix;

    public FoldersConfig(JavaPlugin plugin, String pluginPrefix) {
        this.plugin = plugin;
        this.pluginPrefix = pluginPrefix;
    }

    /**
     * 创建 arenas.yml 配置文件.
     *
     * @return 返回 FileConfiguration 对象
     */
    public FileConfiguration createArenasConfig() {
        File arenasFile = new File(plugin.getDataFolder(), "arenas.yml");
        if (!arenasFile.exists()) {
            try {
                arenasFile.getParentFile().mkdirs();
                arenasFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("无法创建 arenas.yml 文件: " + e.getMessage());
            }
        }
        return YamlConfiguration.loadConfiguration(arenasFile);
    }

    /**
     * 创建 shop.yml 配置文件.
     *
     * @return 返回 FileConfiguration 对象
     */
    public FileConfiguration createShopConfig() {
        File shopFile = new File(plugin.getDataFolder(), "shop.yml");
        if (!shopFile.exists()) {
            try {
                shopFile.getParentFile().mkdirs();
                InputStream inputStream = plugin.getResource("shop.yml");
                if (inputStream != null) {
                    Files.copy(inputStream, shopFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    inputStream.close();
                    Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.GOLD + " 已从资源文件夹复制" + ChatColor.GREEN + " shop.yml" + ChatColor.GOLD + " 文件到插件目录.");
                } else {
                    Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.RED + " 资源文件 shop.yml 未找到，请确保它在 resources 目录中.");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("无法创建 shop.yml 文件: " + e.getMessage());
            }
        }
        return YamlConfiguration.loadConfiguration(shopFile);
    }

    /**
     * 创建 kits.yml 配置文件.
     *
     * @return 返回 FileConfiguration 对象
     */
    public FileConfiguration createKitsConfig() {
        File kitsFile = new File(plugin.getDataFolder(), "kits.yml");
        if (!kitsFile.exists()) {
            try {
                kitsFile.getParentFile().mkdirs();
                InputStream inputStream = plugin.getResource("kits.yml");
                if (inputStream != null) {
                    Files.copy(inputStream, kitsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    inputStream.close();
                    Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.GOLD + " 已从资源文件夹复制" + ChatColor.GREEN + " kits.yml" + ChatColor.GOLD + " 文件到插件目录.");
                } else {
                    Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.RED + " 资源文件 kits.yml 未找到，请确保它在 resources 目录中.");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("无法创建 kits.yml 文件: " + e.getMessage());
            }
        }
        return YamlConfiguration.loadConfiguration(kitsFile);
    }

    /**
     * 创建 scoreboard.yml 配置文件.
     *
     * @return 返回 FileConfiguration 对象
     */
    public FileConfiguration createScoreboardConfig() {
        File scoreboardFile = new File(plugin.getDataFolder(), "scoreboard.yml");
        if (!scoreboardFile.exists()) {
            try {
                scoreboardFile.getParentFile().mkdirs();
                InputStream inputStream = plugin.getResource("scoreboard.yml");
                if (inputStream != null) {
                    Files.copy(inputStream, scoreboardFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    inputStream.close();
                    Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.GOLD + " 已从资源文件夹复制" + ChatColor.GREEN + " scoreboard.yml" + ChatColor.GOLD + " 文件到插件目录.");
                } else {
                    Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.RED + " 资源文件 scoreboard.yml 未找到，请确保它在 resources 目录中.");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("无法创建 scoreboard.yml 文件: " + e.getMessage());
            }
        }
        return YamlConfiguration.loadConfiguration(scoreboardFile);
    }

    /**
     * 创建 Mobs 文件夹并复制默认配置文件.
     */
    public void createMobsFolder() {
        File mobsFolder = new File(plugin.getDataFolder(), "Mobs");
        if (!mobsFolder.exists()) {
            mobsFolder.mkdirs();
        }

        String[] defaultMobFiles = {"NormalZombie.yml", "NormalSkeleton.yml", "TheOldOne.yml"};
        for (String yamlFilesName : defaultMobFiles) {
            File configFile = new File(mobsFolder, yamlFilesName);
            if (!configFile.exists()) {
                try {
                    configFile.createNewFile();
                    InputStream inputStream = plugin.getResource("Mobs/" + yamlFilesName);
                    if (inputStream != null) {
                        Files.copy(inputStream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        inputStream.close();
                        Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.GOLD + " 已从资源文件夹复制" + ChatColor.GREEN + " Mobs/" + yamlFilesName + ChatColor.GOLD + " 文件到插件目录.");
                    }
                } catch (IOException e) {
                    plugin.getLogger().severe("无法复制默认的 " + yamlFilesName + " 文件: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 获取 arenas.yml 文件.
     *
     * @return arenas.yml 文件
     */
    public File getArenasFile() {
        return new File(plugin.getDataFolder(), "arenas.yml");
    }
}
