package top.brmc.ampura16.mobarena.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import top.brmc.ampura16.mobarena.prearena.Arena;

public class MAArenaLeaveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Arena arena;

    public MAArenaLeaveEvent(Player player, Arena arena) {
        this.player = player;
        this.arena = arena;
    }

    public Player getPlayer() {
        return player;
    }

    public Arena getArena() {
        return arena;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
