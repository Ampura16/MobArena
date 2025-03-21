package top.brmc.ampura16.mobarena.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import top.brmc.ampura16.mobarena.prearena.Arena;

/**
 * 竞技场开始事件类
 */
public class MAArenaStartEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Arena arena; // 被启动的竞技场

    /**
     * 事件构造函数
     *
     * @param arena 竞技场实例
     */
    public MAArenaStartEvent(Arena arena) {
        this.arena = arena;
    }

    /**
     * 获取被启动的竞技场实例
     *
     * @return 竞技场实例
     */
    public Arena getArena() {
        return arena;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers; // 返回事件处理器列表
    }

    public static HandlerList getHandlerList() {
        return handlers; // 返回处理器列表的静态方法
    }
}
