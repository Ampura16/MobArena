package top.blug.mobarena.mobarena.arena;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Arena {
    private final String mapName;
    private final String displayName;
    private final int minPlayer;
    private final int maxPlayer;
    private final String material;
    private final List<String> lore;

    public Arena(String mapName, String displayName, String material, int minPlayer, int maxPlayer, List<String> lore) {
        this.mapName = mapName;
        this.displayName = displayName;
        this.minPlayer = minPlayer;
        this.maxPlayer = maxPlayer;
        this.material = material;
        this.lore = lore != null ? lore : new ArrayList<>();
    }

    public static List<Arena> loadArenasFromConfig(FileConfiguration config) {
        List<Arena> arenaList = new ArrayList<>();

        for (String mapName : config.getKeys(false)) {
            try {
                // 去除 mapName 的前后空格和冒号
                mapName = mapName.trim();
                if (mapName.startsWith(":")) {
                    mapName = mapName.substring(1).trim();
                }

                // 从配置文件中读取属性
                String displayName = config.getString(mapName + ".display-name", mapName);
                int minPlayer = config.getInt(mapName + ".min-player", 1);
                int maxPlayer = config.getInt(mapName + ".max-player", 10);
                String material = config.getString(mapName + ".material", "BRICKS");
                List<String> lore = config.getStringList(mapName + ".arena-lore");

                // 验证配置合法性
                if (minPlayer > maxPlayer) {
                    System.err.println("Invalid player configuration for " + mapName + ": minPlayer > maxPlayer");
                    continue;
                }

                // 创建 Arena 对象并添加到列表中
                Arena arena = new Arena(mapName, displayName, material, minPlayer, maxPlayer, lore);
                arenaList.add(arena);
                System.out.println("Loaded arena: " + arena.getName() + " with display-name: " + displayName);

            } catch (Exception e) {
                // 捕获并输出异常信息
                System.err.println("Error loading arena configuration for " + mapName + ": " + e.getMessage());
            }
        }

        return arenaList;
    }

    public String getName() {
        return mapName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMaterial() {
        return material;
    }

    public int getMinPlayer() {
        return minPlayer;
    }

    public int getMaxPlayer() {
        return maxPlayer;
    }

    public List<String> getLore() {
        return lore;
    }

    // 工具方法:清理地图名称
    private String fixMapName(String mapName) {
        if (mapName == null) return "";
        mapName = mapName.trim();  // 去掉前后空格
        if (mapName.startsWith(":")) {
            mapName = mapName.substring(1).trim();  // 去掉冒号
        }
        return mapName;
    }

    @Override
    public String toString() {
        return "Arena{" +
                "mapName='" + mapName + '\'' +
                ", displayName='" + displayName + '\'' +
                ", minPlayer=" + minPlayer +
                ", maxPlayer=" + maxPlayer +
                ", material='" + material + '\'' +
                ", lore=" + lore +
                '}';
    }
}
