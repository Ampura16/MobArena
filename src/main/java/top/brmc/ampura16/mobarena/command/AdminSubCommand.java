package top.brmc.ampura16.mobarena.command;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import top.brmc.ampura16.mobarena.prearena.Arena;
import top.brmc.ampura16.mobarena.prearena.MapManager;

import java.io.IOException;

public class AdminSubCommand {
    private final String pluginPrefix;
    private final MapManager mapManager;
    private final MASetMobSpawnLocationCommand setMobSpawnLocationCommand;

    public AdminSubCommand(String pluginPrefix, MapManager mapManager, MASetMobSpawnLocationCommand setMobSpawnLocationCommand) {
        this.pluginPrefix = pluginPrefix;
        this.mapManager = mapManager;
        this.setMobSpawnLocationCommand = setMobSpawnLocationCommand;
    }

    /**
     * 处理 admin 子命令.
     *
     * @param player 玩家对象
     * @param args   命令参数
     */
    public void handleAdminCommands(Player player, String[] args) {
        // 权限检查
        if (!player.hasPermission("mobarena.admin")) {
            player.sendMessage(pluginPrefix + ChatColor.RED + " 你没有权限执行此命令.");
            return;
        }

        // 检查是否有子命令
        if (args.length < 2) {
            printAdminHelpMessages(player);
            return;
        }

        String command = args[1].toLowerCase();
        switch (command) {
            case "setwaitloc" -> {
                if (args.length < 3) {
                    printAdminHelpMessages(player);
                    return;
                }
                String waitMapName = args[2];
                Arena waitArena = mapManager.getArenaByName(waitMapName); // 从 MapManager 获取 Arena 对象
                if (waitArena == null) {
                    player.sendMessage(pluginPrefix + ChatColor.RED + " 地图名称无效: " + waitMapName);
                    return;
                }

                // 获取玩家的当前位置
                Location waitLocation = player.getLocation();
                String waitWorldName = waitLocation.getWorld().getName();
                double waitX = waitLocation.getX();
                double waitY = waitLocation.getY();
                double waitZ = waitLocation.getZ();

                // 更新配置文件中的等待位置
                FileConfiguration arenasConfig = mapManager.getArenasConfig(); // 获取地图配置
                arenasConfig.set(waitMapName + ".waitingLocation.world", waitWorldName);
                arenasConfig.set(waitMapName + ".waitingLocation.x", waitX);
                arenasConfig.set(waitMapName + ".waitingLocation.y", waitY);
                arenasConfig.set(waitMapName + ".waitingLocation.z", waitZ);

                // 保存配置文件
                try {
                    mapManager.saveArenasConfig(); // 执行保存方法
                } catch (IOException e) {
                    player.sendMessage(pluginPrefix + ChatColor.RED + " 保存配置时出错: " + e.getMessage());
                    return;
                }
                player.sendMessage(pluginPrefix + ChatColor.GREEN + " 地图 " + waitMapName + " 的等待大厅已设置为当前位置.");
                player.sendMessage(pluginPrefix + ChatColor.GREEN + " 现在使用/ma admin setstartloc <地图名称> 设置游戏开始传送位置.");
            }
            case "setstartloc" -> {
                if (args.length < 3) {
                    printAdminHelpMessages(player);
                    return;
                }
                String startMapName = args[2];
                Arena startArena = mapManager.getArenaByName(startMapName); // 从 MapManager 获取 Arena 对象
                if (startArena == null) {
                    player.sendMessage(pluginPrefix + ChatColor.RED + " 地图名称无效: " + startMapName);
                    return;
                }

                // 获取玩家的当前位置
                Location startLocation = player.getLocation();
                String startWorldName = startLocation.getWorld().getName();
                double startX = startLocation.getX();
                double startY = startLocation.getY();
                double startZ = startLocation.getZ();

                // 更新配置文件中的起始位置
                FileConfiguration arenasConfigStart = mapManager.getArenasConfig(); // 获取地图配置
                arenasConfigStart.set(startMapName + ".startLocation.world", startWorldName);
                arenasConfigStart.set(startMapName + ".startLocation.x", startX);
                arenasConfigStart.set(startMapName + ".startLocation.y", startY);
                arenasConfigStart.set(startMapName + ".startLocation.z", startZ);

                // 保存配置文件
                try {
                    mapManager.saveArenasConfig(); // 执行保存方法
                } catch (IOException e) {
                    player.sendMessage(pluginPrefix + ChatColor.RED + " 保存配置时出错: " + e.getMessage());
                    return;
                }
                player.sendMessage(pluginPrefix + ChatColor.GREEN + " 地图 " + startMapName + " 的起始位置已设置为当前位置.");
                player.sendMessage(pluginPrefix + ChatColor.GOLD + " 由于技术问题,现在需要重启服务器才能正确保存配置.");
            }
            case "setmobspawnloc" -> {
                if (args.length < 3) {
                    printAdminHelpMessages(player);
                    return;
                }
                String spawnMapName = args[2];
                setMobSpawnLocationCommand.setMobSpawnLocation(player, spawnMapName);
            }
            default -> player.sendMessage(pluginPrefix + ChatColor.RED + " 未知的子命令: " + args[1]);
        }
    }

    /**
     * 打印管理员帮助消息.
     *
     * @param admin 管理员玩家对象
     */
    private void printAdminHelpMessages(Player admin) {
        admin.sendMessage(ChatColor.AQUA + "Usage: /ma admin");
        admin.sendMessage(ChatColor.GRAY + "        ├── " + ChatColor.GOLD + "setwaitloc <地图名称>" + ChatColor.GRAY + " - 设置等待大厅位置");
        admin.sendMessage(ChatColor.GRAY + "        ├── " + ChatColor.GOLD + "setstartloc <地图名称>" + ChatColor.GRAY + " - 设置起始位置");
        admin.sendMessage(ChatColor.GRAY + "        └── " + ChatColor.GOLD + "setmobspawnloc <地图名称>" + ChatColor.GRAY + " - 新增地图刷怪点");
        admin.sendMessage(ChatColor.RED + "⊙ 注意: " + ChatColor.GRAY + "暂时因为技术原因,创建地图后要及时设置等待位置并且重启服务器,才能正确识别...");
    }

}
