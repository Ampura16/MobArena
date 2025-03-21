package top.brmc.ampura16.mobarena.arena;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import top.brmc.ampura16.mobarena.events.MAArenaRoundTask;
import top.brmc.ampura16.mobarena.prearena.Arena;

import java.util.*;

/**
 * 该类负责管理怪物的生成逻辑,包括加载生成位置和在每个回合生成怪物.
 */
public class MAArenaMobSpawner {
    private final JavaPlugin plugin;
    private final String pluginPrefix;
    private final Arena arena;
    private final List<Location> spawnLocations = new ArrayList<>();
    private final Random random = new Random();
    private MAArenaRoundTask roundTask; // 用于保存回合任务

    /**
     * 创建一个新的 MAArenaMobSpawner 实例.
     *
     * @param plugin         插件的实例
     * @param pluginPrefix   插件前缀，用于日志输出
     * @param arena          当前竞技场的实例
     * @param arenasConfig   竞技场配置文件
     * @param roundTask      回合任务实例，用于通知回合状态
     */
    public MAArenaMobSpawner(
            JavaPlugin plugin,
            String pluginPrefix,
            Arena arena,
            FileConfiguration arenasConfig,
            MAArenaRoundTask roundTask
    ) {
        this.plugin = plugin;
        this.pluginPrefix = pluginPrefix;
        this.arena = arena;
        loadSpawnLocations(arenasConfig);
        this.roundTask = roundTask;
    }

    /**
     * 加载怪物生成位置配置.
     *
     * @param arenasConfig 竞技场配置文件
     */
    private void loadSpawnLocations(FileConfiguration arenasConfig) {
        ConfigurationSection section = arenasConfig.getConfigurationSection(arena.getName() + ".mobSpawnLocations");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                double x = section.getDouble(key + ".x");
                double y = section.getDouble(key + ".y");
                double z = section.getDouble(key + ".z");
                String world = section.getString(key + ".world");
                Location loc = new Location(Bukkit.getWorld(world), x, y, z);
                spawnLocations.add(loc);
            }
            Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.DARK_GREEN + " 成功加载生成位置: " + spawnLocations);
        } else {
            Bukkit.getLogger().warning("未找到怪物生成位置配置.");
        }
    }

    /**
     * 根据当前回合的配置生成怪物.
     *
     * @param currentRound 当前回合数
     */
    public void spawnPerRoundMobs(int currentRound) {
        Bukkit.getLogger().info("开始生成回合 " + currentRound + " 的怪物.");
        Map<String, Integer> currentRoundConfig = arena.getRounds().get(currentRound);
        if (currentRoundConfig == null || currentRoundConfig.isEmpty()) {
            Bukkit.getLogger().warning("当前回合配置为空: " + currentRound);
            return;
        }
        Bukkit.getLogger().info("当前回合的怪物配置: " + currentRoundConfig);
        for (Map.Entry<String, Integer> entry : currentRoundConfig.entrySet()) {
            String mobType = entry.getKey();
            int currentRoundAmount = entry.getValue();
            Bukkit.getLogger().info("生成怪物类型: " + mobType + ", 数量: " + currentRoundAmount);
            for (int i = 0; i < currentRoundAmount; i++) {
                Location spawnLocation = getRandomValidSpawnLocation();
                if (spawnLocation != null) {
                    spawnMob(mobType, spawnLocation);
                } else {
                    Bukkit.getLogger().warning("没有可用的生成位置.");
                }
            }
        }
    }

    /**
     * 获取有效的随机生成位置.
     *
     * @return 随机有效的位置，如果没有可用位置，则返回 null
     */
    private Location getRandomValidSpawnLocation() {
        if (spawnLocations.isEmpty()) {
            return null;
        }

        for (int i = 0; i < 10; i++) { // 尝试10次获取有效位置
            int randomIndex = random.nextInt(spawnLocations.size());
            Location location = spawnLocations.get(randomIndex);

            if (isLocationValid(location)) {
                return location;
            }
        }

        return null; // 如果没有找到有效位置，返回null
    }

    /**
     * 检查指定位置是否有效.
     *
     * @param location 要检查的位置
     * @return 如果位置有效则返回 true，否则返回 false
     */
    private boolean isLocationValid(Location location) {
        if (location.getWorld() == null) {
            return false; // 世界不存在
        }
        // 检查生成位置的上方是否是空气，下方是否是实心方块
        return location.getBlock().getType().isAir() && location.getBlock().getRelative(0, -1, 0).getType().isSolid();
    }

    /**
     * 生成指定类型的怪物.
     *
     * @param mobType   怪物的类型
     * @param location  生成位置
     */
    private void spawnMob(String mobType, Location location) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (MythicBukkit.inst().getMobManager().getMythicMob(mobType).isPresent()) {
                ActiveMob spawnedMob = MythicBukkit.inst().getMobManager().spawnMob(mobType, location, 1.0);
                if (spawnedMob != null) {
                    // 调试日志
                    if (roundTask == null) {
                        Bukkit.getLogger().warning("mobSpawner 中的 roundTask 为 null.");
                    } else {
                        roundTask.onMobSpawned(mobType); // 增加数量
                    }

                    // Bukkit.broadcastMessage("成功生成怪物: " + mobType);

                } else {
                    Bukkit.getLogger().warning("生成怪物 " + mobType + " 失败.");
                }
            } else {
                Bukkit.getLogger().warning("未找到怪物类型: " + mobType);
            }
        });
    }

    /**
     * 设置回合任务实例.
     *
     * @param roundTask 当前的回合任务实例
     */
    public void setRoundTask(MAArenaRoundTask roundTask) {
        this.roundTask = roundTask;
    }

    /**
     * 获取指定回合的怪物配置.
     *
     * @param round 回合数
     * @return 当前回合的怪物配置，如果不存在则返回空的 HashMap
     */
    public Map<String, Integer> getMobCountForRound(int round) {
        Map<Integer, Map<String, Integer>> roundMobCounts = arena.getRounds();
        return roundMobCounts.getOrDefault(round, new HashMap<>());
    }
}
