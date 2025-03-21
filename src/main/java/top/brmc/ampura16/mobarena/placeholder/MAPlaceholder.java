package top.brmc.ampura16.mobarena.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import top.brmc.ampura16.mobarena.Main;
import top.brmc.ampura16.mobarena.prearena.Arena;
import top.brmc.ampura16.mobarena.prearena.MapManager;

public class MAPlaceholder extends PlaceholderExpansion {

    private final Main plugin;

    public MAPlaceholder(Main plugin) {
        this.plugin = plugin;
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "mobarena"; // 变量前缀
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "Ampura16"; // 插件作者
    }

    @NotNull
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion(); // 插件版本
    }

    @Override
    public boolean persist() {
        return false; // 不需要持久化
    }

    @Override
    public boolean canRegister() {
        return true; // 可以注册
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {

        // 先处理玩家直接退出服务器的默认情况
        if (player == null || !player.isOnline()) {
            return "未知"; // 返回默认值
        }

        MapManager mapManager = plugin.getMapManager(); // 获取 MapManager 实例
        Arena currentArena = mapManager.getPlayerArena(player); // 获取当前竞技场

        switch (identifier) {
            case "currentmap":
                // 返回当前竞技场的展示名称
                return ChatColor.translateAlternateColorCodes('&', (currentArena != null) ? currentArena.getDisplayName() : "无");

            case "currentplayers":
                // 返回当前玩家数量
                return ChatColor.translateAlternateColorCodes('&', String.valueOf(currentArena != null ? currentArena.getCurrentPlayerCount() : 0));

            case "maxplayers":
                // 返回最大玩家数量
                return ChatColor.translateAlternateColorCodes('&', String.valueOf(currentArena != null ? currentArena.getMaxPlayer() : 0));

            case "currentmapstatus":
                // 返回当前地图状态（游戏状态），支持颜色
                currentArena = mapManager.getPlayerArena(player); // 重新获取当前竞技场
                String statusMessage = (currentArena != null) ? (currentArena.isGameStarted() ? "&a已开始" : "&e等待中") : "&6未加入游戏";
                return ChatColor.translateAlternateColorCodes('&', statusMessage);

            case "currentkit":
                // 获取玩家当前选择的职业名称
                String currentKit = getCurrentKit(player);
                return ChatColor.translateAlternateColorCodes('&', currentKit != null ? currentKit : "未选择职业");

            case "currentmobleft":
                // 获取当前回合剩余怪物数量
                return ChatColor.translateAlternateColorCodes('&', String.valueOf(getRemainingMobs(currentArena)));

            case "currentround":
                // 返回当前回合
                return ChatColor.translateAlternateColorCodes('&', (currentArena != null) ? String.valueOf(currentArena.getCurrentRound()) : "未加入游戏");

            default:
                return null; // 返回 null 以避免错误
        }
    }

    // 获取玩家当前选择的职业名称
    private String getCurrentKit(Player player) {
        if (player == null || !player.isOnline()) {
            return "未知"; // 返回默认值
        }
        PersistentDataContainer container = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "currentKit"); // 确保使用正确的 key
        return container.get(key, PersistentDataType.STRING); // 返回当前职业名称
    }


    // 获取当前回合还剩多少怪物
    private int getRemainingMobs(Arena arena) {
        if (arena == null || !arena.isGameStarted()) {
            return 0; // 如果竞技场未开始或不存在，返回0
        }
        return arena.getRemainingMobCount(); // 直接获取剩余怪物数量
    }
}
