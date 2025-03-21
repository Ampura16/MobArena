package top.brmc.ampura16.mobarena.events;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import top.brmc.ampura16.mobarena.arena.MAArenaMobSpawner;
import top.brmc.ampura16.mobarena.prearena.Arena;
import top.brmc.ampura16.mobarena.scoreboard.MAScoreboard;

import java.util.Set;
public class MAArenaStartEventListener implements Listener {
    private final JavaPlugin plugin; // 插件实例
    private final FileConfiguration arenasConfig; // arenasConfig
    private final FileConfiguration config; // 其他配置
    private final String localListenerName = ChatColor.GOLD + "[MAArenaStartEventListener]";
    private MAScoreboard scoreboard;
    // 构造函数，接受 FileConfiguration 参数
    public MAArenaStartEventListener(JavaPlugin plugin, FileConfiguration arenasConfig, FileConfiguration config, MAScoreboard scoreboard) {
        this.plugin = plugin;
        this.arenasConfig = arenasConfig; // 保存 arenasConfig
        this.config = config; // 保存其他配置
        this.scoreboard = scoreboard;
    }
    @EventHandler
    public void onArenaStart(MAArenaStartEvent event) {
        // 获取竞技场
        Arena arena = event.getArena();
        // 检查竞技场是否为 null
        if (arena == null) {
            plugin.getLogger().warning(localListenerName + "竞技场为空,无法启动.");
            return;
        }
        // 获取参与的玩家
        Set<Player> players = arena.getPlayersInArena();
        // 检查参与玩家集合是否有效
        if (players == null || players.isEmpty()) {
            plugin.getLogger().warning(localListenerName + " 没有玩家在竞技场中, 无法开始.");
            return;
        }
        // 从 config.yml 中读取 base-health 的值
        double baseHealth = config.getDouble("base-health", 40.0); // 默认值为 40.0
        // 发送反馈消息给所有参与玩家，并设置最大生命值为 baseHealth
        players.forEach(player -> {
            player.sendMessage(localListenerName + ChatColor.GREEN + " 已调用.");
            // 设置最大生命值为 baseHealth
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(baseHealth);
            // 如果当前生命值大于 baseHealth，将其设置为 baseHealth
            if (player.getHealth() > baseHealth) {
                player.setHealth(baseHealth);
            }
        });
        // 创建新的回合任务
        String pluginPrefix = config.getString("plugin-prefix", "&6[MobArena]");
        MAArenaRoundTask roundTask = new MAArenaRoundTask(plugin, null, arena, arena.getRounds().size(), scoreboard);
        // 创建新的怪物生成器并传递 roundTask
        MAArenaMobSpawner mobSpawner = new MAArenaMobSpawner(plugin, pluginPrefix, arena, arenasConfig, roundTask);
        // 将 mobSpawner 设置到 Arena 中
        arena.setMobSpawner(mobSpawner); // 将 mobSpawner 关联到 Arena
        // 启动回合任务
        roundTask.setMobSpawner(mobSpawner); // 在这里设置 mobSpawner
        roundTask.runTaskTimer(plugin, 0L, 20L); // 每20个 ticks 调用一次
        // 发布事件以开始第一回合
        for (Player player : arena.getPlayersInArena()) { // 为每个玩家触发事件
            Bukkit.getPluginManager().callEvent(new MAArenaRoundUpdateEvent(arena, 0, player)); // 通知回合更新
        }
    }
}
