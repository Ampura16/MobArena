package top.brmc.ampura16.mobarena.prearena;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerGameStatus {
    private final Map<Player, Boolean> playerStatusMap;
    private final Map<Player, Arena> playerArenaMap;
    private final Map<Player, Boolean> isPlayerInMap; // 统一标识符:玩家是否在游戏中,用于允许选择职业和使用商店

    public PlayerGameStatus() {
        playerStatusMap = new HashMap<>();
        playerArenaMap = new HashMap<>();
        isPlayerInMap = new HashMap<>(); // 统一标识符:初始化玩家游戏中的状态
    }

    // 设置玩家为游戏中状态
    public void setPlayerTrueInGame(Player player, Arena arena) {
        playerStatusMap.put(player, true);
        isPlayerInMap.put(player, true); // 统一标识符:标记玩家正在游戏中
        playerArenaMap.put(player, arena); // 记录玩家所在的地图
    }

    // 设置玩家为不在游戏中状态
    public void setPlayerNotInGame(Player player) {
        playerStatusMap.put(player, false);
        playerArenaMap.remove(player); // 移除地图信息
        isPlayerInMap.put(player, false); // 统一标识符:标记玩家不在游戏中
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "玩家 " + player.getName() + " 的状态已更新为不在游戏中.");
    }

    // 测试方法:检查玩家在指令逻辑上是否在游戏中
    public boolean isPlayerInGame(Player player) {
        boolean status = playerStatusMap.getOrDefault(player, false);
        // System.out.println("检查玩家 " + player.getName() + " 的状态: " + status); // 日志输出
        return status;
    }

    // 统一标识符:检测玩家实际上是否在游戏中
    public boolean isPlayerCurrentInGame(Player player) {
        return isPlayerInMap.getOrDefault(player, false);
    }

    // 获取玩家所在地图
    public Arena getPlayerArena(Player player) {
        return playerArenaMap.get(player);
    }

    // 清除玩家状态
    public void clearPlayerStatus(Player player) {
        playerStatusMap.remove(player);
        isPlayerInMap.put(player, false); // 统一标识符:标记玩家不在游戏中
        playerArenaMap.remove(player); // 清除地图信息
    }
}
