package top.brmc.ampura16.mobarena.events;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import top.brmc.ampura16.mobarena.arena.MAArenaMobSpawner;
import top.brmc.ampura16.mobarena.prearena.Arena;

public class MAArenaRoundUpdateEventListener implements Listener {

    private final JavaPlugin plugin;
    private final FileConfiguration arenasConfig;
    private final String localListenerName = ChatColor.GOLD + "[MAArenaRoundUpdateListener]";

    public MAArenaRoundUpdateEventListener(JavaPlugin plugin, FileConfiguration arenasConfig) {
        this.plugin = plugin;
        this.arenasConfig = arenasConfig;
    }

    @EventHandler
    public void onRoundUpdate(MAArenaRoundUpdateEvent event) {
        Arena arena = event.getArena();
        int currentRound = event.getCurrentRound();
        Player player = event.getPlayer(); // 获取玩家对象

        // 从 Arena 获取现有的 mobSpawner
        MAArenaMobSpawner mobSpawner = arena.getMobSpawner();

        if (mobSpawner != null) {
            // 生成当前回合的怪物
            mobSpawner.spawnPerRoundMobs(currentRound);
            player.sendMessage(localListenerName + ChatColor.GREEN + " 已调用.");
        } else {
            plugin.getLogger().warning("未找到当前竞技场的 mobSpawner.");
        }
    }


}
