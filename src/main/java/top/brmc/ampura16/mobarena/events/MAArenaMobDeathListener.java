package top.brmc.ampura16.mobarena.events;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import top.brmc.ampura16.mobarena.arena.MAPlayerNoticeActionBar;
import top.brmc.ampura16.mobarena.prearena.Arena;
import top.brmc.ampura16.mobarena.prearena.MapManager;
import top.brmc.ampura16.mobarena.scoreboard.MAScoreboard;

public class MAArenaMobDeathListener implements Listener {
    private final MAArenaRoundTask roundTask;
    private final MapManager mapManager;
    private final MAScoreboard scoreboard;
    private final MAPlayerNoticeActionBar playerNoticeActionBar;

    public MAArenaMobDeathListener(MAArenaRoundTask roundTask, MapManager mapManager, MAScoreboard scoreboard) {
        this.roundTask = roundTask;
        this.mapManager = mapManager;
        this.scoreboard = scoreboard;
        this.playerNoticeActionBar = new MAPlayerNoticeActionBar(roundTask);
    }

    @EventHandler
    public void onMobDeath(MythicMobDeathEvent event) {
        // 从事件中获取被击杀的怪物类型
        String mobType = event.getMobType().getInternalName(); // 获取怪物的内部名称
        // 获取击杀怪物的玩家
        Player player = (Player) event.getKiller(); // 获取击杀者
        if (player == null) return; // 确保击杀者为玩家
        // 当怪物被击杀时，通知 RoundTask
        roundTask.onMobKilled(mobType); // 传递 mobType
        // 获取当前竞技场
        Arena arena = mapManager.getPlayerArena(player);
        if (arena != null) {
            // 减少剩余怪物数量并更新计分板
            arena.decreaseRemainingMobCount(); // 确保 Arena 类中有此方法
            scoreboard.updateScoreboard(player, true); // 更新计分板
            player.sendMessage(ChatColor.GREEN + "你击杀了怪物: " + mobType); // 向玩家发送提示信息
            // 检查是否所有怪物都已被击杀
            if (arena.getRemainingMobCount() == 0) {
                // 通知所有玩家当前回合已结束
                for (Player p : arena.getPlayersInArena()) {
                    p.sendMessage(ChatColor.GOLD + "当前回合的所有怪物已被击杀!");
                }
                // 更新 currentRound 变量并准备下一回合
                roundTask.onRoundEnd(); // 结束当前回合
                // 启动倒计时任务，倒计时为20秒
                playerNoticeActionBar.startCountdown(arena.getPlayersInArena(), 20, arena); // 传递 Set<Player>
                // 更新所有玩家的计分板
                for (Player p : arena.getPlayersInArena()) {
                    scoreboard.updateScoreboard(p, true); // 更新计分板以反映新的回合状态
                    scoreboard.updateRoundScoreboard(p, arena);
                }
            }
        }
    }
}
