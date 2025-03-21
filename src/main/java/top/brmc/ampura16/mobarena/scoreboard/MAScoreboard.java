package top.brmc.ampura16.mobarena.scoreboard;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import top.brmc.ampura16.mobarena.Main;
import top.brmc.ampura16.mobarena.prearena.Arena;

public class MAScoreboard {

    private final Main plugin;

    public MAScoreboard(Main plugin) {
        this.plugin = plugin;
    }

    public String getTitle() {
        return plugin.getScoreboardConfig().getString("title", "默认标题").replace('&', '§');
    }

    public String getBoardLine(int line, boolean inGame) {
        String path = inGame ? "ingame.board-list.line-" : "waiting.board-list.line-";
        return plugin.getScoreboardConfig().getString(path + line, "&a&l@Ampura16").replace('&', '§');
    }

    /**
     * 更新指定玩家的计分板内容。
     *
     * @param player 需要更新计分板的玩家。
     */
    public void updateScoreboard(Player player, boolean inGame) {
        if (player == null || !player.isOnline()) {
            return; // 如果玩家为 null 或已离线，直接返回
        }
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("MobArena", "dummy", getTitle());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        for (int i = 1; i <= 10; i++) {
            String line = getBoardLine(i, inGame);
            if (!line.isEmpty()) {
                line = PlaceholderAPI.setPlaceholders(player, line);
                // 替换 currentplayers 变量
                line = line.replace("%mobarena_currentplayers%", String.valueOf(getCurrentPlayerCount(player)));
                if (!line.trim().isEmpty()) {
                    objective.getScore(line).setScore(10 - i); // 保持顺序
                }
            }
        }
        player.setScoreboard(scoreboard);
    }

    public void updateRoundScoreboard(Player player, Arena arena) {

        // 先处理玩家直接退出服务器的情况
        if (player == null || !player.isOnline()) {
            return; // 如果玩家为 null 或已离线，直接返回
        }

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("MobArena", "dummy", getTitle());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        for (int i = 1; i <= 10; i++) {
            String line = getBoardLine(i, true); // 只关心游戏中的计分板
            line = line.replace("%mobarena_currentround%", String.valueOf(arena.getCurrentRound()));
            line = line.replace("%mobarena_currentmobleft%", String.valueOf(arena.getRemainingMobCount()));
            line = PlaceholderAPI.setPlaceholders(player, line);
            if (!line.trim().isEmpty()) {
                objective.getScore(line).setScore(10 - i); // 保持顺序
            }
        }
        player.setScoreboard(scoreboard);
    }

    /**
     * 清空计分板
     * */
    public void clearPlayerScoreboard(Player player) {

        // 先处理玩家直接退出服务器的情况
        if (player == null || !player.isOnline()) {
            return; // 如果玩家为 null 或已离线，直接返回
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard(); // 获取 ScoreboardManager 并设置一个新的空计分板
        player.setScoreboard(scoreboard); // 设置一个新的空计分板
    }

    private int getCurrentPlayerCount(Player player) {
        Arena arena = plugin.getMapManager().getArenaByPlayer(player);
        return arena != null ? arena.getCurrentPlayerCount() : 0;
    }

}
