package top.brmc.ampura16.mobarena.events;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import top.brmc.ampura16.mobarena.prearena.Arena;

import java.util.List;

public class MAArenaEndEventListener implements Listener {
    private final JavaPlugin plugin;
    private final FileConfiguration arenasConfig;
    private final String localListenerName = ChatColor.GOLD + "[MAArenaEndEventListener]";

    public MAArenaEndEventListener(JavaPlugin plugin, FileConfiguration arenasConfig) {
        this.plugin = plugin;
        this.arenasConfig = arenasConfig;
    }

    @EventHandler
    public void onMAArenaEnd(MAArenaEndEvent event) {
        // 获取刚刚结束游戏的玩家和竞技场
        List<Player> players = event.getPlayers();
        Arena arena = event.getArena();
        // 向每个玩家发送提示消息
        players.forEach(player -> {
            player.sendMessage(localListenerName + ChatColor.GREEN + " 已调用.");
            player.sendMessage(localListenerName + ChatColor.YELLOW + " 竞技场 " + arena.getDisplayName() + " 已重置.");
        });
        // 日志输出到控制台
        plugin.getLogger().info(localListenerName + " 游戏已结束,竞技场 " + arena.getDisplayName() + " 已重置.");
    }
}
