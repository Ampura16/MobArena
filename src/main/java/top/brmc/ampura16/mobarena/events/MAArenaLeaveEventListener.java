package top.brmc.ampura16.mobarena.events;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;
import top.brmc.ampura16.mobarena.Main;
import top.brmc.ampura16.mobarena.arenaitemmanager.coinshop.MAShopCategory;
import top.brmc.ampura16.mobarena.prearena.Arena;
import top.brmc.ampura16.mobarena.scoreboard.MAScoreboard;

import java.util.Set;

public class MAArenaLeaveEventListener implements Listener {
    private final MAScoreboard maScoreboard;
    public MAArenaLeaveEventListener(Main plugin) {
        this.maScoreboard = plugin.getMAScoreboard();
    }

    @EventHandler
    public void onMAArenaLeave(MAArenaLeaveEvent event) {
        Player player = event.getPlayer();
        Arena arena = event.getArena();
        if (arena != null) {
            arena.removePlayer(player); // 从竞技场中移除玩家
            player.setHealth(20.0);
            MAShopCategory.clearClassShopIdentifiers(player); // 移除职业标签
            clearPlayerScoreboard(player); // 清空计分板
            // 向竞技场内剩余玩家发送提示
            Set<Player> remainingPlayers = arena.getPlayersInArena();
            for (Player remainingPlayer : remainingPlayers) {
                remainingPlayer.sendMessage(ChatColor.YELLOW + player.getName() + " 已离开竞技场.");
                // 更新剩余玩家的计分板
                maScoreboard.updateScoreboard(remainingPlayer, arena.isGameStarted());
            }
        } else {
            player.sendMessage("未能找到地图信息.");
        }
    }

    private void clearPlayerScoreboard(Player player) {
        // 获取 ScoreboardManager 并设置一个新的空计分板
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        player.setScoreboard(scoreboard); // 设置一个新的空计分板
    }

}
