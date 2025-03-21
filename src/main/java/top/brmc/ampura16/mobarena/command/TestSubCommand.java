package top.brmc.ampura16.mobarena.command;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import top.brmc.ampura16.mobarena.Main;
import top.brmc.ampura16.mobarena.events.MAArenaEndEvent;
import top.brmc.ampura16.mobarena.prearena.Arena;
import top.brmc.ampura16.mobarena.prearena.MapManager;
import top.brmc.ampura16.mobarena.prearena.PlayerGameStatus;

import java.util.List;

public class TestSubCommand {
    private final Main plugin;
    private final String pluginPrefix;
    private final MapManager mapManager;
    private final SpawnMobCommand spawnMobCommand;
    private final SpawnMythicMobCommand spawnMythicMobCommand;

    public TestSubCommand(Main plugin, String pluginPrefix, MapManager mapManager, SpawnMobCommand spawnMobCommand, SpawnMythicMobCommand spawnMythicMobCommand) {
        this.plugin = plugin;
        this.pluginPrefix = pluginPrefix;
        this.mapManager = mapManager;
        this.spawnMobCommand = spawnMobCommand;
        this.spawnMythicMobCommand = spawnMythicMobCommand;
    }

    /**
     * 处理 test 子命令.
     *
     * @param player     玩家对象
     * @param subCommand 子命令
     * @param args       命令参数
     */
    public void handleTestSubCommand(Player player, String subCommand, String[] args) {
        PlayerGameStatus playerGameStatus = plugin.getPlayerGameStatus(); // 获取持久化的状态管理实例
        Arena playerArena = playerGameStatus.getPlayerArena(player); // 获取当前玩家所在的地图
        boolean inGame = playerGameStatus.isPlayerInGame(player); // 检查玩家是否在游戏中

        switch (subCommand.toLowerCase()) {
            case "status":
                if (inGame) {
                    player.sendMessage(ChatColor.GREEN + "你当前所在地图为: " + playerArena.getDisplayName() + ", 游戏状态为: " + playerGameStatus.isPlayerCurrentInGame(player));
                } else {
                    player.sendMessage(ChatColor.RED + " 你当前不在任何游戏中, 游戏状态为: " + playerGameStatus.isPlayerCurrentInGame(player));
                }
                break;

            case "info":
                player.sendMessage(ChatColor.AQUA + "玩家信息: ");
                if (inGame) {
                    player.sendMessage(ChatColor.AQUA + "你当前所在地图为: " + playerArena.getDisplayName() + ".");
                } else {
                    player.sendMessage(ChatColor.AQUA + "你当前不在任何游戏中.");
                }
                break;

            case "logic-leave-current-game":
                if (inGame) {
                    List<Player> playersInGame = mapManager.getPlayersInGame(playerArena);
                    MAArenaEndEvent endEvent = new MAArenaEndEvent(playerGameStatus, pluginPrefix, playersInGame, playerArena);
                    endEvent.endGame();
                    boolean removedFromQueue = mapManager.leaveQueue(player); // 从队列中移除玩家
                    if (removedFromQueue) {
                        playerGameStatus.setPlayerNotInGame(player); // 确保状态更新为不在游戏中
                        player.sendMessage(pluginPrefix + ChatColor.GREEN + " 你已成功退出游戏,状态已更新为 false.");
                    } else {
                        player.sendMessage(pluginPrefix + ChatColor.RED + " 状态更新失败,请检查逻辑.");
                    }
                } else {
                    player.sendMessage(pluginPrefix + ChatColor.RED + " 你当前不在任何游戏中,无法退出.");
                }
                break;

            case "spawnmob":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "用法: /ma test spawnmob <怪物名称>");
                    return;
                }
                String mobName = args[2]; // 从 args[2] 获取怪物名称
                spawnMobCommand.spawnMob(player, mobName);
                break;

            case "spawnmythicmobs":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "用法: /ma test spawnmythicmobs <怪物名称>");
                    return;
                }
                String mythicMobName = args[2]; // 从 args[2] 获取怪物名称
                spawnMythicMobCommand.spawnMob(player, mythicMobName); // 调用生成怪物的方法
                break;

            case "checkmobspawnloc":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "请提供地图名称: /ma test checkmobspawnloc <地图名称>");
                    return;
                }
                String arenaNameMob = args[2];
                System.out.println("获取的地图名称: " + arenaNameMob); // 调试输出，确认获取的名称

                CheckMobSpawnLocCommand checkMobSpawnLocCommand = new CheckMobSpawnLocCommand(plugin);
                checkMobSpawnLocCommand.execute(player, arenaNameMob); // 执行命令
                break;

            case "checkroundconfig":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "请提供地图名称: /ma test checkroundconfig <地图名称>");
                    return;
                }
                String arenaNameRound = args[2];
                System.out.println("获取的地图名称: " + arenaNameRound); // 调试输出，确认获取的名称

                CheckArenaRoundConfigCommand checkArenaRoundConfigCommand = new CheckArenaRoundConfigCommand(plugin);
                checkArenaRoundConfigCommand.execute(player, arenaNameRound); // 执行命令
                break;

            default:
                player.sendMessage(pluginPrefix + ChatColor.GOLD + " 未知的子命令: " + subCommand + ". 请使用: /ma test <status|info|logic-leave-current-game|mob>");
                break;
        }
    }
}
