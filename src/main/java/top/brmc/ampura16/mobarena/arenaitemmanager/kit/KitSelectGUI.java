package top.brmc.ampura16.mobarena.arenaitemmanager.kit;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import top.brmc.ampura16.mobarena.arenaitemmanager.coinshop.MAShopCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 该类用于创建并管理职业选择 GUI
 */
public class KitSelectGUI implements Listener {

    private final JavaPlugin plugin; // 插件实例
    private final KitManager kitManager; // KitManager 实例
    private final NamespacedKey kitItemKey; // 用于存储职业数据的 NamespacedKey
    private static final String LOCK_LORE = "职业道具"; // 锁定物品的 Lore

    /**
     * 构造一个新的 KitSelectGUI 实例
     * @param plugin 插件实例
     * @param kitManager KitManager 实例
     * @param kitItemKey 用于存储职业数据的 NamespacedKey
     */
    public KitSelectGUI(JavaPlugin plugin, KitManager kitManager, NamespacedKey kitItemKey) {
        this.plugin = plugin;
        this.kitManager = kitManager; // 初始化 KitManager
        this.kitItemKey = kitItemKey;
    }

    /**
     * 打开职业选择 GUI
     * @param player 要打开 GUI 的玩家
     */
    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.AQUA + "选择职业"); // 创建一个 9 个格子的 GUI

        // 添加职业到 GUI
        for (Kit kit : kitManager.getAllKits().values()) {
            ItemStack kitItem = createKitItem(kit);
            gui.addItem(kitItem); // 将职业物品添加到 GUI
        }

        player.openInventory(gui); // 打开 GUI
    }

    /**
     * 创建职业物品的 ItemStack
     * @param kit 职业包信息
     * @return 创建的 ItemStack
     */
    private ItemStack createKitItem(Kit kit) {
        // 获取材料名称
        String materialName = kit.getKitMaterial();
        Material material = Material.getMaterial(materialName);

        if (material == null) {
            throw new IllegalArgumentException("无效的材料名称: " + materialName);
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            throw new NullPointerException("ItemMeta 不能为空");
        }

        meta.setDisplayName(ChatColor.GOLD + kit.getKitDisplayName());

        List<String> lore = new ArrayList<>();
        for (String line : kit.getLore()) {
            lore.add(ChatColor.GRAY + line);
        }
        meta.setLore(lore); // 设置 Lore

        // 使用 kitItemKey 设置标签
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(kitItemKey, PersistentDataType.STRING, kit.getKitDisplayName()); // 使用职业名称作为标签

        item.setItemMeta(meta); // 设置 ItemMeta
        return item;
    }

    /**
     * 处理玩家在职业选择 GUI 中的点击事件
     * @param event 点击事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.AQUA + "选择职业")) {
            event.setCancelled(true); // 取消点击事件，禁止物品移动

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null && clickedItem.hasItemMeta()) {
                String kitName = extractKitName(clickedItem); // 提取职业名
                Kit selectedKit = kitManager.getKit(kitName);
                if (selectedKit != null) {
                    giveKitItems(player, selectedKit); // 给玩家对应的职业物品
                    player.closeInventory(); // 关闭 GUI
                }
            }
        }
    }

    /**
     * 从 ItemStack 中提取职业名称
     * @param item 被点击的 ItemStack
     * @return 提取的职业名称
     */
    private String extractKitName(ItemStack item) {
        if (item.getItemMeta() == null) {
            return null; // 防止 NullPointerException
        }

        String clickedDisplayName = item.getItemMeta().getDisplayName();
        for (Map.Entry<String, Kit> entry : kitManager.getAllKits().entrySet()) {
            // 直接比较显示名称，不使用 ChatColor.GOLD
            if (clickedDisplayName.equals(entry.getValue().getKitDisplayName())) {
                return entry.getKey(); // 返回职业名称
            }
        }
        return null; // 未找到职业名称
    }

    /**
     * 给玩家对应的职业物品
     * @param player 玩家
     * @param selectedKit 选择的职业包
     */
    private void giveKitItems(Player player, Kit selectedKit) {
        // 首先清除之前的职业物品
        clearPreviousKitItems(player); // 只需调用而不传递 Lore 参数

        // 然后给玩家新的职业物品
        for (String invItem : selectedKit.getKitInventory()) {
            String[] parts = invItem.split(",");
            Material material = Material.getMaterial(parts[0]);
            int amount = Integer.parseInt(parts[1]);
            if (material != null) {
                ItemStack itemStack = new ItemStack(material, amount);
                ItemMeta meta = itemStack.getItemMeta(); // 获取 ItemMeta

                if (meta != null) {
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.LIGHT_PURPLE + "职业道具"); // 添加锁定的 Lore
                    meta.setLore(lore); // 设置 Lore

                    itemStack.setItemMeta(meta); // 将更新后的 ItemMeta 设置回 ItemStack
                }

                player.getInventory().addItem(itemStack); // 添加物品到玩家背包
            }
        }

        MAShopCategory.clearClassShopIdentifiers(player);
        MAShopCategory.giveClassShopIdentifier(player, selectedKit.getKitDisplayName());
        updatePlayerKitInfo(player, selectedKit); // 设置当前职业到玩家的 PersistentDataContainer
    }

    /**
     * 清除玩家之前的职业物品
     * @param player 玩家
     */
    public void clearPreviousKitItems(Player player) {
        clearPlayerEquipment(player);

        // 创建一个列表来存储需要移除的物品
        List<ItemStack> identifyKitItems = new ArrayList<>();
        String lockLore = "职业道具"; // 锁定的判定 Lore

        // 遍历玩家的物品，找出包含锁定 Lore 的物品
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                List<String> itemLore = meta.getLore();

                // 如果物品的 Lore 不为空，检查其中是否包含锁定的 Lore
                if (itemLore != null) {
                    for (String loreLine : itemLore) {
                        if (ChatColor.stripColor(loreLine).equals(lockLore)) {
                            identifyKitItems.add(item); // 添加到待移除列表
                            break; // 一旦找到匹配项，跳出 inner 循环
                        }
                    }
                }
            }
        }

        // 移除列表中的所有职业物品
        for (ItemStack item : identifyKitItems) {
            player.getInventory().remove(item); // 使用 remove 方法
        }
    }

    /**
     * 移除玩家的所有装备
     * @param player 要移除装备的玩家
     */
    public void clearPlayerEquipment(Player player) {
        if (player != null) {
            // 清空玩家的装备，设置为 null
            player.getInventory().setHelmet(null); // 移除头盔
            player.getInventory().setChestplate(null); // 移除胸甲
            player.getInventory().setLeggings(null); // 移除护腿
            player.getInventory().setBoots(null); // 移除靴子
        }
    }

    /**
     * 更新玩家的职业信息
     * @param player 玩家
     * @param selectedKit 选择的职业包
     */
    private void updatePlayerKitInfo(Player player, Kit selectedKit) {
        // 更新 PersistentDataContainer
        player.getPersistentDataContainer().set(new NamespacedKey(plugin, "currentKit"), PersistentDataType.STRING, selectedKit.getKitDisplayName());

        // 尝试获取并发送当前职业的消息
        String currentKitMessage = PlaceholderAPI.setPlaceholders(player, "%mobarena_currentkit%");
        player.sendMessage(ChatColor.GREEN + "你已获得职业: " + currentKitMessage);
    }
}
