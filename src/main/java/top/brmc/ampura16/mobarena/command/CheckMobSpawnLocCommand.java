package top.brmc.ampura16.mobarena.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import top.brmc.ampura16.mobarena.Main;

/**
 * 处理检查竞技场怪物生成位置配置的命令.
 */
public class CheckMobSpawnLocCommand {
    private final FileConfiguration arenasConfig; // 存储 arenas.yml 配置

    /**
     * 构造函数，初始化 CheckMobSpawnLocCommand 实例.
     *
     * @param plugin 主插件实例，提供获取 arenas.yml 配置的方法
     */
    public CheckMobSpawnLocCommand(Main plugin) {
        this.arenasConfig = plugin.getArenasConfig(); // 获取 arenas.yml 配置
    }

    /**
     * 检查指定的竞技场名称是否存在.
     *
     * @param arenaName 竞技场名称
     * @return 如果竞技场存在返回 true，否则返回 false
     */
    private boolean doesArenaExist(String arenaName) {
        // 检查配置文件中是否存在该地图的配置
        return arenasConfig.contains(arenaName); // 检查顶级键是否存在
    }

    /**
     * 执行检查竞技场怪物生成位置配置的操作.
     *
     * @param player 玩家对象，接收消息
     * @param arenaName 竞技场名称
     */
    public void execute(Player player, String arenaName) {
        // 先检查地图名称是否存在
        System.out.println("检查地图名称: " + arenaName);
        if (!doesArenaExist(arenaName)) {
            player.sendMessage(ChatColor.RED + "未找到地图 " + arenaName + " 的怪物生成位置配置.");
            System.out.println("尝试访问的地图名称不存在: " + arenaName);
            return; // 退出方法
        }

        // 根据提供的地图名称获取对应竞技场的怪物生成位置配置
        ConfigurationSection mobSpawnLocations = arenasConfig.getConfigurationSection(arenaName + ".mobSpawnLocations");

        if (mobSpawnLocations != null) {
            player.sendMessage(ChatColor.GREEN + "怪物生成位置 for " + arenaName + ":");
            for (String key : mobSpawnLocations.getKeys(false)) {
                ConfigurationSection locationSection = mobSpawnLocations.getConfigurationSection(key);
                if (locationSection != null) {
                    String worldName = locationSection.getString("world");
                    double x = locationSection.getDouble("x");
                    double y = locationSection.getDouble("y");
                    double z = locationSection.getDouble("z");
                    player.sendMessage(ChatColor.AQUA + key + ": " + worldName + " (" + x + ", " + y + ", " + z + ")");
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "未找到地图 " + arenaName + " 的怪物生成位置配置.");
            System.out.println("尝试访问怪物生成位置时出错: " + arenaName);
        }
    }
}
