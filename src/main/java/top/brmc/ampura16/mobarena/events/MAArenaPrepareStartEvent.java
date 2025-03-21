package top.brmc.ampura16.mobarena.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import top.brmc.ampura16.mobarena.Main;
import top.brmc.ampura16.mobarena.arena.MAArenaScreenBossBar;
import top.brmc.ampura16.mobarena.arena.MAArenaScreenTitle;
import top.brmc.ampura16.mobarena.prearena.Arena;
import top.brmc.ampura16.mobarena.prearena.MAQueueUtils;
import top.brmc.ampura16.mobarena.prearena.PlayerGameStatus;

import java.util.ArrayList;
import java.util.List;

public class MAArenaPrepareStartEvent extends Event {
    private final Main plugin;
    private String pluginPrefix;
    private static final HandlerList handlers = new HandlerList();
    private final Arena arena;
    private final List<Player> players; // 队列中的玩家
    private BukkitTask arenaPreStartTimer; // 倒计时任务
    private PlayerGameStatus playerGameStatus; // 添加玩家状态管理
    private int countdownTime;
    private MAArenaScreenBossBar screenBossbar;

    public MAArenaPrepareStartEvent(Main plugin, String pluginPrefix, Arena arena, MAArenaScreenBossBar screenTitle) {
        this.plugin = plugin;
        this.pluginPrefix = pluginPrefix;
        this.arena = arena;
        this.players = new ArrayList<>(); // 初始化队列
        this.playerGameStatus = new PlayerGameStatus();
        this.countdownTime = plugin.getConfig().getInt("start-countdown-time", 30);
        this.screenBossbar = new MAArenaScreenBossBar(plugin);
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

    /**
     * 将玩家添加到队列
     * @param player 玩家
     */
    public void addPlayerToArena(Player player) {
        if (players.contains(player)) {
            player.sendMessage(ChatColor.RED + "你已经在 " + arena.getName() + " 地图的队列中.");
            return;
        }
        players.add(player);
        //  触发加入竞技场事件
        MAArenaJoinEvent joinEvent = new MAArenaJoinEvent(player, arena);
        Bukkit.getPluginManager().callEvent(joinEvent);
        player.sendMessage(ChatColor.GREEN + "你已加入 " + arena.getName() + " 地图队列.");
        // 检查是否需要启动倒计时
        if (players.size() >= arena.getMinPlayer() && arenaPreStartTimer == null) {
            startCountdown(countdownTime); // 使用读取的倒计时
        }
    }

    /**
     * 将玩家从队列中移除
     * @param player 玩家
     * @return 是否成功移除
     */
    public boolean removePlayerFromQueue(Player player) {
        if (!players.contains(player)) {
            player.sendMessage(ChatColor.RED + "你不在任何队列中.");
            return false;
        }

        players.remove(player);
        player.sendMessage(pluginPrefix + ChatColor.YELLOW + " 你已成功退出队列.");

        // 如果队列人数不足，取消倒计时
        if (players.size() < arena.getMinPlayer() && arenaPreStartTimer != null) {
            cancelCountdown();
            players.forEach(p -> p.sendMessage(ChatColor.RED + "由于队伍人数不足,倒计时已取消."));
        }

        // 如果倒计时已结束且游戏未开始，确保不再启动游戏
        if (players.size() < arena.getMinPlayer() && arenaPreStartTimer == null) {
            // 在此处可以加入逻辑来处理未开始的游戏状态，比如重置游戏状态
            Bukkit.getLogger().info("游戏未能开始,队伍人数不足,已取消游戏启动.");
        }

        return true;
    }


    /**
     * 启动游戏倒计时
     * @param countdownTime 倒计时时间（秒）
     */
    public void startCountdown(int countdownTime) {
        arenaPreStartTimer = new BukkitRunnable() {
            int timeLeft = countdownTime;

            @Override
            public void run() {
                // 检查队列人数是否不足
                if (players.size() < arena.getMinPlayer()) {
                    cancel();
                    players.forEach(player -> player.sendMessage(ChatColor.RED + "游戏因人数不足而取消."));
                    arenaPreStartTimer = null; // 清理引用
                    return;
                }

                // 倒计时逻辑
                if (timeLeft > 0) {
                    players.forEach(player ->
                            player.sendMessage(ChatColor.GREEN + "游戏将在 " + ChatColor.YELLOW + timeLeft + ChatColor.GREEN + " 秒后开始..."));
                    timeLeft--;
                } else {
                    cancel();
                    players.forEach(player -> player.sendMessage(ChatColor.GREEN + "游戏开始!"));
                    startGame(); // 调用比赛开始逻辑
                    arenaPreStartTimer = null; // 清理引用
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("MobArena"), 0, 20); // 每秒更新
    }

    /**
     * 游戏开始逻辑
     */
    public void startGame() {

        arena.startGame(); // 调用Arena中更新标识的方法

        // 触发游戏开始事件
        MAArenaStartEvent startEvent = new MAArenaStartEvent(arena);
        Bukkit.getPluginManager().callEvent(startEvent); // 调用事件

        PlayerGameStatus playerGameStatus = plugin.getPlayerGameStatus();
        MAArenaScreenTitle screenTitle = new MAArenaScreenTitle(plugin);

        players.forEach(player -> {
            playerGameStatus.setPlayerTrueInGame(player, arena); // 将玩家状态设置为在游戏中
            player.closeInventory();
            player.sendMessage(pluginPrefix + ChatColor.GREEN + " 你的游戏标记已更新为true.");
            player.sendMessage(ChatColor.GOLD + "你正在 " + ChatColor.LIGHT_PURPLE + arena.getDisplayName() + ChatColor.GOLD + " 地图上进行游戏.");

            // 初始化背包并给予商店道具
            MAQueueUtils queueUtils = new MAQueueUtils(plugin);
            queueUtils.clearQueueItems(player);
            // queueUtils.giveShopItem(player);

            // 传送玩家到起始位置
            Location startLocation = arena.getStartLocation();
            player.teleport(startLocation);

            // 更新计分板
            System.out.println(player.getName() + " [DEBUG] 进入游戏,更新计分板为在游戏中.");
            plugin.getMAScoreboard().updateScoreboard(player, true);

            screenBossbar.createBossBarForPlayers(players); // 创建 BossBar
            screenTitle.sendTitleToPlayer(player);

            // 其他游戏开始逻辑

            player.sendMessage(ChatColor.GOLD + arena.getDisplayName() + " 游戏开始逻辑执行完成.");
        });

        // 创建一个定时任务来定期更新计分板
        new BukkitRunnable() {
            @Override
            public void run() {
                players.forEach(player -> {
                    // 使用 PlayerGameStatus 来检查玩家是否在游戏中并更新计分板
                    boolean inGame = playerGameStatus.isPlayerCurrentInGame(player);
                    plugin.getMAScoreboard().updateScoreboard(player, inGame); // 始终传递当前状态
                });
            }
        }.runTaskTimer(plugin, 0, 20); // 每秒更新

        // 游戏开始后清空队列
        //// players.clear();

        // TODO: 在此处添加更多游戏启动的逻辑，比如设置比赛状态、初始化地图等。
    }

    /**
     * 取消倒计时
     */
    public void cancelCountdown() {
        if (arenaPreStartTimer != null) {
            arenaPreStartTimer.cancel();
            arenaPreStartTimer = null; // 清理引用
        }
    }
}
