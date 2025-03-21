package top.brmc.ampura16.mobarena.events;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import top.brmc.ampura16.mobarena.arena.MAArenaMobSpawner;
import top.brmc.ampura16.mobarena.prearena.Arena;
import top.brmc.ampura16.mobarena.scoreboard.MAScoreboard;

import java.util.Map;

public class MAArenaRoundTask extends BukkitRunnable {
    private final JavaPlugin plugin;
    private MAArenaMobSpawner mobSpawner;
    private final Arena arena;
    private final int roundCount;
    private int currentRound;
    private int currentMobCount = 0; // 当前回合怪物数量
    private boolean roundInProgress = false; // 标记回合是否正在进行
    private final MAScoreboard scoreboard;

    // 构造函数
    public MAArenaRoundTask(JavaPlugin plugin, MAArenaMobSpawner mobSpawner, Arena arena, int roundCount, MAScoreboard scoreboard) {
        this.plugin = plugin;
        this.mobSpawner = mobSpawner; // 初始化 mobSpawner
        this.arena = arena; // 初始化 arena
        this.roundCount = roundCount;
        this.currentRound = 1; // 初始值设为 1
        this.scoreboard = scoreboard;
    }

    @Override
    public void run() {
        // 检查当前回合是否正在进行
        if (roundInProgress) {
            if (currentMobCount <= 0) {
                // 所有怪物被击杀，更新回合状态
                onRoundEnd(); // 调用结束回合逻辑
            }
        } else {
            // 如果当前回合还没有开始，开始生成怪物
            if (currentRound < roundCount) {
                spawnNextRoundMobs();
            }
        }
    }

    // 生成下一回合的怪物
    public void spawnNextRoundMobs() {
        int currentRound = arena.getCurrentRound(); // 通过 Arena 获取当前回合
        Bukkit.getLogger().info("开始生成回合 " + currentRound + " 的怪物.");
        Map<String, Integer> mobConfig = arena.getRounds().get(currentRound); // 获取当前回合的怪物配置
        if (mobConfig == null || mobConfig.isEmpty()) {
            Bukkit.getLogger().warning("未找到回合 " + currentRound + " 的怪物配置，跳过生成怪物.");
            roundInProgress = false;
            return;
        }
        Bukkit.getLogger().info("当前回合的怪物配置: " + mobConfig);
        currentMobCount = mobConfig.values().stream().mapToInt(Integer::intValue).sum();
        if (currentMobCount > 0) {
            roundInProgress = true;
            mobSpawner.spawnPerRoundMobs(currentRound); // 调用 mobSpawner 生成怪物
            Bukkit.getLogger().info("回合 " + currentRound + " 的怪物已成功生成, 当前怪物数量: " + currentMobCount);
        } else {
            Bukkit.getLogger().warning("回合 " + currentRound + " 生成的怪物数量为0, 回合将不会进行.");
            roundInProgress = false;
        }
    }

    // 当有怪物被生成时调用
    public void onMobSpawned(String mobType) {
        currentMobCount++;
        arena.mobSpawned(mobType); // 调用 Arena 的 mobSpawned 方法
        // Bukkit.getLogger().info("当前回合怪物数量增加: " + currentMobCount);
    }

    // 当怪物被击杀时调用
    public void onMobKilled(String mobType) {
        if (currentMobCount > 0) {
            currentMobCount--;
            arena.mobKilled(); // 更新 Arena 中的剩余怪物数量
        }
    }

    public void onRoundEnd() {
        String localEventName = ChatColor.GREEN + "[MAArenaRoundTask-onRoundEnd-Event] ";
        int currentRound = arena.getCurrentRound(); // 获取当前回合数

        // 向玩家发送“当前回合结束”消息
        for (Player player : arena.getPlayersInArena()) {
            player.sendMessage(localEventName + ChatColor.YELLOW + "当前回合结束, 当前回合: " + currentRound);
        }

        // 更新回合数
        int nextRound = currentRound + 1; // 下一回合
        arena.setCurrentRound(nextRound); // 通过 Arena 更新回合数

        // 向玩家发送“更新回合数”消息
        for (Player player : arena.getPlayersInArena()) {
            player.sendMessage(localEventName + ChatColor.YELLOW + "更新回合数为: " + nextRound);
        }

        // 加载下一回合的怪物配置
        arena.loadCurrentRoundMobCounts();

        // 更新计分板
        for (Player p : arena.getPlayersInArena()) {
            if (scoreboard != null) {
                scoreboard.updateRoundScoreboard(p, arena);
            } else {
                Bukkit.getLogger().warning("scoreboard 为 null，无法更新计分板.");
            }
        }

        roundInProgress = false;

        // 发布回合更新事件
        for (Player player : arena.getPlayersInArena()) { // 遍历所有玩家
            MAArenaRoundUpdateEvent roundUpdateEvent = new MAArenaRoundUpdateEvent(arena, nextRound, player);
            Bukkit.getPluginManager().callEvent(roundUpdateEvent); // 调用事件
        }
    }

    public void setMobSpawner(MAArenaMobSpawner mobSpawner) {
        this.mobSpawner = mobSpawner;
    }

    public int getCurrentRound() {
        return arena.getCurrentRound(); // 通过 Arena 获取回合数
    }

    public void setCurrentRound(int currentRound) {
        arena.setCurrentRound(currentRound);
    }

}
