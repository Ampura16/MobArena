package top.brmc.ampura16.mobarena.command;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import top.brmc.ampura16.mobarena.prearena.Arena;
import top.brmc.ampura16.mobarena.prearena.MapManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 处理设置怪物生成位置命令的类.
 */
public class MASetMobSpawnLocationCommand {

    private final MapManager mapManager;
    private final String pluginPrefix;

    /**
     * 构造函数，初始化 MASetMobSpawnLocationCommand 实例.
     *
     * @param mapManager 地图管理器，用于获取和管理地图信息
     * @param pluginPrefix 插件前缀，用于消息显示
     */
    public MASetMobSpawnLocationCommand(MapManager mapManager, String pluginPrefix) {
        this.mapManager = mapManager;
        this.pluginPrefix = pluginPrefix;
    }

    /**
     * 设置指定地图的怪物生成位置.
     *
     * @param player 玩家对象，执行命令的玩家
     * @param spawnMapName 要设置怪物生成位置的地图名称
     */
    public void setMobSpawnLocation(Player player, String spawnMapName) {
        Arena spawnArena = mapManager.getArenaByName(spawnMapName);
        if (spawnArena == null) {
            player.sendMessage(pluginPrefix + ChatColor.RED + " 地图名称无效: " + spawnMapName);
            return;
        }

        // 获取玩家的当前位置
        Location spawnLocation = player.getLocation();
        String spawnWorldName = spawnLocation.getWorld().getName();
        double spawnX = spawnLocation.getX();
        double spawnY = spawnLocation.getY();
        double spawnZ = spawnLocation.getZ();

        // 更新配置文件中的多个怪物出生位置
        FileConfiguration arenasConfigSpawn = mapManager.getArenasConfig(); // 获取地图配置

        // 创建一个新的位置节点
        String path = spawnMapName + ".mobSpawnLocations";

        // 检查列表是否已经存在
        if (!arenasConfigSpawn.contains(path)) {
            arenasConfigSpawn.createSection(path); // 创建新的列表节点
        }

        // 找到下一个可用的 spawner 名称
        List<String> existingKeys = new ArrayList<>(arenasConfigSpawn.getConfigurationSection(path).getKeys(false));
        String nextSpawner = "spawner" + (existingKeys.size() + 1); // 根据已有的键生成下一个 spawner 名称

        // 将新的出生位置保存到配置中
        arenasConfigSpawn.set(path + "." + nextSpawner + ".world", spawnWorldName);
        arenasConfigSpawn.set(path + "." + nextSpawner + ".x", spawnX);
        arenasConfigSpawn.set(path + "." + nextSpawner + ".y", spawnY);
        arenasConfigSpawn.set(path + "." + nextSpawner + ".z", spawnZ);

        // 保存配置文件
        try {
            mapManager.saveArenasConfig(); // 执行保存方法
        } catch (IOException e) {
            player.sendMessage(pluginPrefix + ChatColor.RED + " 保存配置时出错: " + e.getMessage());
            return;
        }

        // 信息反馈
        player.sendMessage(pluginPrefix + ChatColor.GREEN + " 地图 " + spawnMapName + " 的怪物出生位置已设置为: " +
                "\n坐标: " + ChatColor.YELLOW + "世界: " + spawnWorldName +
                ", x: " + spawnX +
                ", y: " + spawnY +
                ", z: " + spawnZ +
                "\n配置记录名为: " + ChatColor.YELLOW + nextSpawner);
    }
}
