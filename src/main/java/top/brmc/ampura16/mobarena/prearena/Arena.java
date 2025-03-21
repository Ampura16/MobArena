package top.brmc.ampura16.mobarena.prearena;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import top.brmc.ampura16.mobarena.Main;
import top.brmc.ampura16.mobarena.arena.MAArenaMobSpawner;
import top.brmc.ampura16.mobarena.events.MAArenaEndEvent;
import top.brmc.ampura16.mobarena.scoreboard.MAScoreboard;

import java.util.*;
import java.util.logging.Logger;

public class Arena {
    private final Main plugin;
    private final Map<Integer, Map<String, Integer>> roundMobCounts; // 存储每个回合的怪物数量
    private final String pluginPrefix;
    private final String mapName;
    private final String displayName;
    private final int minPlayer;
    private final int maxPlayer;
    private final String material;
    private final List<String> lore;
    private Location waitingLocation;
    private Location startLocation;
    private final Set<Player> playersInArena = new HashSet<>(); // 存储当前地图玩家数 PAPI
    private boolean gameStarted = false;
    private MAArenaMobSpawner mobSpawner;
    private int currentRound; // 当前回合标识
    private Map<String, Integer> currentRoundMobCounts; // 当前回合的怪物数量
    private int remainingMobCount = 0; // 当前回合剩余的怪物数量
    private static final Logger logger = Bukkit.getLogger();
    private MAScoreboard maScoreboard;

    public Arena(Main plugin,
                 String pluginPrefix,
                 String mapName,
                 String displayName,
                 String material,
                 int minPlayer,
                 int maxPlayer,
                 List<String> lore,
                 Location waitingLocation,
                 Location startLocation,
                 Map<Integer, Map<String, Integer>> roundMobCounts,
                 MAScoreboard maScoreboard) {
        this.plugin = plugin;
        this.pluginPrefix = pluginPrefix;
        this.mapName = mapName;
        this.displayName = displayName;
        this.minPlayer = minPlayer;
        this.maxPlayer = maxPlayer;
        this.material = material;
        this.lore = lore != null ? lore : new ArrayList<>();
        this.waitingLocation = waitingLocation;
        this.startLocation = startLocation;
        this.roundMobCounts = roundMobCounts != null ? roundMobCounts : new HashMap<>();
        this.maScoreboard = maScoreboard;
    }

    public static Arena fromConfig(Main plugin, FileConfiguration config, String mapName, String pluginPrefix, MAScoreboard maScoreboard) {
        String displayName = config.getString(mapName + ".display-name", mapName);
        String material = config.getString(mapName + ".material", "BRICKS");
        List<String> lore = config.getStringList(mapName + ".arena-lore");
        Location waitingLocation = getLocationFromConfig(config, mapName, "waitingLocation");
        Location startLocation = getLocationFromConfig(config, mapName, "startLocation");
        int minPlayer = config.getInt(mapName + ".min-player", 2);
        int maxPlayer = config.getInt(mapName + ".max-player", 4);
        // 加载回合怪物配置
        Map<Integer, Map<String, Integer>> roundMobCounts = loadRoundMobCounts(config, mapName);
        return new Arena(plugin, pluginPrefix, mapName, displayName, material, minPlayer, maxPlayer, lore, waitingLocation, startLocation, roundMobCounts, maScoreboard);
    }

    public static Location getLocationFromConfig(FileConfiguration config, String mapName, String locationKey) {
        String worldName = config.getString(mapName + "." + locationKey + ".world");
        if (worldName == null) {
            return null;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Bukkit.getLogger().warning("未找到世界: " + worldName);
            return null;
        }
        double x = config.getDouble(mapName + "." + locationKey + ".x");
        double y = config.getDouble(mapName + "." + locationKey + ".y");
        double z = config.getDouble(mapName + "." + locationKey + ".z");
        return new Location(world, x, y, z);
    }


    private static Map<Integer, Map<String, Integer>> loadRoundMobCounts(FileConfiguration config, String mapName) {
        Map<Integer, Map<String, Integer>> roundMobCounts = new HashMap<>();
        ConfigurationSection roundsSection = config.getConfigurationSection(mapName + ".gamerounds");
        if (roundsSection != null) {
            for (String roundKey : roundsSection.getKeys(false)) {
                try {
                    int round = Integer.parseInt(roundKey); // 将回合键转换为整数
                    ConfigurationSection mobsSection = roundsSection.getConfigurationSection(roundKey);
                    if (mobsSection != null) {
                        Map<String, Integer> mobCounts = new HashMap<>();
                        for (String mobType : mobsSection.getKeys(false)) {
                            int count = mobsSection.getInt(mobType, 0);
                            mobCounts.put(mobType, count);
                        }
                        roundMobCounts.put(round, mobCounts);
                    }
                } catch (NumberFormatException e) {
                    Bukkit.getLogger().warning("无效的回合键: " + roundKey + "，回合键必须是整数。");
                }
            }
        } else {
            Bukkit.getLogger().warning("未找到 " + mapName + " 的 gamerounds 配置。");
        }
        return roundMobCounts;
    }

    public int getRemainingMobCount() {
        return remainingMobCount;
    }

    public void setCurrentRound(int currentRound) {
        Bukkit.getLogger().info("更新回合数为: " + currentRound);
        this.currentRound = currentRound; // 直接设置 currentRound 的值
    }

    public int getCurrentRound() {
        return this.currentRound;
    }

    public void loadCurrentRoundMobCounts() {
        if (roundMobCounts.containsKey(currentRound)) {
            currentRoundMobCounts = new HashMap<>(roundMobCounts.get(currentRound));
            remainingMobCount = currentRoundMobCounts.values().stream().mapToInt(Integer::intValue).sum();
            logger.info("当前回合加载怪物数量: " + remainingMobCount);
        } else {
            currentRoundMobCounts = new HashMap<>(); // 如果未找到配置，初始化一个空的 Map
            remainingMobCount = 0; // 设置剩余怪物数量为 0
            logger.warning("未找到回合配置: " + currentRound);
        }
    }

    public void mobSpawned(String mobType) {
        if (currentRoundMobCounts != null && currentRoundMobCounts.containsKey(mobType)) {
            int count = currentRoundMobCounts.get(mobType);
            if (count > 0) {
                currentRoundMobCounts.put(mobType, count - 1);


                // logger.info("成功生成怪物: " + mobType + "，当前回合剩余怪物数量: " + remainingMobCount);


            } else {
                logger.warning("当前回合该怪物类型的数量已用尽: " + mobType);
            }
        } else {
            logger.warning("当前回合未包含怪物类型: " + mobType);
        }
    }

    public void mobKilled() {
        if (remainingMobCount > 0) {
            remainingMobCount--;
            logger.info("当前回合剩余怪物数量减少: " + remainingMobCount);

            // 更新计分板
            for (Player player : getPlayersInArena()) {
                maScoreboard.updateRoundScoreboard(player, this);
            }

            if (remainingMobCount <= 0) {
                logger.info("当前回合所有怪物已被击杀,准备进入下一回合.");
                nextRound();
            }
        } else {
            logger.warning("当前回合剩余怪物数量已为0,无法再减少.");
        }
    }

    public void nextRound() {
        currentRound++; // 直接增加回合数
        loadCurrentRoundMobCounts();
        remainingMobCount = currentRoundMobCounts.values().stream().mapToInt(Integer::intValue).sum();
        logger.info("当前回合结束,进入下一个回合: " + currentRound + ", 剩余怪物数量: " + remainingMobCount);
        // 更新计分板
        for (Player player : getPlayersInArena()) {
            maScoreboard.updateRoundScoreboard(player, this);
        }
    }

    public static List<Arena> loadArenasFromConfig(Main plugin, FileConfiguration config, String pluginPrefix, MAScoreboard maScoreboard) {
        List<Arena> arenaList = new ArrayList<>();
        for (String mapName : config.getKeys(false)) {
            try {
                Arena arena = fromConfig(plugin, config, mapName, pluginPrefix, maScoreboard);
                arenaList.add(arena);
                Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.GREEN + " 已加载竞技场: " + arena.getDisplayName());
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.RED + mapName + " 时出错: " + e.getMessage());
            }
        }
        return arenaList;
    }

    public String getName() {
        return mapName;
    }

    public void setWaitingLocation(Location waitingLocation) {
        this.waitingLocation = waitingLocation;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMaterial() {
        return material;
    }

    public int getMinPlayer() {
        return minPlayer;
    }

    public int getMaxPlayer() {
        return maxPlayer;
    }

    public List<String> getLore() {
        return lore;
    }

    public Location getWaitingLocation() {
        return waitingLocation;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public Map<Integer, Map<String, Integer>> getRounds() {
        return roundMobCounts;
    }

    public void addPlayerToArena(Player player) {
        playersInArena.add(player);
    }

    public void removePlayer(Player player) {
        playersInArena.remove(player);
    }

    public int getCurrentPlayerCount() {
        return playersInArena.size();
    }

    public void startGame() {
        this.gameStarted = true; // 标记游戏为进行中
        this.currentRound = 1; // 初始化为 1
        loadCurrentRoundMobCounts();
        logger.info("游戏已启动,当前回合: " + currentRound + "，剩余怪物数量: " + remainingMobCount);
        // 检查 maScoreboard 是否为 null
        if (maScoreboard == null) {
            logger.warning("maScoreboard 为 null,无法更新计分板.");
            return;
        }
        // 更新计分板
        for (Player player : getPlayersInArena()) {
            maScoreboard.updateRoundScoreboard(player, this);
        }
    }

    public void endGame() {
        this.gameStarted = false; // 重置游戏状态为未开始
        this.currentRound = 0; // 重置当前回合为 0
        this.remainingMobCount = 0; // 重置剩余怪物数量
        this.currentRoundMobCounts = null; // 清空当前回合怪物数量
        logger.info("游戏已结束，竞技场状态已重置.");

        // 获取当前竞技场的玩家列表
        Set<Player> players = new HashSet<>(this.playersInArena);

        // 清空玩家列表
        this.playersInArena.clear();

        // 触发 MAArenaEndEvent 事件
        PlayerGameStatus playerGameStatus = plugin.getPlayerGameStatus(); // 假设 plugin 是 Main 类的实例
        MAArenaEndEvent endEvent = new MAArenaEndEvent(playerGameStatus, pluginPrefix, new ArrayList<>(players), this);
        Bukkit.getPluginManager().callEvent(endEvent);
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public String getStatus() {
        return isGameStarted() ? "进行中" : "等待中";
    }

    public Set<Player> getPlayersInArena() {
        return new HashSet<>(playersInArena); // 返回竞技场内的玩家集合
    }

    public boolean isPlayerInArena(Player player) {
        return playersInArena.contains(player);
    }

    public void setMobSpawner(MAArenaMobSpawner mobSpawner) {
        this.mobSpawner = mobSpawner;
    }

    public MAArenaMobSpawner getMobSpawner() {
        return mobSpawner;
    }

    public void decreaseRemainingMobCount() {
        if (remainingMobCount > 0) {
            remainingMobCount--;
        }
    }

    @Override
    public String toString() {
        return "Arena{" +
                "mapName='" + mapName + '\'' +
                ", displayName='" + displayName + '\'' +
                ", minPlayer=" + minPlayer +
                ", maxPlayer=" + maxPlayer +
                ", material='" + material + '\'' +
                ", lore=" + lore +
                ", waitingLocation=" + (waitingLocation != null ?
                waitingLocation.getWorld().getName() + " (" +
                        waitingLocation.getX() + ", " +
                        waitingLocation.getY() + ", " +
                        waitingLocation.getZ() + ")" : "null") +
                '}';
    }
}
