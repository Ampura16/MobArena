package top.brmc.ampura16.mobarena.prearena;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import top.brmc.ampura16.mobarena.arenaitemmanager.coinshop.MACoinShop;
import top.brmc.ampura16.mobarena.arenaitemmanager.kit.KitSelectGUI;

public class MAQueueListener implements Listener {

    private final JavaPlugin plugin;
    private final MapManager mapManager;
    private final MAQueueUtils queueUtils;
    private NamespacedKey kitItemKey;

    public MAQueueListener(JavaPlugin plugin, MapManager mapManager, MAQueueUtils queueUtils, NamespacedKey kitItemKey) {
        this.plugin = plugin;
        this.mapManager = mapManager;
        this.queueUtils = queueUtils;
        this.kitItemKey = kitItemKey;
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (isQueueItem(item)) {
            event.setCancelled(true);
            // event.getPlayer().sendMessage(ChatColor.RED + "你不能丢弃队列物品.");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();

        if (item != null && isQueueItem(item)) {
            event.setCancelled(true);
            // event.getWhoClicked().sendMessage(ChatColor.RED + "你不能修改队列物品.");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && isQueueItem(item)) {

            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            String key = container.get(new NamespacedKey(plugin, "queue_item"), PersistentDataType.STRING);

            if (key != null) {
                Player player = event.getPlayer();
                switch (key) {
                    case "select-map":
                        mapManager.openSelectMapGUI(player); // 打开选择地图的 GUI
                        event.setCancelled(true); // 取消事件以防止默认交互行为
                        break;
                    case "select-kit":
                        KitSelectGUI kitSelectGUI = new KitSelectGUI(plugin, mapManager.getKitManager(), kitItemKey); // 传递 kitItemKey
                        kitSelectGUI.open(player); // 打开职业选择 GUI
                        event.setCancelled(true);
                        break;
                    case "leave-game":
                        mapManager.leaveQueue(player);
                        event.setCancelled(true); // 取消事件以防止默认交互行为
                        break;
                    case "shop":
                        String feedback = MACoinShop.checkPlayerProfessionItems(player);
                        player.sendMessage(feedback); // 反馈职业物品信息
                        event.setCancelled(true);
                        break;
                    default:
                        // 处理未知的情况下
                        break;
                }
            }
        }
    }

    private boolean isQueueItem(ItemStack item) {
        if (item.hasItemMeta()) {
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            return container.has(new NamespacedKey(plugin, "queue_item"), PersistentDataType.STRING);
        }
        return false;
    }

}
