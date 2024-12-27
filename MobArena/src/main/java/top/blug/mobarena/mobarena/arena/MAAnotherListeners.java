package top.blug.mobarena.mobarena.arena;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import top.blug.mobarena.mobarena.Main;

public class MAAnotherListeners implements Listener {

    private final Main plugin;
    private final MapManager mapManager;

    public MAAnotherListeners(Main plugin) {
        this.plugin = plugin;
        this.mapManager = plugin.getMapManager();
    }

    @EventHandler
    public void playerItemGiver(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        mapManager.regiveSelectMapItem(player);
    }
}
