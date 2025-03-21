package top.brmc.ampura16.mobarena.command;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import top.brmc.ampura16.mobarena.Main;
import top.brmc.ampura16.mobarena.prearena.MapManager;
import top.brmc.ampura16.mobarena.prearena.SelectMapGUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 处理 MobArena 插件的所有命令，包括玩家和管理员命令.
 */
public class MACommand implements CommandExecutor, Listener {

    private final Main plugin;
    private final String pluginPrefix;
    private final MapManager mapManager;
    private final TestSubCommand testSubCommand; // 新增 TestSubCommand 的实例
    private final AdminSubCommand adminSubCommand; // 新增 AdminSubCommand 的实例

    /**
     * 构造函数，初始化 MACommand 实例.
     *
     * @param plugin 主插件实例
     * @param pluginPrefix 插件前缀，用于消息显示
     */
    public MACommand(Main plugin, String pluginPrefix) {
        this.plugin = plugin;
        this.pluginPrefix = pluginPrefix;
        this.mapManager = plugin.getMapManager();
        SpawnMobCommand spawnMobCommand = new SpawnMobCommand(plugin);
        SpawnMythicMobCommand spawnMythicMobCommand = new SpawnMythicMobCommand(); // 声明 spawnMythicMobCommand
        MASetMobSpawnLocationCommand setMobSpawnLocationCommand = new MASetMobSpawnLocationCommand(mapManager, pluginPrefix);
        this.testSubCommand = new TestSubCommand(plugin, pluginPrefix, mapManager, spawnMobCommand, spawnMythicMobCommand); // 实例化 TestSubCommand
        this.adminSubCommand = new AdminSubCommand(pluginPrefix, mapManager, setMobSpawnLocationCommand); // 实例化 AdminSubCommand
    }

    /**
     * 处理玩家输入的命令.
     *
     * @param sender 命令发送者
     * @param cmd 命令对象
     * @param label 命令标签
     * @param args 命令参数
     * @return 返回命令执行的结果
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            printConsoleHelpMessages(sender);
            return true;
        }

        if (args.length == 0) {
            printPlayerHelpMessages(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                printPlayerHelpMessages(player);
                break;

            case "create":
                handleCreateCommand(player, args);
                break;

            case "remove":
                handleRemoveCommand(player, args);
                break;

            case "gui":
                new SelectMapGUI(mapManager.getArenaList(), plugin.getConfig()).openSelectMapGUI(player);
                break;

            case "leave":
                mapManager.leaveQueue(player);
                break;

            case "maplist":
                mapManager.listMaps(player);
                break;

            case "test":
                if (args.length == 1) {
                    player.sendMessage(pluginPrefix + ChatColor.GOLD + " 二级子命令为空或无效.");
                } else {
                    // 调用 TestSubCommand 中的方法
                    testSubCommand.handleTestSubCommand(player, args[1], args);
                }
                break;

            case "admin":
                adminSubCommand.handleAdminCommands(player, args); // 调用 AdminSubCommand 中的方法
                break;

            case "reload":
                if (player.hasPermission("mobarena.admin")) {
                    plugin.reloadConfigurations(); // 调用 Main 类中的重载配置方法
                    player.sendMessage(pluginPrefix + ChatColor.GREEN + " 插件配置已重载.");
                } else {
                    player.sendMessage(pluginPrefix + ChatColor.RED + " 你没有权限执行此命令.");
                }
                break;

            default:
                player.sendMessage(pluginPrefix + ChatColor.GOLD + " 未知的命令,请查看指令帮助.");
                printPlayerHelpMessages(player);
        }
        return true;
    }

    // create子命令
    private void handleCreateCommand(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(ChatColor.RED + "用法: /ma create <地图名称> <显示名称> <最小玩家数> <最大玩家数> [材质(可选)] [描述信息(可选)]");
            return;
        }
        String mapName = args[1];
        String displayName = args[2];

        try {
            int minPlayer = Integer.parseInt(args[3]);
            int maxPlayer = Integer.parseInt(args[4]);
            String materialName = (args.length >= 6) ? args[5].toUpperCase() : "BRICKS";

            List<String> lore = new ArrayList<>();
            if (args.length > 6) {
                // 解析从第七个参数开始的所有内容作为描述信息
                lore = Arrays.asList(Arrays.copyOfRange(args, 6, args.length));
            }

            mapManager.createMap(player, mapName, displayName, materialName, minPlayer, maxPlayer, lore);
            // player.sendMessage(ChatColor.GREEN + "地图创建成功.");
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + " 最小玩家数和最大玩家数必须是整数.");
        }
    }

    // remove子命令
    private void handleRemoveCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + " 用法: /ma remove <地图名称>");
            return;
        }
        String mapToRemove = args[1];
        mapManager.removeMap(player, mapToRemove);
    }

    /**
     * 打印玩家帮助消息.
     *
     * @param player 玩家对象
     */
    private void printPlayerHelpMessages(Player player) {
        player.sendMessage(ChatColor.AQUA + "==========[ MobArena - 指令帮助 ]==========");
        player.sendMessage(ChatColor.GOLD + "/ma help - 查看指令帮助");
        player.sendMessage(ChatColor.GOLD + "/ma gui - 打开选择地图GUI");
        player.sendMessage(ChatColor.GOLD + "/ma create <地图名称> <显示名称> <最小玩家数> <最大玩家数> [材质(可选)] [描述信息(可选)] - 创建地图");
        player.sendMessage(ChatColor.GOLD + "/ma remove <地图名称> - 删除一个现有地图");
        player.sendMessage(ChatColor.GOLD + "/ma maplist - 展示可用地图列表");
        player.sendMessage(ChatColor.RED + "/ma admin - 管理员指令");
        player.sendMessage(ChatColor.GOLD + "/ma reload - 重载插件配置");
        player.sendMessage(ChatColor.RED + "⊙ 注意: reload 命令不太好使,如遇问题请使用 PlugmanX 彻底重启本插件!");
        player.sendMessage(ChatColor.AQUA + "==================================================");
    }

    /**
     * 打印控制台帮助消息.
     *
     * @param sender 控制台发送者
     */
    private void printConsoleHelpMessages(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "==============================");
        sender.sendMessage(ChatColor.AQUA + "MobArena - 指令帮助");
        sender.sendMessage(ChatColor.AQUA + "==============================");
    }
}
