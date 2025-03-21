package top.brmc.ampura16.mobarena.arena;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import top.brmc.ampura16.mobarena.Main;

/**
 * 该类用于向玩家发送游戏标题和副标题
 */
public class MAArenaScreenTitle {

    private final Main plugin;

    /**
     * 创建一个新的 MAArenaScreenTitle 实例
     * @param plugin 插件的实例
     */
    public MAArenaScreenTitle(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * 向指定玩家播放标题
     * @param player 要播放标题的玩家
     */
    public void sendTitleToPlayer(Player player) {
        FileConfiguration config = plugin.getConfig(); // 获取配置文件

        String mainTitle = config.getString("Title.main-title", "&a&l游戏开始");
        String subTitle = config.getString("Title.sub-title", "&e&lMobArena &b&l- &c&l怪物竞技场");

        // 发送标题和副标题
        player.sendTitle(ChatColor.translateAlternateColorCodes('&', mainTitle),
                ChatColor.translateAlternateColorCodes('&', subTitle),
                10, 70, 20); // fadeIn, stay, fadeOut
    }
}
