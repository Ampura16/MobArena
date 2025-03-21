package top.brmc.ampura16.mobarena.prearena;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import top.brmc.ampura16.mobarena.Main;
import top.brmc.ampura16.mobarena.arena.MAArenaScreenBossBar;
import top.brmc.ampura16.mobarena.arenaitemmanager.coinshop.MAShopCategory;
import top.brmc.ampura16.mobarena.arenaitemmanager.kit.KitManager;
import top.brmc.ampura16.mobarena.arenaitemmanager.kit.KitSelectGUI;
import top.brmc.ampura16.mobarena.events.MAArenaLeaveEvent;
import top.brmc.ampura16.mobarena.events.MAArenaPrepareStartEvent;
import top.brmc.ampura16.mobarena.scoreboard.MAScoreboard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapManager {
    private final Main plugin;
    private final File arenasFile;
    private FileConfiguration arenasConfig;
    private final List<Arena> arenaList;
    private String pluginPrefix;
    private final PlayerGameStatus playerGameStatus;
    private final Map<String, List<Player>> mapQueues = new HashMap<>(); // 使用 Map 存储每个地图对应的队列
    private final SelectMapGUI selectMapGUI; // 新增 SelectMapGUI 实例
    private final Map<String, MAArenaPrepareStartEvent> activeCountdowns = new HashMap<>();
    private final KitManager kitManager;
    private KitSelectGUI kitSelectGUI;
    private MAScoreboard maScoreboard;
    private final Map<String, Arena> arenas = new HashMap<>();

    public MapManager(Main plugin,
                      File arenasFile,
                      FileConfiguration arenasConfig,
                      String pluginPrefix,
                      PlayerGameStatus playerGameStatus,
                      KitManager kitManager,
                      KitSelectGUI kitSelectGUI,
                      MAScoreboard maScoreboard) {
        this.plugin = plugin;
        this.arenasFile = arenasFile;
        this.arenasConfig = arenasConfig; // 保留对配置的引用
        this.pluginPrefix = pluginPrefix;
        this.arenaList = new ArrayList<>(); // 初始化 arenaList
        loadArenasConfig(plugin); // 在构造时加载地图配置
        checkAndUpdateMapQueues(); // 初始化时检查并更新队列状态
        this.selectMapGUI = new SelectMapGUI(arenaList, plugin.getConfig()); // 初始化 SelectMapGUI
        this.playerGameStatus = playerGameStatus;
        this.kitManager = kitManager;
        this.kitSelectGUI = kitSelectGUI;
        this.maScoreboard = maScoreboard;

    }

    // 打开选择地图GUI
    public void openSelectMapGUI(Player player) {
        selectMapGUI.openSelectMapGUI(player);  // 调用 SelectMapGUI 的方法来显示界面
    }

    // 创建地图配置
    public void createMap(Player player, String mapName, String displayName, String materialName, int minPlayer, int maxPlayer, List<String> lore) {
        if (arenasConfig.contains(mapName)) {
            player.sendMessage(pluginPrefix + ChatColor.RED + " 地图名称已存在,请使用其他名称.");
            return;
        }
        if (minPlayer >= maxPlayer) {
            player.sendMessage(pluginPrefix + ChatColor.RED + " 最小玩家数不得大于或等于最大玩家数.");
            return;
        }
        // 设置地图配置，不包含等待位置
        setArenaCfgToFile(mapName, displayName, materialName, minPlayer, maxPlayer, lore);
        try {
            arenasConfig.save(arenasFile);
        } catch (IOException e) {
            player.sendMessage(pluginPrefix + ChatColor.RED + " 创建地图时出错,请重试.");
            e.printStackTrace();
            return;
        }
        // 初始化 roundMobCounts，并为回合 1 添加一个空的怪物配置
        Map<Integer, Map<String, Integer>> roundMobCounts = new HashMap<>();
        roundMobCounts.put(1, new HashMap<>()); // 添加默认的回合 1 配置
        // 将新地图添加到 arenaList，默认等待位置为 null
        arenaList.add(new Arena(plugin, pluginPrefix, mapName, displayName, materialName, minPlayer, maxPlayer, lore, null, null, roundMobCounts, maScoreboard));
        player.sendMessage(pluginPrefix + ChatColor.GREEN + " 地图 " + mapName + " 创建成功.");
        setupArenaTextGuide(player);
    }

    private void setupArenaTextGuide(Player p) {
        p.sendMessage(pluginPrefix + ChatColor.BOLD + ChatColor.GREEN + " 地图设置向导");
        p.sendMessage(pluginPrefix + ChatColor.DARK_GREEN + " Step 1:" + ChatColor.GREEN + " 使用/ma admin setwaitloc <地图名称> 设置地图等待大厅位置.");
        p.sendMessage(pluginPrefix + ChatColor.DARK_GREEN + " Step 2:" + ChatColor.GREEN + " 使用/ma admin setstartloc <地图名称> 设置游戏开始传送位置.");
        p.sendMessage(pluginPrefix + ChatColor.DARK_GREEN + " Step 3:" + ChatColor.GREEN + " 使用/stop 彻底关闭服务器并重启." + ChatColor.GOLD + " [!]一定要在这时操作,不要弄乱设置顺序.");
        p.sendMessage(pluginPrefix + ChatColor.DARK_GREEN + " Step 4:" + ChatColor.GREEN + " 使用/ma admin setmobspawnloc <地图名称> 设置任意数量的怪物生成位置.");
        p.sendMessage(pluginPrefix + ChatColor.DARK_GREEN + " Step 5:" + ChatColor.GREEN + " 在MobArena/arenas.yml中该地图配置下面添加回合怪物设置,格式如下:");
        p.sendMessage(ChatColor.DARK_GREEN + "<地图名称>:");
        p.sendMessage(ChatColor.LIGHT_PURPLE + "  其他设置: xxx");
        p.sendMessage(ChatColor.LIGHT_PURPLE + "  ...");
        p.sendMessage(ChatColor.BLUE + "  gamerounds:");
        p.sendMessage(ChatColor.AQUA + "    1:");
        p.sendMessage(ChatColor.YELLOW + "      mm怪物: int");
        p.sendMessage(ChatColor.YELLOW + "      mm怪物: int");
        p.sendMessage(ChatColor.YELLOW + "      ...");
        p.sendMessage(ChatColor.AQUA + "    2:");
        p.sendMessage(ChatColor.YELLOW + "      mm怪物: int");
        p.sendMessage(ChatColor.YELLOW + "      mm怪物: int");
        p.sendMessage(ChatColor.YELLOW + "      ...");
        p.sendMessage(ChatColor.AQUA + "    3:");
        p.sendMessage(ChatColor.YELLOW + "      mm怪物: int");
        p.sendMessage(ChatColor.YELLOW + "      mm怪物: int");
        p.sendMessage(ChatColor.YELLOW + "      ...");
        p.sendMessage(ChatColor.AQUA + "    更多回合...");
        p.sendMessage(pluginPrefix + ChatColor.DARK_GREEN + " Step 6:" + ChatColor.GREEN + " 使用/stop 彻底关闭服务器并重启.");
        p.sendMessage(pluginPrefix + ChatColor.DARK_GREEN + " Step 7:" + ChatColor.GREEN + " 设置完成.现在可以使用/ma gui指令或者钻石队列道具选择并加入游戏.");
        p.sendMessage(pluginPrefix + ChatColor.GOLD + " Notice:" + ChatColor.GREEN + " 插件不支持一端多图,即一个服务器只能创建一个地图.");

    }

    // 私有方法写入地图配置
    private void setArenaCfgToFile(String mapName, String displayName, String materialName, int minPlayer, int maxPlayer, List<String> lore) {
        arenasConfig.set(mapName + ".display-name", displayName);
        arenasConfig.set(mapName + ".material", materialName);
        arenasConfig.set(mapName + ".min-player", minPlayer);
        arenasConfig.set(mapName + ".max-player", maxPlayer);
        arenasConfig.set(mapName + ".arena-lore", lore);
    }


    // 列出地图
    public void listMaps(Player player) {
        if (arenasConfig.getKeys(false).isEmpty()) {
            player.sendMessage(pluginPrefix + ChatColor.RED + "当前没有可用的地图.");
            return;
        }

        player.sendMessage(ChatColor.AQUA + "可用地图列表:");
        for (String mapName : arenasConfig.getKeys(false)) {
            int minPlayer = arenasConfig.getInt(mapName + ".min-player");
            int maxPlayer = arenasConfig.getInt(mapName + ".max-player");
            player.sendMessage(ChatColor.GOLD + mapName + ChatColor.WHITE + " - 最小玩家数: " + minPlayer + ", 最大玩家数: " + maxPlayer);
        }
    }

    // 删除地图
    public void removeMap(Player player, String mapName) {
        if (!arenasConfig.contains(mapName)) {
            player.sendMessage(ChatColor.RED + "地图名称不存在.");
            return;
        }

        arenasConfig.set(mapName, null);
        System.out.println("删除地图配置: " + mapName);

        try {
            arenasConfig.save(arenasFile);
            player.sendMessage(pluginPrefix + ChatColor.GREEN + " 地图 " + mapName + " 已成功删除.");
        } catch (IOException e) {
            player.sendMessage(pluginPrefix + ChatColor.RED + " 删除地图时出错,请重试.");
            e.printStackTrace();
            return;
        }

        arenaList.removeIf(arena -> arena.getName().equals(mapName));
    }

    // 加载 arenas.yml 配置
    private void loadArenasConfig(Main plugin) {
        arenasConfig = YamlConfiguration.loadConfiguration(arenasFile); // 重新加载 arenas.yml 配置
        arenaList.clear(); // 清空当前的 arenaList
        List<Arena> loadedArenas = Arena.loadArenasFromConfig(plugin, arenasConfig, pluginPrefix, maScoreboard); // 使用 Arena 类的方法加载地图信息
        arenaList.addAll(loadedArenas); // 将加载的地图添加到 arenaList 中
    }

    // 重载配置
    public void reloadConfigs() {
        plugin.reloadConfig(); // 重新加载插件的主配置文件
        loadArenasConfig(plugin); // 重新加载 arenas.yml 配置文件
        selectMapGUI.updateTitle(plugin.getConfig()); // 更新 SelectMapGUI 的标题
        pluginPrefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("plugin-prefix", "&6[MobArena]")); // 更新插件前缀
        Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.GREEN + " 配置文件已重载.");
    }

    // 将玩家添加到队列
    public void addPlayerToQueue(Player player, Arena selectedArena, MAArenaScreenBossBar screenTitle) {
        if (plugin == null) {
            throw new IllegalStateException("plugin 实例在 addPlayerToQueue 中为 null.");
        }

        if (selectedArena == null) {
            plugin.getLogger().severe("所选 arena 实例为 null, 无法将玩家添加到队列.");
            player.sendMessage(pluginPrefix + ChatColor.RED + " 无法加入队列, 地图未正确加载.");
            return;
        }

        String mapName = selectedArena.getName();
        MAArenaPrepareStartEvent event = activeCountdowns.get(mapName);

        // 如果事件不存在，则创建新事件
        if (event == null) {
            event = new MAArenaPrepareStartEvent(plugin, pluginPrefix, selectedArena, screenTitle);
            activeCountdowns.put(mapName, event);
        }

        // 将玩家加入队列并依赖事件逻辑处理倒计时
        event.addPlayerToArena(player);
    }

    // 移除玩家的逻辑
    public boolean removePlayerFromQueue(Player player) {
        boolean removed = false;

        for (Map.Entry<String, MAArenaPrepareStartEvent> entry : activeCountdowns.entrySet()) {
            MAArenaPrepareStartEvent event = entry.getValue();
            if (event.getPlayers().contains(player)) {
                removed = event.removePlayerFromQueue(player);

                // 如果队列为空或不足以继续倒计时，删除事件
                if (event.getPlayers().size() < event.getArena().getMinPlayer()) {
                    event.cancelCountdown();
                    activeCountdowns.remove(entry.getKey());
                    checkAndUpdateMapQueues(); // 检查并更新队列状态
                    updatePAPIStatus(event.getArena()); // 更新 PAPI 状态
                }
                break;
            }
        }

        if (!removed) {
            player.sendMessage(pluginPrefix + ChatColor.RED + " 你不在任何地图的队列中.");
        }

        return removed;
    }

    // 公共的退出队列方法
    public boolean leaveQueue(Player player) {
        boolean wasRemoved = removePlayerFromQueue(player);
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("plugin-prefix"));
        // 通过 getArenaByPlayer 方法获取玩家所在的竞技场
        Arena arena = getArenaByPlayer(player);
        if (wasRemoved) {
            if (arena != null) {
                // 触发离开竞技场事件
                MAArenaLeaveEvent leaveEvent = new MAArenaLeaveEvent(player, arena);
                Bukkit.getPluginManager().callEvent(leaveEvent);
                MAShopCategory.clearClassShopIdentifiers(player); // 移除职业标签
                // 从竞技场中移除玩家
                arena.removePlayer(player);
                player.setHealth(20.0);
                player.sendMessage(prefix + ChatColor.GREEN + " 你已成功离开竞技场 " + arena.getDisplayName());
            }
            // 清理玩家状态
            plugin.getQueueUtils().removeQueueItems(player);
            kitSelectGUI.clearPreviousKitItems(player);
            kitSelectGUI.clearPlayerEquipment(player); // 清空玩家物品
            maScoreboard.clearPlayerScoreboard(player); // 清空计分板
            playerGameStatus.setPlayerNotInGame(player); // 更新状态为不在游戏中
            // 给予选择地图的道具
            regiveSelectMapItem(player);
        } else {
            player.sendMessage(prefix + ChatColor.RED + " 你不在任何队列中.");
        }
        return wasRemoved;
    }

    // 从配置文件中创建选择地图道具，并给予玩家
    public void regiveSelectMapItem(Player player) {
        ItemStack selectMapItem = createSelectMapItem();
        if (selectMapItem != null) {
            player.getInventory().addItem(selectMapItem);
        }
    }

    // 从配置文件中创建选择地图的道具
    private ItemStack createSelectMapItem() {
        FileConfiguration config = plugin.getConfig();

        String path = "queue-item-settings.select-map";
        String name = config.getString(path + ".name", "&2选择地图");
        String materialName = config.getString(path + ".material", "DIAMOND");
        Material material = Material.matchMaterial(materialName);
        List<String> lore = config.getStringList(path + ".lore");

        if (material == null) {
            plugin.getLogger().warning("无效的材料类型: " + materialName);
            return null;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(coloredLore);

            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(plugin, "queue_item"), PersistentDataType.STRING, "select-map");

            item.setItemMeta(meta);
        }

        return item;
    }

    // 添加竞技场
    public void addArena(Arena arena) {
        arenas.put(arena.getName(), arena);
    }

    public Arena getArenaByName(String name) {
        if (name == null) {
            return null;
        }

        // 清理传入的名称
        name = fixMapName(name);
        // System.out.println("Searching for arena with name: " + name);

        for (Arena arena : arenaList) {
            String arenaName = fixMapName(arena.getName());
            // System.out.println("Checking arena: " + arenaName);

            if (arenaName.equalsIgnoreCase(name)) {
                // System.out.println("Found arena: " + name);
                return arena;
            }
        }

        System.out.println("未找到地图: " + name);
        return null;
    } // 辅助方法获取 Arena

    private String fixMapName(String mapName) {
        if (mapName == null) return "";
        mapName = mapName.trim();  // 去掉前后空格
        if (mapName.startsWith(":")) {
            mapName = mapName.substring(1).trim();  // 去掉冒号
        }
        return mapName;
    } // 辅助方法 修复地图名称

    // 根据玩家获取竞技场
    public Arena getArenaByPlayer(Player player) {
        for (Arena arena : arenas.values()) {
            if (arena.isPlayerInArena(player)) {
                return arena; // 找到并返回玩家所在的竞技场
            }
        }
        return null; // 如果没有找到则返回 null
    }

    /**
     * PAPI Getter Method
     * */
    public Arena getPlayerArena(Player player) {
        // 根据玩家的状态返回其所在的 Arena
        for (Map.Entry<String, MAArenaPrepareStartEvent> entry : activeCountdowns.entrySet()) {
            MAArenaPrepareStartEvent event = entry.getValue();
            if (event.getPlayers().contains(player)) {
                return event.getArena();
            }
        }
        return null; // 如果玩家不在任何地图中，返回 null
    }

    // 检查并更新地图队列状态
    public void checkAndUpdateMapQueues() {
        for (Map.Entry<String, List<Player>> entry : mapQueues.entrySet()) {
            String mapName = entry.getKey();
            List<Player> players = entry.getValue();

            if (players.isEmpty()) {
                // 如果队列为空，更新 PAPI 状态
                Arena arena = getArenaByName(mapName);
                if (arena != null) {
                    arena.setGameStarted(false); // 确保游戏状态为等待中
                    updatePAPIStatus(arena); // 更新 PAPI 状态
                }
            }
        }
    }

    // 更新 PAPI 状态的方法
    private void updatePAPIStatus(Arena arena) {
        String mapName = arena.getName();
        boolean isEmpty = arena.getCurrentPlayerCount() == 0; // 检查玩家数量是否为 0

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (arena.isPlayerInArena(player)) {
                // 这里可以使用 PlaceholderAPI 的相关方法更新状态
                if (isEmpty) {
                    arena.endGame();
                } else {
                    // 如果有玩家，正常更新状态
                    String status = arena.isGameStarted() ? "&a已开始" : "&e等待中";
                    //// player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e当前地图: " + arena.getDisplayName() + " - " + status));
                }
            }
        }
    }

    public List<Arena> getArenaList() {
        return arenaList; // 返回地图列表
    }

    // 根据地图名称获取当前在游戏中的玩家列表
    public List<Player> getPlayersInGame(Arena arena) {
        if (arena == null) {
            return new ArrayList<>(); // 如果提供的地图为 null，返回空列表
        }

        String mapName = arena.getName(); // 获取地图名称
        return mapQueues.getOrDefault(mapName, new ArrayList<>()); // 返回地图对应的玩家列表，如果没有玩家则返回空列表
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public FileConfiguration getArenasConfig() {
        return arenasConfig; // 返回当前的配置对象
    }

    public void saveArenasConfig() throws IOException {
        arenasConfig.save(arenasFile); // 保存配置到文件
    }
}
