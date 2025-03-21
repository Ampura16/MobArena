package top.brmc.ampura16.mobarena.configs;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 处理各种README文件的创建和加载
 * */
public record READMEConfig(JavaPlugin plugin) {

    /**
     * 在插件文件夹根目录内创建 README.txt 文件并写入内容.
     *
     * @param folder 目标文件夹
     */
    public void createReadMeFile(File folder) {
        if (!folder.exists()) {
            folder.mkdirs(); // 如果文件夹不存在，则创建
        }

        File readMeFile = new File(folder, "README.txt"); // 定义 README.txt 文件路径
        if (!readMeFile.exists()) {
            try {
                readMeFile.createNewFile(); // 创建文件
                try (BufferedWriter readmeWriter = new BufferedWriter(new FileWriter(readMeFile))) {
                    // 写入内容
                    readmeWriter.write("「                                                                                                  \n");
                    readmeWriter.write("                MobArena - 怪物竞技场                \n");
                    readmeWriter.write("              Platform: Spigot-1.20.1              \n");
                    readmeWriter.write("       Powered by Ampura16 @BlockLand Studio       \n");
                    readmeWriter.write("                                                                                                  」\n");
                    readmeWriter.write("\n");
                    readmeWriter.write("该文件为插件使用时需要了解的一些事项\n");
                    readmeWriter.write("\n");
                    readmeWriter.write("# 地图设置向导:\n");
                    readmeWriter.write("# 该向导也会在创建地图时自动提示,但是可能受到需要重启服务器的影响,无法在后续设置过程中提示\n");
                    readmeWriter.write("Step 1: 使用 /ma admin setwaitloc <地图名称> 设置地图等待大厅位置.\n");
                    readmeWriter.write("Step 2: 使用 /ma admin setstartloc <地图名称> 设置游戏开始传送位置.\n");
                    readmeWriter.write("Step 3: 使用 /stop 彻底关闭服务器并重启. [!]一定要在这时操作,不要弄乱设置顺序.\n");
                    readmeWriter.write("Step 4: 使用 /ma admin setmobspawnloc <地图名称> 设置任意数量的怪物生成位置.\n");
                    readmeWriter.write("Step 5: 在 MobArena/arenas.yml 中该地图配置下面添加回合怪物设置,格式如下:\n");
                    readmeWriter.write("\n");
                    readmeWriter.write("<地图名称>:\n");
                    readmeWriter.write("  其他设置: xxx\n");
                    readmeWriter.write("  ...\n");
                    readmeWriter.write("  gamerounds:\n");
                    readmeWriter.write("    1:\n");
                    readmeWriter.write("      mm怪物: int\n");
                    readmeWriter.write("      mm怪物: int\n");
                    readmeWriter.write("      ...\n");
                    readmeWriter.write("    2:\n");
                    readmeWriter.write("      mm怪物: int\n");
                    readmeWriter.write("      mm怪物: int\n");
                    readmeWriter.write("      ...\n");
                    readmeWriter.write("    3:\n");
                    readmeWriter.write("      mm怪物: int\n");
                    readmeWriter.write("      mm怪物: int\n");
                    readmeWriter.write("      ...\n");
                    readmeWriter.write("    更多回合...\n");
                    readmeWriter.write("\n");
                    readmeWriter.write("Step 6: 使用 /stop 彻底关闭服务器并重启.\n");
                    readmeWriter.write("Step 7: 设置完成.现在可以使用 /ma gui 指令或者钻石队列道具选择并加入游戏.\n");
                    readmeWriter.write("Notice: 插件不支持一端多图,即一个服务器只能创建一个地图.\n");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("无法创建 README.txt 文件: " + e.getMessage());
            }
        }
    }

    /**
     * 创建 Mobs/README.txt 文件并写入内容.
     *
     * @param mobsFolder 存储怪物配置的文件夹
     */
    public void createMobsConfigReadMeFile(File mobsFolder) {
        if (!mobsFolder.exists()) {
            mobsFolder.mkdirs(); // 如果文件夹不存在，则创建
        }

        File readMeFile = new File(mobsFolder, "README.txt");
        if (!readMeFile.exists()) {
            try {
                readMeFile.createNewFile(); // 创建README.txt文件
                try (BufferedWriter readmeWriter = new BufferedWriter(new FileWriter(readMeFile))) {
                    readmeWriter.write("===================================================\n");
                    readmeWriter.write("[ ! ] 插件原生怪物已停止支持,具体怪物设置方法见地图设置向导!\n");
                    readmeWriter.write("===================================================\n");
                    readmeWriter.write("该文件夹存储怪物配置,每个YAML配置文件单独存储一个怪物的配置.\n");
                    readmeWriter.write("为了避免插件识别错误，并且区别于Git代码阅读文档规范,此README文件格式为txt文本文档.\n");
                    readmeWriter.write("YAML文件的命名即为插件识别该怪物的命名.\n");
                    readmeWriter.write("配置文件格式示例:\n");
                    readmeWriter.write("===================================================\n");
                    readmeWriter.write("怪物识别名称，一级父键\n");
                    readmeWriter.write("NormalZombie:\n");
                    readmeWriter.write("  怪物显示名称，如果config.yml设置显示为true则会显示\n");
                    readmeWriter.write("  name: \"&r普通僵尸\"\n");
                    readmeWriter.write("  怪物种类，全大写\n");
                    readmeWriter.write("  type: ZOMBIE\n");
                    readmeWriter.write("  怪物生命值\n");
                    readmeWriter.write("  health: 20.0\n");
                    readmeWriter.write("  怪物装备\n");
                    readmeWriter.write("  equipments:\n");
                    readmeWriter.write("    手持\n");
                    readmeWriter.write("    hand: null\n");
                    readmeWriter.write("    头盔\n");
                    readmeWriter.write("    head: LEATHER_HELMET\n");
                    readmeWriter.write("    胸甲\n");
                    readmeWriter.write("    chestplate: null\n");
                    readmeWriter.write("    护腿\n");
                    readmeWriter.write("    leggings: null\n");
                    readmeWriter.write("    靴子\n");
                    readmeWriter.write("    boots: null\n");
                    readmeWriter.write("  击杀获得金币\n");
                    readmeWriter.write("  killCoin: 10.0\n");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("无法创建 README.txt 文件: " + e.getMessage());
            }
        }
    }
}
