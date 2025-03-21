package top.brmc.ampura16.mobarena.prearena;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MAQueueUtils {

    private final JavaPlugin plugin;
    private Map<String, QueueItemConfig> queueItems;

    public MAQueueUtils(JavaPlugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin 不能为空..");
        }
        this.plugin = plugin;
        this.queueItems = loadQueueItems(plugin.getConfig());
    }


    // 重新加载队列物品配置
    public void reloadQueueSettings() {
        this.queueItems = loadQueueItems(plugin.getConfig());
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "队列物品配置已重载.");
    }

    private Map<String, QueueItemConfig> loadQueueItems(FileConfiguration config) {
        Map<String, QueueItemConfig> queueItems = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("queue-item-settings");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String name = ChatColor.translateAlternateColorCodes('&', section.getString(key + ".name", ""));
                String materialName = section.getString(key + ".material", "BARRIER");
                Material material = Material.matchMaterial(materialName.toUpperCase());
                if (material == null) {
                    material = Material.BARRIER; // 默认材质，防止错误
                }
                int slot = section.getInt(key + ".slot", -1);
                List<String> lore = section.getStringList(key + ".lore");
                lore.replaceAll(line -> ChatColor.translateAlternateColorCodes('&', line));

                QueueItemConfig itemConfig = new QueueItemConfig(material, name, slot, lore);
                queueItems.put(key, itemConfig);
            }
        }
        return queueItems;
    }

    public void giveQueueItems(Player player) {
        queueItems.forEach((key, itemConfig) -> {
            ItemStack item = new ItemStack(itemConfig.getMaterial());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(itemConfig.getName());
                meta.setLore(itemConfig.getLore());
                PersistentDataContainer container = meta.getPersistentDataContainer();

                // 使用安全的 NamespacedKey 创建方法
                NamespacedKey queueItemKey = createSafeNamespacedKey("queue_item");
                if (queueItemKey != null) {
                    container.set(queueItemKey, PersistentDataType.STRING, key);
                }
                // 为商店道具设置一个不同的键
                if (key.equals("shop")) {
                    NamespacedKey shopItemKey = createSafeNamespacedKey("shop_item");
                    if (shopItemKey != null) {
                        container.set(shopItemKey, PersistentDataType.STRING, "shop");
                    }
                }

                item.setItemMeta(meta);
                player.getInventory().setItem(itemConfig.getSlot(), item);
            }
        });
    }

    public void removeQueueItems(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
                if (container.has(new NamespacedKey(plugin, "queue_item"), PersistentDataType.STRING)) {
                    player.getInventory().remove(item);
                }
            }
        }
    }

    /**
     * 清空队列道具，保留商店道具
     */
    public void clearQueueItems(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
                // 检查是否是队列道具
                if (container.has(new NamespacedKey(plugin, "queue_item"), PersistentDataType.STRING)) {
                    // 检查是否是商店道具
                    if (!container.has(new NamespacedKey(plugin, "shop_item"), PersistentDataType.STRING)) {
                        player.getInventory().remove(item); // 移除队列道具
                    }
                }
            }
        }
        // player.sendMessage(ChatColor.GREEN + "你的队列道具已被清空，商店道具已保留.");
    }

    /**
     * 安全地创建 NamespacedKey，并捕获可能的异常
     *
     * @param key 名称
     * @return 创建成功的 NamespacedKey 或 null
     */
    private NamespacedKey createSafeNamespacedKey(String key) {
        try {
            return new NamespacedKey(plugin, key);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("创建 NamespacedKey '" + key + "' 时发生错误: " + e.getMessage());
        } catch (Exception e) {
            plugin.getLogger().severe("未知错误导致 NamespacedKey '" + key + "' 创建失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // 返回 null 表示创建失败
    }

    private static class QueueItemConfig {
        private final Material material;
        private final String name;
        private final int slot;
        private final List<String> lore;

        public QueueItemConfig(Material material, String name, int slot, List<String> lore) {
            this.material = material;
            this.name = name;
            this.slot = slot;
            this.lore = lore;
        }

        public Material getMaterial() {
            return material;
        }

        public String getName() {
            return name;
        }

        public int getSlot() {
            return slot;
        }

        public List<String> getLore() {
            return lore;
        }
    }
}
