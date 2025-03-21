package top.brmc.ampura16.mobarena.command;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * 处理生成 MythicMob 怪物的命令.
 */
public class SpawnMythicMobCommand {

    private final MythicBukkit mythicBukkit;

    /**
     * 构造函数，初始化 SpawnMythicMobCommand 实例.
     */
    public SpawnMythicMobCommand() {
        this.mythicBukkit = MythicBukkit.inst();
    }

    /**
     * 生成指定名称的 MythicMob 怪物.
     *
     * @param player 玩家对象，执行命令的玩家
     * @param mobName 要生成的怪物名称
     */
    // 生成怪物的方法
    public void spawnMob(Player player, String mobName) {
        Location location = player.getLocation(); // 在玩家当前位置生成怪物

        // 检查 MythicMob 是否存在
        if (mythicBukkit.getMobManager().getMythicMob(mobName).isPresent()) {
            // 使用 MythicMobs 的方法生成怪物
            ActiveMob spawnedMob = mythicBukkit.getMobManager().spawnMob(
                    mobName,
                    location, // 可能直接使用 Bukkit 的 Location
                    1.0 // 生成的速度或其他参数
            );

            if (spawnedMob != null) {
                player.sendMessage("成功生成 " + mobName + "!");
            } else {
                player.sendMessage("生成 " + mobName + " 失败!");
            }
        } else {
            player.sendMessage("未找到怪物类型: " + mobName);
        }
    }
}
