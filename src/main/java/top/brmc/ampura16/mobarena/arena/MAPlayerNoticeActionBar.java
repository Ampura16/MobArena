package top.brmc.ampura16.mobarena.arena;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import top.brmc.ampura16.mobarena.events.MAArenaRoundTask;
import top.brmc.ampura16.mobarena.events.MAArenaRoundUpdateEvent;
import top.brmc.ampura16.mobarena.prearena.Arena;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 该类用于向玩家发送动作条通知
 * 包括倒计时和回合开始的消息
 */
public class MAPlayerNoticeActionBar {

    private final MAArenaRoundTask roundTask; // 将 roundTask 声明为 final

    /**
     * 创建一个新的 MAPlayerNoticeActionBar 实例
     * @param roundTask 回合任务实例
     */
    public MAPlayerNoticeActionBar(MAArenaRoundTask roundTask) {
        this.roundTask = roundTask; // 初始化 roundTask
    }

    /**
     * 向指定玩家发送动作条消息
     * @param player 要发送消息的玩家
     * @param message 要发送的消息内容
     */
    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    /**
     * 开始倒计时并向玩家发送通知
     * @param players 玩家集合
     * @param countdownTime 倒计时时间，以秒为单位
     * @param arena 竞技场实例
     */
    public void startCountdown(Set<Player> players, int countdownTime, Arena arena) {
        List<Player> playerList = new ArrayList<>(players);
        int currentRound = arena.getCurrentRound(); // 通过 Arena 获取当前回合
        new BukkitRunnable() {
            int timeLeft = countdownTime;
            @Override
            public void run() {
                if (timeLeft > 0) {
                    String actionBarMessage = ChatColor.DARK_GREEN + "下一回合将在 " + ChatColor.YELLOW + timeLeft + ChatColor.DARK_GREEN + " 秒后开始!";
                    for (Player player : playerList) {
                        sendActionBar(player, actionBarMessage);
                    }
                    timeLeft--;
                } else {
                    // 倒计时结束，取消任务
                    this.cancel();
                    // 通知玩家下一回合已经开始
                    for (Player player : playerList) {
                        sendActionBar(player, ChatColor.GREEN + "下一回合开始");
                    }
                    // 更新回合数
                    int nextRound = currentRound + 1; // 下一回合
                    arena.setCurrentRound(nextRound); // 通过 Arena 更新回合数
                    Bukkit.getLogger().info("倒计时结束，更新回合数为: " + nextRound);
                    // 加载下一回合的怪物配置
                    arena.loadCurrentRoundMobCounts();
                    // 触发回合更新事件
                    for (Player player : arena.getPlayersInArena()) { // 遍历所有玩家
                        MAArenaRoundUpdateEvent roundUpdateEvent = new MAArenaRoundUpdateEvent(arena, nextRound, player);
                        Bukkit.getPluginManager().callEvent(roundUpdateEvent); // 调用事件
                    }
                    // 移除直接调用生成怪物逻辑，由事件监听器处理
                    // roundTask.spawnNextRoundMobs(); // 不再直接调用
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("MobArena"), 0L, 20L); // 每秒执行一次
    }
}
