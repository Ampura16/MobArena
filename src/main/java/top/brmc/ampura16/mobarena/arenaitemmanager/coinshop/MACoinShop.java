package top.brmc.ampura16.mobarena.arenaitemmanager.coinshop;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 该类用于管理玩家的职业物品
 */
public class MACoinShop {

    /**
     * 检查玩家背包中的职业物品
     * @param player 要检查的玩家
     * @return 包含职业物品信息的字符串
     */
    public static String checkPlayerProfessionItems(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        StringBuilder message = new StringBuilder();

        boolean hasProfessionItem = false;

        for (ItemStack item : contents) {
            if (item != null && item.getType() == Material.MOJANG_BANNER_PATTERN) {
                hasProfessionItem = true;
                String professionName = item.getItemMeta().getDisplayName();
                message.append(ChatColor.GOLD + "您的职业标识: ").append(professionName).append("\n");
            }
        }

        if (!hasProfessionItem) {
            message.append(ChatColor.RED + "你没有职业物品.");
        }

        return message.toString();
    }
}
