package top.brmc.ampura16.mobarena.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import top.brmc.ampura16.mobarena.prearena.Arena;
import top.brmc.ampura16.mobarena.prearena.MapManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 处理 MobArena 插件命令的自动补全功能.
 */
public class MATabCompleter implements TabCompleter {

    private final List<String> commandList = Arrays.asList("help", "gui", "leave", "create", "remove", "maplist", "admin", "reload", "test");
    private final List<String> testSubCommandList = Arrays.asList("status", "info", "logic-leave-current-game", "spawnmob", "spawnmythicmobs", "checkmobspawnloc", "checkroundconfig");
    private final List<String> adminSubCommandList = Arrays.asList("setwaitloc", "setstartloc", "setmobspawnloc");
    private final File mobsFolder;
    private final MapManager mapManager;

    /**
     * 构造函数，初始化 MATabCompleter 实例.
     *
     * @param mobsFolder 存放怪物配置文件的文件夹
     * @param mapManager 地图管理器，用于获取和管理地图信息
     */
    public MATabCompleter(File mobsFolder, MapManager mapManager) {
        this.mobsFolder = mobsFolder;
        this.mapManager = mapManager;
    }

    /**
     * 处理命令的自动补全.
     *
     * @param sender 命令发送者
     * @param command 命令对象
     * @param label 命令标签
     * @param args 命令参数
     * @return 建议的补全列表
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // 如果没有参数，则返回所有主命令
        if (args.length == 1) {
            return getMatchingCommands(args[0]);
        }
        // 如果第一个参数是 test，则返回 test 的子命令
        if (args.length == 2 && "test".equalsIgnoreCase(args[0])) {
            return getMatchingCommands(args[1], testSubCommandList);
        }
        // 如果第一个参数是 test 且第二个参数是 spawnmob，则返回 YAML 文件名
        if (args.length == 3 && "test".equalsIgnoreCase(args[0]) && "spawnmob".equalsIgnoreCase(args[1])) {
            return getMobFileNames(args[2]);
        }
        // 如果第一个参数是 admin，则返回 admin 的子命令
        if (args.length == 2 && "admin".equalsIgnoreCase(args[0])) {
            return getMatchingCommands(args[1], adminSubCommandList);
        }
        // 如果第一个参数是 admin 且第二个参数是 setwaitloc、setstartloc 或 setmobspawnloc，返回地图名称
        if (args.length == 3 && "admin".equalsIgnoreCase(args[0]) &&
                (args[1].equalsIgnoreCase("setwaitloc") ||
                        args[1].equalsIgnoreCase("setstartloc") ||
                        args[1].equalsIgnoreCase("setmobspawnloc"))) {
            return getArenaNames(); // 获取地图名称的建议
        }
        // 如果第一个参数是 test 且第二个参数是 checkmobspawnloc，返回地图名称
        if (args.length == 3 && "test".equalsIgnoreCase(args[0]) && "checkmobspawnloc".equalsIgnoreCase(args[1])) {
            return getArenaNames(); // 获取地图名称的建议
        }
        // 如果第一个参数是 test 且第二个参数是 checkroundconfig，返回地图名称
        if (args.length == 3 && "test".equalsIgnoreCase(args[0]) && "checkroundconfig".equalsIgnoreCase(args[1])) {
            return getArenaNames(); // 获取地图名称的建议
        }
        // 返回空列表以禁止进一步的补全
        return new ArrayList<>();
    }

    /**
     * 获取与指定前缀匹配的主命令列表.
     *
     * @param prefix 输入的命令前缀
     * @return 匹配的命令列表
     */
    private List<String> getMatchingCommands(String prefix) {
        List<String> suggestions = new ArrayList<>();
        for (String cmd : commandList) {
            if (cmd.startsWith(prefix.toLowerCase())) {
                suggestions.add(cmd);
            }
        }
        return suggestions;
    }

    /**
     * 获取与指定前缀匹配的命令列表.
     *
     * @param prefix 输入的命令前缀
     * @param commands 可供匹配的命令列表
     * @return 匹配的命令列表
     */
    private List<String> getMatchingCommands(String prefix, List<String> commands) {
        List<String> suggestions = new ArrayList<>();
        for (String cmd : commands) {
            if (cmd.startsWith(prefix.toLowerCase())) {
                suggestions.add(cmd);
            }
        }
        return suggestions;
    }

    /**
     * 获取以指定前缀匹配的怪物 YAML 文件名.
     *
     * @param prefix 输入的文件名前缀
     * @return 匹配的文件名列表
     */
    private List<String> getMobFileNames(String prefix) {
        List<String> suggestions = new ArrayList<>();
        if (mobsFolder.exists() && mobsFolder.isDirectory()) {
            File[] files = mobsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    String fileNameWithoutExtension = file.getName().substring(0, file.getName().length() - 4); // 去掉 .yml 后缀
                    if (fileNameWithoutExtension.startsWith(prefix.toLowerCase())) {
                        suggestions.add(fileNameWithoutExtension);
                    }
                }
            }
        }
        return suggestions;
    }

    /**
     * 获取当前可用的地图名称列表.
     *
     * @return 地图名称列表
     */
    private List<String> getArenaNames() {
        List<String> arenaNames = new ArrayList<>();
        for (Arena arena : mapManager.getArenaList()) { // 使用 mapManager 获取地图列表
            arenaNames.add(arena.getName());
        }
        return arenaNames;
    }
}
