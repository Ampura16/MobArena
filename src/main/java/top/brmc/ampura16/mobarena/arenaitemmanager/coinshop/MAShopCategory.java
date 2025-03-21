package top.brmc.ampura16.mobarena.arenaitemmanager.coinshop;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 该类用于管理职业标识物品的发放和清除
 * 同时监听玩家丢弃职业标识物品的事件
 */
public class MAShopCategory implements Listener {

    /**
     * 给玩家发放职业标识物品
     * @param player 要发放物品的玩家
     * @param kitDisplayName 职业名称
     */
    public static void giveClassShopIdentifier(Player player, String kitDisplayName) {
        ItemStack shopIdentifier = new ItemStack(Material.MOJANG_BANNER_PATTERN);
        ItemMeta meta = shopIdentifier.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(kitDisplayName + " 职业标识");
            shopIdentifier.setItemMeta(meta);
        }

        player.getInventory().addItem(shopIdentifier); // 将标识物品加入玩家背包
        player.sendMessage(ChatColor.GREEN + "你获得了 " + kitDisplayName + " 的职业标识.");
    }

    /**
     * 清除玩家背包中的职业标识物品
     * @param player 要清除物品的玩家
     */
    public static void clearClassShopIdentifiers(Player player) {
        // 遍历背包中的物品
        ItemStack[] contents = player.getInventory().getContents();
        boolean removed = false; // 用于判断是否有物品被移除

        for (ItemStack item : contents) {
            if (item != null && item.getType() == Material.MOJANG_BANNER_PATTERN) {
                player.getInventory().remove(item); // 移除该物品
                removed = true; // 标记物品已被移除
                player.sendMessage(ChatColor.RED + "已清除职业标识物品.");
            }
        }

        if (!removed) {
            player.sendMessage(ChatColor.GREEN + "没有找到职业标识物品.");
        }
    }

    /**
     * 处理玩家丢弃物品事件
     * @param event 玩家丢弃物品事件
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() == Material.MOJANG_BANNER_PATTERN) {
            event.setCancelled(true); // 取消丢弃事件
            // event.getPlayer().sendMessage(ChatColor.RED + "你不能丢弃职业标识物品.");
        }
    }
}
