package top.brmc.ampura16.mobarena.arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.boss.BossBar;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * 该类用于管理在游戏中为玩家显示的 BossBar.
 * 包括创建、更新和移除 BossBar 的功能。
 */
public class MAArenaScreenBossBar {
    private final Map<Player, BossBar> bossBars = new HashMap<>();
    private final JavaPlugin plugin;

    /**
     * 创建一个新的 MAArenaScreenBossBar 实例.
     *
     * @param plugin 插件的实例
     */
    public MAArenaScreenBossBar(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 获取 BossBar 显示的持续时间，以秒为单位.
     *
     * @return BossBar 的显示时间
     */
    public int getBossBarDisplayTime() {
        return plugin.getConfig().getInt("bossbar-info.display-time", 3600);
    }

    /**
     * 获取 BossBar 的标题.
     *
     * @return BossBar 的标题，支持颜色代码
     */
    public String getBossBarTitle() {
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("bossbar-info.title", "&a默认标题"));
    }

    /**
     * 获取 BossBar 的颜色.
     *
     * @return BossBar 的颜色
     */
    public BarColor getBossBarColor() {
        String colorString = plugin.getConfig().getString("bossbar-info.color", "blue").toUpperCase();
        try {
            return BarColor.valueOf(colorString);
        } catch (IllegalArgumentException e) {
            return BarColor.BLUE; // 默认颜色
        }
    }

    /**
     * 获取 BossBar 的样式.
     *
     * @return BossBar 的样式
     */
    public BarStyle getBossBarStyle() {
        String styleString = plugin.getConfig().getString("bossbar-info.style", "solid").toUpperCase();
        try {
            return BarStyle.valueOf(styleString); // 获取样式
        } catch (IllegalArgumentException e) {
            return BarStyle.SOLID; // 默认样式
        }
    }

    /**
     * 为指定玩家创建 BossBar.
     *
     * @param players 要为其创建 BossBar 的玩家集合
     */
    public void createBossBarForPlayers(Iterable<Player> players) {
        String title = getBossBarTitle();
        BarColor color = getBossBarColor();
        BarStyle style = getBossBarStyle();

        for (Player player : players) {
            BossBar bossBar = Bukkit.createBossBar(title, color, style);
            bossBar.addPlayer(player);
            bossBars.put(player, bossBar);

            // 创建一个延迟任务，在指定时间后移除 BossBar
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    removeBossBarForPlayer(player);
                }
            }, getBossBarDisplayTime() * 20L);

            // 设置 BossBar 的进度
            bossBar.setProgress(1.0); // 设置为 100%（完成状态）
        }
    }

    /**
     * 更新指定玩家的 BossBar 进度.
     *
     * @param players 要更新的玩家集合
     * @param progress 新的进度值（范围 0.0 到 1.0）
     */
    public void updateBossBarForPlayers(Iterable<Player> players, double progress) {
        for (Player player : players) {
            BossBar bossBar = bossBars.get(player);
            if (bossBar != null) {
                bossBar.setProgress(progress);
            }
        }
    }

    /**
     * 移除所有玩家的 BossBar.
     */
    public void removeBossBarForPlayers() {
        for (Player player : bossBars.keySet()) {
            BossBar bossBar = bossBars.get(player);
            if (bossBar != null) {
                bossBar.removeAll();
            }
        }
        bossBars.clear();
    }

    /**
     * 移除特定玩家的 BossBar.
     *
     * @param player 要移除 BossBar 的玩家
     */
    public void removeBossBarForPlayer(Player player) {
        BossBar bossBar = bossBars.get(player);
        if (bossBar != null) {
            bossBar.removeAll(); // 从该玩家移除 BossBar
            bossBars.remove(player); // 从映射中移除该玩家的记录
            // player.sendMessage(ChatColor.GREEN + "Bossbar已移除.");
        }
    }

    /**
     * 设置指定玩家的 BossBar 标题
     *
     * @param players 要更新标题的玩家集合
     * @param title   新的 BossBar 标题
     */
    public void setBossBarTitle(Iterable<Player> players, String title) {
        for (Player player : players) {
            BossBar bossBar = bossBars.get(player);
            if (bossBar != null) {
                bossBar.setTitle(title);
            }
        }
    }
}
