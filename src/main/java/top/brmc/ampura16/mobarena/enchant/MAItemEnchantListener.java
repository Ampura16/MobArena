package top.brmc.ampura16.mobarena.enchant;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * 处理物品附魔的事件监听器，允许在铁砧中根据附魔书附加附魔到物品上。
 */
public class MAItemEnchantListener implements Listener {
    private final JavaPlugin plugin;
    private String pluginPrefix;
    private final Map<String, Integer> maxEnchantLevels = new HashMap<>();

    /**
     * 构造函数，初始化 MAItemEnchantListener 实例并加载附魔设置.
     *
     * @param plugin      插件实例
     * @param pluginPrefix 插件前缀，用于日志输出
     */
    public MAItemEnchantListener(JavaPlugin plugin, String pluginPrefix) {
        this.plugin = plugin;
        this.pluginPrefix = pluginPrefix;
        loadEnchantSettings();
    }

    /**
     * 加载附魔设置，从配置文件中读取最大附魔等级.
     */
    private void loadEnchantSettings() {
        FileConfiguration config = plugin.getConfig();
        if (config.contains("enchant-settings")) {
            for (String enchant : config.getConfigurationSection("enchant-settings").getKeys(false)) {
                int maxLevel = config.getInt("enchant-settings." + enchant + ".max-level");
                maxEnchantLevels.put(enchant, maxLevel);
                Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.DARK_GREEN + " 已加载附魔: " + enchant + ChatColor.GREEN + " 最大等级: " + maxLevel);
            }
        } else {
            plugin.getLogger().warning("配置中未找到附魔设置.");
        }
    }

    /**
     * 处理铁砧准备事件，在物品和附魔书之间进行附魔合并.
     *
     * @param event 铁砧预合成事件,在铁砧输入栏中放入物品时触发.
     */
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory anvil = event.getInventory();
        ItemStack item = anvil.getItem(0); // 主物品
        ItemStack enchantBook = anvil.getItem(1); // 附魔书

        plugin.getLogger().info("准备铁砧，物品: " + item + " 附魔书: " + enchantBook);

        if (item != null && enchantBook != null && enchantBook.getType() == Material.ENCHANTED_BOOK) {
            if (enchantBook.hasItemMeta() && enchantBook.getItemMeta() instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) enchantBook.getItemMeta();
                Map<Enchantment, Integer> enchantments = meta.getStoredEnchants();

                plugin.getLogger().info("附魔书上的附魔: " + enchantments);

                if (!enchantments.isEmpty()) {
                    Map.Entry<Enchantment, Integer> entry = enchantments.entrySet().iterator().next();
                    Enchantment enchantment = entry.getKey();
                    int bookLevel = entry.getValue();

                    plugin.getLogger().info("附魔: " + enchantment.getKey().getKey() + " 等级: " + bookLevel);

                    int currentLevel = item.getEnchantments().getOrDefault(enchantment, 0);
                    plugin.getLogger().info("当前物品的附魔等级: " + currentLevel);

                    // 计算更新后的附魔等级，并从配置中获取最大等级
                    int updatedLevel = currentLevel + bookLevel;
                    int maxLevel = maxEnchantLevels.getOrDefault(enchantment.getKey().getKey(), Integer.MAX_VALUE);

                    plugin.getLogger().info("最大等级: " + maxLevel);

                    // 无需限制等级，只需输出调试信息
                    if (updatedLevel > maxLevel) {
                        plugin.getLogger().warning("更新后的附魔等级超出最大等级,设定为最高等级: " + maxLevel);
                    }

                    updatedLevel = Math.min(updatedLevel, maxLevel); // 保证不超过最大等级

                    plugin.getLogger().info("更新后的附魔等级: " + updatedLevel);

                    // 创建新的物品并应用新的附魔等级
                    ItemStack newItem = item.clone();
                    newItem.removeEnchantment(enchantment); // 移除原有附魔

                    try {
                        newItem.addEnchantment(enchantment, updatedLevel); // 添加新等级的附魔
                    } catch (IllegalArgumentException e) {
                        // 处理添加附魔时的异常
                        plugin.getLogger().severe("尝试添加附魔时出错: " + e.getMessage());
                    }

                    plugin.getLogger().info("新物品: " + newItem + " 附魔等级: " + updatedLevel);
                    event.setResult(newItem);
                } else {
                    plugin.getLogger().info("附魔书没有附魔.");
                    event.setResult(null);
                }
            } else {
                plugin.getLogger().info("附魔书的元数据不正确.");
                event.setResult(null);
            }
        } else {
            plugin.getLogger().info("物品或附魔书为空,或附魔书类型不正确.");
            event.setResult(null);
        }
    }
}
