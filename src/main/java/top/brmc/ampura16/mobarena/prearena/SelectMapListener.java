package top.brmc.ampura16.mobarena.prearena;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.brmc.ampura16.mobarena.Main;
import top.brmc.ampura16.mobarena.arena.MAArenaScreenBossBar;

import java.util.List;

public class SelectMapListener implements Listener {

    private final Main plugin;
    private final List<Arena> arenaList;
    private final String pluginPrefix;
    private final String guiTitle;
    private final MAQueueUtils queueUtils;

    public SelectMapListener(Main plugin, List<Arena> arenaList, String pluginPrefix) {
        this.plugin = plugin;
        this.arenaList = arenaList;
        this.pluginPrefix = pluginPrefix;
        this.guiTitle = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("queue-item-settings.select-map.gui-title", "&2选择地图"));
        this.queueUtils = new MAQueueUtils(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(guiTitle)) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) {
                return;
            }

            ItemMeta meta = clickedItem.getItemMeta();
            List<String> lore = meta.getLore();
            if (lore == null) {
                return;
            }

            String mapName = null;
            for (String line : lore) {
                if (line.startsWith(ChatColor.DARK_GRAY + "Map ID: ")) {
                    // 修复提取逻辑，确保前后清理多余字符
                    mapName = ChatColor.stripColor(line).substring("Map ID: ".length()).trim();

                    // 进一步清理任何可能的前导冒号
                    if (mapName.startsWith(":")) {
                        mapName = mapName.substring(1).trim();
                    }
                    break;
                }
            }

            if (mapName != null) {
                Player player = (Player) event.getWhoClicked();
                Arena selectedArena = getArenaByName(mapName);

                // 检查竞技场是否正在进行游戏
                if (selectedArena.isGameStarted()) {
                    player.sendMessage(ChatColor.RED + "该竞技场正在进行游戏,无法加入.");
                    return;
                }

                // 检查等待位置是否不为 null
                if (selectedArena.getWaitingLocation() == null) {
                    player.sendMessage(ChatColor.RED + "该地图尚未配置等待位置,无法加入.");
                    return; // 结束处理，阻止加入
                }
                try {
                    MAArenaScreenBossBar screenTitle = new MAArenaScreenBossBar(plugin); // 创建实例
                    plugin.getMapManager().addPlayerToQueue(player, selectedArena, screenTitle);
                    queueUtils.giveQueueItems(player);
                    player.closeInventory();
                    player.sendMessage(ChatColor.GREEN  + "你可以输入 /ma leave 离开当前队列.");

                    // 触发加入游戏的事件
                    //// MAArenaJoinEvent joinEvent = new MAArenaJoinEvent(player, selectedArena); // 使用事件类
                    //// plugin.getServer().getPluginManager().callEvent(joinEvent); // 调用事件
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "加入队列时出现问题,请重试.");
                    plugin.getLogger().severe("玩家尝试加入游戏时发生错误: " + e.getMessage());
                }
            }
        }
    }

    public void onPlayerLeaveQueue(Player player) {
        queueUtils.removeQueueItems(player);
    }

    private Arena getArenaByName(String name) {
        // 清理传入的名称
        name = fixMapName(name);
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "正在寻找地图: [" + name + "]");

        for (Arena arena : arenaList) {
            String arenaName = fixMapName(arena.getName());
            Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "加载地图列表: [" + arenaName + "]");

            if (arenaName.equalsIgnoreCase(name)) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "发现地图: [" + name + "]");
                return arena;
            }
        }

        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "未找到地图: [" + name + "]");
        return null;
    }

    private String fixMapName(String mapName) {
        if (mapName == null) return "";
        mapName = mapName.trim();  // 去掉前后空格
        if (mapName.startsWith(":")) {
            mapName = mapName.substring(1).trim();  // 去掉冒号
        }
        return mapName;
    } // 辅助方法 修复地图名称

}
