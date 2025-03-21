package top.brmc.ampura16.mobarena.prearena;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.brmc.ampura16.mobarena.Main;

import java.util.List;

public class MAAnotherListeners implements Listener {

    private final MapManager mapManager;
    private final PlayerGameStatus playerGameStatus;

    public MAAnotherListeners(Main plugin) {
        this.mapManager = plugin.getMapManager();
        this.playerGameStatus = plugin.getPlayerGameStatus();
    }

    @EventHandler
    public void playerItemGiver(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getInventory().clear();
        mapManager.regiveSelectMapItem(player);
    }

    @EventHandler
    public void playerQuitClearInventory(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        player.getInventory().clear(); // 清空背包
    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        // 获取玩家所在的竞技场
        Arena arena = playerGameStatus.getPlayerArena(player);
        if (arena != null && arena.isGameStarted()) {
            // 如果游戏已经开始，取消放置方块
            event.setCancelled(true);
            // 发送提示
            player.sendMessage(ChatColor.RED + "你不能在游戏运行时修改地图.");
        }
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        // 获取玩家所在的竞技场
        Arena arena = playerGameStatus.getPlayerArena(player);
        if (arena != null && arena.isGameStarted()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "你不能在游戏运行时修改地图.");
        }
    }

    @EventHandler
    public void onPlayerHunger(BlockBreakEvent event) {
        Player player = event.getPlayer();
        // 获取玩家所在的竞技场
        Arena arena = playerGameStatus.getPlayerArena(player);
        if (arena != null && arena.isGameStarted()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 获取玩家所在的竞技场
            Arena arena = playerGameStatus.getPlayerArena(player);
            if (arena != null && arena.isGameStarted()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEquipDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        // 获取玩家所在的竞技场
        Arena arena = playerGameStatus.getPlayerArena(player);
        if (arena != null && arena.isGameStarted()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropClassItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        // 获取丢弃的物品
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        // 检查物品是否有 lore
        if (droppedItem.hasItemMeta()) {
            ItemMeta meta = droppedItem.getItemMeta();
            if (meta.hasLore()) {
                List<String> lore = meta.getLore();
                // 遍历 lore 列表，检查是否包含 "职业道具"
                if (lore != null) {
                    for (String line : lore) {
                        if (line.contains("职业道具")) {
                            // 取消丢弃事件
                            event.setCancelled(true);
                            // 发送提示
                            player.sendMessage(ChatColor.RED + "职业道具无法丢弃.");
                            return; // 如果找到匹配项，直接返回
                        }
                    }
                }
            }
        }
    }

}

