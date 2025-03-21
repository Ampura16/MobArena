package top.brmc.ampura16.mobarena.prearena;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SelectMapGUI {

    private final List<Arena> arenaList;
    private String guiTitle;

    public SelectMapGUI(List<Arena> arenaList, FileConfiguration config) {
        this.arenaList = arenaList;
        this.guiTitle = ChatColor.translateAlternateColorCodes('&', config.getString("queue-item-settings.select-map.gui-title", "&2选择地图"));
    }

    public void updateTitle(FileConfiguration config) {
        this.guiTitle = ChatColor.translateAlternateColorCodes('&', config.getString("queue-item-settings.select-map.gui-title", "&b选择地图"));
    }

    public void openSelectMapGUI(Player player) {
        int size = ((arenaList.size() - 1) / 9 + 1) * 9; // 动态调整大小
        Inventory inventory = Bukkit.createInventory(null, size, guiTitle);

        for (Arena arena : arenaList) {
            ItemStack mapItem = createMapItem(arena);
            inventory.addItem(mapItem);
        }

        player.openInventory(inventory);
    }

    private ItemStack createMapItem(Arena arena) {
        Material material = getValidMaterial(arena.getMaterial(), Material.BRICKS);
        ItemStack mapIcon = new ItemStack(material);
        ItemMeta meta = mapIcon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', arena.getDisplayName()));

            List<String> lore = new ArrayList<>();
            for (String loreLine : arena.getLore()) {
                // 转换颜色代码
                lore.add(ChatColor.translateAlternateColorCodes('&', loreLine));
            }

            // 添加调试日志，检查生成的内容
            String sanitizedMapName = fixMapName(arena.getName());
            // System.out.println("Creating map item with Map ID: " + sanitizedMapName);

            // 确保生成的地图名称无多余字符
            lore.add(ChatColor.DARK_GRAY + "Map ID: " + sanitizedMapName);
            // 这里的Map ID 是插件内部标识符NamespacedKey
            // 他的颜色是DARK_GRAY，不要试图修改他的显示颜色，可能需要涉及修改很多队列处理类代码
            // 这很麻烦 (危
            lore.add(ChatColor.DARK_GREEN + "最小玩家数: " + ChatColor.AQUA + arena.getMinPlayer());
            lore.add(ChatColor.DARK_GREEN + "最大玩家数: " + ChatColor.AQUA + arena.getMaxPlayer());
            lore.add(ChatColor.GREEN + "点击加入该地图队列");

            meta.setLore(lore);
            mapIcon.setItemMeta(meta);
        }
        return mapIcon;
    }

    // 工具方法
    private String fixMapName(String mapName) {
        if (mapName == null) return "";
        mapName = mapName.trim();  // 去掉前后空格
        if (mapName.startsWith(":")) {
            mapName = mapName.substring(1).trim();  // 去掉冒号
        }
        return mapName;
    }


    private Material getValidMaterial(String materialName, Material defaultMaterial) {
        Material material = Material.matchMaterial(materialName.toUpperCase());
        return (material != null) ? material : defaultMaterial;
    }

    public void updateMapList(Player player) {
        if (player.getOpenInventory().getTitle().equals(guiTitle)) {
            openSelectMapGUI(player);
        }
    }
}

