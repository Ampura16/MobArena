package top.blug.mobarena.mobarena.arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class MAArenaPrepareStartEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Arena arena;
    private final List<Player> players; // 队列中的玩家
    private BukkitTask arenaPreStartTimer; // 倒计时任务

    public MAArenaPrepareStartEvent(Arena arena, List<Player> players) {
        this.arena = arena;
        this.players = players;
    }

    public Arena getArena() {
        return arena;
    }

    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public void startCountdown(int countdownTime) {
        arenaPreStartTimer = new BukkitRunnable() {
            int timeLeft = countdownTime;

            @Override
            public void run() {
                if (players.size() < arena.getMinPlayer()) {
                    cancel();
                    players.forEach(player -> player.sendMessage(ChatColor.RED + "游戏因人数不足而取消."));
                    return;
                }

                if (timeLeft > 0) {
                    players.forEach(player -> player.sendMessage(ChatColor.YELLOW + "游戏将在 " + timeLeft + " 秒后开始..."));
                    timeLeft--;
                } else {
                    cancel();
                    players.forEach(player -> player.sendMessage(ChatColor.GREEN + "游戏开始!"));
                    startGame(); // 调用比赛开始逻辑
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("TowerDefense"), 0, 20); // 每秒更新
    }

    // 游戏开始逻辑
    public void startGame() {
        // 在此处添加游戏开始具体逻辑
        // 例如，初始化地图、传送玩家、设置比赛状态等
    }


    public void cancelCountdown() {
        if (arenaPreStartTimer != null) {
            arenaPreStartTimer.cancel();
            arenaPreStartTimer = null; // 清理引用
        }
    }
}
