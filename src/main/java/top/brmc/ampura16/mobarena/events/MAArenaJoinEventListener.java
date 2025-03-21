package top.brmc.ampura16.mobarena.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import top.brmc.ampura16.mobarena.prearena.Arena;
import top.brmc.ampura16.mobarena.scoreboard.MAScoreboard;

import java.util.Set;

public class MAArenaJoinEventListener implements Listener {

    private final MAScoreboard maScoreboard;
    private String localListenerName = ChatColor.GOLD + "[MAArenaJoinEventListener]";

    public MAArenaJoinEventListener(MAScoreboard maScoreboard) {
        this.maScoreboard = maScoreboard;
    }

    @EventHandler
    public void onMAArenaJoin(MAArenaJoinEvent event) {
        Player player = event.getPlayer();
        Arena arena = event.getArena();
        if (arena != null) {
            // 打印日志，确认监听器被调用
            Bukkit.getConsoleSender().sendMessage(localListenerName + ChatColor.GREEN + " 已调用.");
            // 将玩家加入竞技场
            arena.addPlayerToArena(player);
            // 获取竞技场内的所有玩家
            Set<Player> playersInArena = arena.getPlayersInArena();
            // 更新所有玩家的计分板
            for (Player p : playersInArena) {
                maScoreboard.updateScoreboard(p, arena.isGameStarted());
                p.sendMessage(localListenerName + ChatColor.GREEN + " 已调用.");
            }
            // 获取等待位置并将玩家传送过去
            Location waitingLocation = arena.getWaitingLocation();
            if (waitingLocation != null) {
                player.teleport(waitingLocation);
                player.sendMessage(ChatColor.GREEN + "你已被传送到等待区域.");
            } else {
                player.sendMessage(ChatColor.RED + "等待区域未配置,无法传送.");
            }
            // 向玩家发送提示消息
            player.sendMessage(ChatColor.GREEN + "你已成功加入竞技场 " + arena.getDisplayName() + ".");
            // 向竞技场内其他玩家发送提示消息
            for (Player p : playersInArena) {
                if (!p.equals(player)) {
                    p.sendMessage(ChatColor.YELLOW + player.getName() + " 已加入竞技场.");
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "未能找到地图信息,加入竞技场失败.");
        }
    }

}
