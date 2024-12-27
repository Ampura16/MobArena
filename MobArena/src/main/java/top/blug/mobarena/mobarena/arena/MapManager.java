package top.blug.mobarena.mobarena.arena;

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
import top.blug.mobarena.mobarena.Main;

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
    private final Map<String, List<Player>> mapQueues = new HashMap<>(); // 使用 Map 存储每个地图对应的队列
    private final SelectMapGUI selectMapGUI; // 新增 SelectMapGUI 实例
    private final Map<String, MAArenaPrepareStartEvent> activeCountdowns = new HashMap<>();

    public MapManager(Main plugin, File arenasFile, FileConfiguration arenasConfig, String pluginPrefix) {
        this.plugin = plugin;
        this.arenasFile = arenasFile;
        this.arenasConfig = arenasConfig; // 保留对配置的引用
        this.pluginPrefix = pluginPrefix;
        this.arenaList = new ArrayList<>(); // 初始化 arenaList
        this.selectMapGUI = new SelectMapGUI(arenaList, plugin.getConfig()); // 初始化 SelectMapGUI
        loadArenasConfig(); // 在构造时加载地图配置
    }

    // 打开选择地图GUI
    public void openSelectMapGUI(Player player) {
        selectMapGUI.openSelectMapGUI(player);  // 调用 SelectMapGUI 的方法来显示界面
    }

    // 处理玩家选择地图的逻辑
    public void playerSelectsMap(Player player, String mapName) {
        Arena selectedArena = getArenaByName(mapName);
        if (selectedArena != null) {
            addPlayerToQueue(player, selectedArena);
        } else {
            player.sendMessage(pluginPrefix + ChatColor.RED + " 无法找到地图: " + mapName);
        }
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

        // 设置地图配置
        setArenaCfgToFile(mapName, displayName, materialName, minPlayer, maxPlayer, lore);

        try {
            arenasConfig.save(arenasFile);
        } catch (IOException e) {
            player.sendMessage(pluginPrefix + ChatColor.RED + " 创建地图时出错,请重试.");
            e.printStackTrace();
            return;
        }

        // 将新地图添加到 arenaList
        arenaList.add(new Arena(mapName, displayName, materialName, minPlayer, maxPlayer, lore));
        player.sendMessage(pluginPrefix + ChatColor.GREEN + " 地图 " + mapName + " 创建成功.");
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
    private void loadArenasConfig() {
        arenasConfig = YamlConfiguration.loadConfiguration(arenasFile); // 重新加载 arenas.yml 配置
        arenaList.clear(); // 清空当前的 arenaList
        List<Arena> loadedArenas = Arena.loadArenasFromConfig(arenasConfig); // 使用 Arena 类的方法加载地图信息
        arenaList.addAll(loadedArenas); // 将加载的地图添加到 arenaList 中
    }

    // 重载配置
    public void reloadConfigs() {
        plugin.reloadConfig(); // 重新加载插件的主配置文件
        loadArenasConfig(); // 重新加载 arenas.yml 配置文件
        selectMapGUI.updateTitle(plugin.getConfig()); // 更新 SelectMapGUI 的标题
        pluginPrefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("plugin-prefix", "&6[MobArena]")); // 更新插件前缀
        Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.GREEN + " 配置文件已重载.");
    }


    // 将玩家添加到队列
    public void addPlayerToQueue(Player player, Arena selectedArena) {
        String mapName = selectedArena.getName();
        // System.out.println(player.getName() + " 尝试加入地图 " + mapName);

        for (Map.Entry<String, List<Player>> entry : mapQueues.entrySet()) {
            List<Player> queue = entry.getValue();
            if (queue.contains(player)) {
                player.sendMessage(pluginPrefix + ChatColor.RED + " 你已在 " + entry.getKey() + " 地图的队列中,不能加入其他地图.");
                return;
            }
        }

        mapQueues.putIfAbsent(mapName, new ArrayList<>());
        mapQueues.get(mapName).add(player);
        player.sendMessage(pluginPrefix + ChatColor.GREEN + " 你已加入 " + mapName + " 地图队列.");

        if (mapQueues.get(mapName).size() >= selectedArena.getMinPlayer()) {
            MAArenaPrepareStartEvent event = new MAArenaPrepareStartEvent(selectedArena, mapQueues.get(mapName));
            Bukkit.getPluginManager().callEvent(event);
            event.startCountdown(10);

            // 存储倒计时事件
            activeCountdowns.put(mapName, event);
        }
    }

    // 移除玩家的逻辑
    private boolean removePlayerFromQueue(Player player) {
        boolean removed = false;

        for (Map.Entry<String, List<Player>> entry : mapQueues.entrySet()) {
            String mapName = entry.getKey();
            List<Player> queue = entry.getValue();
            if (queue.contains(player)) {
                queue.remove(player);
                removed = true;
                // player.sendMessage(pluginPrefix + ChatColor.GREEN + " 你已成功离开 " + mapName + " 地图的队列.");

                if (queue.size() < getArenaByName(mapName).getMinPlayer()) {
                    // 检查是否有活动的倒计时
                    MAArenaPrepareStartEvent event = activeCountdowns.get(mapName);
                    if (event != null) {
                        event.cancelCountdown(); // 取消倒计时
                        activeCountdowns.remove(mapName); // 移除该事件
                    }
                }
                break;
            }
        }

        if (!removed) {
            // player.sendMessage(pluginPrefix + ChatColor.RED + " 你不在任何地图的队列中.");
            player.sendMessage(pluginPrefix + ChatColor.GREEN + " 使用 /ma gui 选择并加入一个游戏.");
        }

        return removed;
    }

    // 公共的退出队列方法
    public boolean leaveQueue(Player player) {
        boolean wasRemoved = removePlayerFromQueue(player);
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("plugin-prefix"));

        if (wasRemoved) {
            plugin.getQueueUtils().removeQueueItems(player);
            player.sendMessage(prefix + ChatColor.YELLOW + " 你已成功退出队列.");

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

    private Arena getArenaByName(String name) {
        if (name == null) {
            return null;
        }

        // 清理传入的名称
        name = fixMapName(name);
        System.out.println("Searching for arena with name: " + name);

        for (Arena arena : arenaList) {
            String arenaName = fixMapName(arena.getName());
            System.out.println("Checking arena: " + arenaName);

            if (arenaName.equalsIgnoreCase(name)) {
                System.out.println("Found arena: " + name);
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

    public List<Arena> getArenaList() {
        return arenaList; // 返回地图列表
    }

}
