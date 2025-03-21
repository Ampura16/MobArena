package top.brmc.ampura16.mobarena.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import top.brmc.ampura16.mobarena.prearena.Arena;

public class MAArenaRoundUpdateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Arena arena;
    private final int currentRound;
    private final Player player; // 新增玩家字段

    public MAArenaRoundUpdateEvent(Arena arena, int currentRound, Player player) {
        this.arena = arena;
        this.currentRound = currentRound;
        this.player = player;
    }

    public Arena getArena() {
        return arena;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public Player getPlayer() {
        return player;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}



