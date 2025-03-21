package top.brmc.ampura16.mobarena.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import top.brmc.ampura16.mobarena.prearena.Arena;
import top.brmc.ampura16.mobarena.prearena.PlayerGameStatus;

import java.util.ArrayList;
import java.util.List;

public class MAArenaEndEvent extends Event {

    private static final HandlerList handlers = new HandlerList(); // 静态 HandlerList
    private final String pluginPrefix;
    private final PlayerGameStatus playerGameStatus;
    private final List<Player> players;
    private final Arena arena;

    public MAArenaEndEvent(PlayerGameStatus playerGameStatus, String pluginPrefix, List<Player> players, Arena arena) {
        this.playerGameStatus = playerGameStatus;
        this.pluginPrefix = pluginPrefix;
        this.players = new ArrayList<>(players); // 使用副本，避免外部修改
        this.arena = arena;
    }

    // 结束游戏并更新玩家状态的方法
    public void endGame() {
        // 先触发事件，再更新玩家状态
        Bukkit.getPluginManager().callEvent(this);
        players.forEach(player -> {
            player.sendMessage(pluginPrefix + ChatColor.RED + " 你的游戏标记已更新为 false."); // 似乎无效
            player.sendMessage(pluginPrefix + ChatColor.GOLD + " 游戏已结束."); // 似乎无效
            playerGameStatus.setPlayerNotInGame(player); // 更新状态为不在游戏中
        });
        // 更新当前地图状态为"等待中"
        if (arena != null) {
            try {
                arena.endGame();
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "当前地图状态已更新为等待中.");
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "更新地图状态时发生错误: " + e.getMessage());
            }
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "竞技场对象为空,无法更新状态.");
        }
    }

    // Getter 方法
    public List<Player> getPlayers() {
        return players;
    }
    public Arena getArena() {
        return arena;
    }
    // 实现 Event 的抽象方法
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    // 静态方法，用于获取 HandlerList
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
