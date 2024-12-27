package top.blug.mobarena.mobarena.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MATabCompleter implements TabCompleter {

    private final List<String> commandList = Arrays.asList("help", "gui", "leave", "create", "remove", "maplist", "reload");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // 如果没有参数，则返回所有命令
        if (args.length == 1) {
            return getMatchingCommands(args[0]);
        }
        // 返回空列表以禁止进一步的补全
        return new ArrayList<>();
    }

    private List<String> getMatchingCommands(String prefix) {
        List<String> suggestions = new ArrayList<>();
        for (String cmd : commandList) {
            if (cmd.startsWith(prefix.toLowerCase())) {
                suggestions.add(cmd);
            }
        }
        return suggestions;
    }
}
