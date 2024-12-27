package top.blug.mobarena.mobarena.command;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import top.blug.mobarena.mobarena.arena.MapManager;
import top.blug.mobarena.mobarena.arena.SelectMapGUI;
import top.blug.mobarena.mobarena.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MACommand implements CommandExecutor, Listener {

    private final Main plugin;
    private final MapManager mapManager;

    public MACommand(Main plugin) {
        this.plugin = plugin;
        this.mapManager = plugin.getMapManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            printConsoleHelpMessages(sender);
            return true;
        }

        Player player = (Player) sender;

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

            case "reload":
                if (player.hasPermission("mobarena.reload")) {
                    mapManager.reloadConfigs();
                    player.sendMessage(ChatColor.GREEN + "插件配置已重载.");
                } else {
                    player.sendMessage(ChatColor.RED + "你没有权限执行此命令.");
                }
                break;

            default:
                player.sendMessage(ChatColor.GOLD + "未知的命令,请查看指令帮助.");
                printPlayerHelpMessages(player);
        }
        return true;
    }

    // create子命令
    private void handleCreateCommand(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(ChatColor.RED + "用法: /ma create <地图名称> <显示名称> <最小玩家数> <最大玩家数> [材质（可选）] [描述信息（可选）]");
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
            player.sendMessage(ChatColor.GREEN + "地图创建成功!");
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "最小玩家数和最大玩家数必须是整数.");
        }
    }

    // remove子命令
    private void handleRemoveCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "用法: /ma remove <地图名称>");
            return;
        }
        String mapToRemove = args[1];
        mapManager.removeMap(player, mapToRemove);
    }

    private void printPlayerHelpMessages(Player player) {
        player.sendMessage(ChatColor.AQUA + "==========[ MobArena - 指令帮助 ]==========");
        player.sendMessage(ChatColor.GOLD + "/ma help - 查看指令帮助");
        player.sendMessage(ChatColor.GOLD + "/ma gui - 打开选择地图GUI");
        player.sendMessage(ChatColor.GOLD + "/ma create <地图名称> <显示名称> <最小玩家数> <最大玩家数> [材质（可选）] [描述信息（可选）] - 创建地图");
        player.sendMessage(ChatColor.GOLD + "/ma remove <地图名称> - 删除一个现有地图");
        player.sendMessage(ChatColor.GOLD + "/ma maplist - 展示可用地图列表");
        player.sendMessage(ChatColor.GOLD + "/ma reload - 重载插件配置");
        player.sendMessage(ChatColor.RED + "⊙ 注意: reload 命令不太好使,如遇问题请使用 PlugmanX 彻底重启本插件!");
        player.sendMessage(ChatColor.AQUA + "===============================================");
    }

    private void printConsoleHelpMessages(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "==============================");
        sender.sendMessage(ChatColor.AQUA + "MobArena - 指令帮助");
        sender.sendMessage(ChatColor.AQUA + "==============================");
    }

}
