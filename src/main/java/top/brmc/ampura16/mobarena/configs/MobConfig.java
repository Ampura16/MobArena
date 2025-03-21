package top.brmc.ampura16.mobarena.configs;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * 表示一个怪物的配置，包含怪物的属性和装备信息.
 */
public class MobConfig {
    private String name;
    private String type;
    private double health;
    private boolean isBoss;
    private boolean isBaby;
    private ItemStack handEquipment;
    private ItemStack headItem;
    private String headSkin;
    private ItemStack chestplateEquipment;
    private ItemStack leggingsEquipment;
    private ItemStack bootsEquipment;
    private double moveSpeed;
    private double killCoin;

    /**
     * 构造函数，使用配置文件和怪物关键字初始化 MobConfig 实例.
     *
     * @param config 配置文件
     * @param mobKey 怪物的配置键
     */
    public MobConfig(FileConfiguration config, String mobKey) {
        this.name = translateColorCodes(config.getString(mobKey + ".name"));
        this.type = config.getString(mobKey + ".type");
        this.health = config.getDouble(mobKey + ".health");
        this.isBoss = config.getBoolean(mobKey + ".isBoss", false);
        this.isBaby = config.getBoolean(mobKey + ".isBaby", false);

        this.handEquipment = createEquipment(config.getString(mobKey + ".equipments.hand")); // 使用 String 方法

        // 处理 head 装备项
        ConfigurationSection headSection = config.getConfigurationSection(mobKey + ".equipments.head");
        if (headSection != null) {
            String headItemString = headSection.getString("item", "AIR");
            this.headSkin = headSection.getString("skin", null); // 获取玩家名
            this.headItem = createHeadItem(headItemString, this.headSkin);
        } else {
            this.headItem = new ItemStack(Material.AIR);
            this.headSkin = null;
        }

        // 处理其他装备
        this.chestplateEquipment = createEquipment(config.getConfigurationSection(mobKey + ".equipments.chestplate"));
        this.leggingsEquipment = createEquipment(config.getConfigurationSection(mobKey + ".equipments.leggings"));
        this.bootsEquipment = createEquipment(config.getConfigurationSection(mobKey + ".equipments.boots"));

        this.moveSpeed = config.getDouble(mobKey + ".moveSpeed", 0.2);
        this.killCoin = config.getDouble(mobKey + ".killCoin");
    }

    /**
     * 创建头部装备项.
     *
     * @param itemType 装备类型
     * @param skin 玩家名称，用于设置头颅皮肤
     * @return 创建的头部装备项
     */
    private ItemStack createHeadItem(String itemType, String skin) {
        if ("PLAYER_HEAD".equals(itemType) && skin != null) {
            if (skin.length() > 16) {
                skin = skin.substring(0, 16); // 截断名称
                Bukkit.getLogger().warning("玩家头颅名称 '" + skin + "' 超过 16 个字符,已被截断.");
            }
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            if (skullMeta != null) {
                // 设置头颅拥有者
                OfflinePlayer player = Bukkit.getOfflinePlayer(skin);
                skullMeta.setOwningPlayer(player);
                head.setItemMeta(skullMeta);
            }
            return head;
        } else {
            return createEquipment(itemType);
        }
    }

    /**
     * 创建装备项.
     *
     * @param section 装备的配置节
     * @return 创建的装备项
     */
    private ItemStack createEquipment(ConfigurationSection section) {
        if (section != null) {
            String itemType = section.getString("item", "AIR");
            Material material = Material.getMaterial(itemType);

            if (material != null) {
                ItemStack itemStack = new ItemStack(material);

                // 如果是皮革装备，则应用颜色
                if (material == Material.LEATHER_CHESTPLATE ||
                        material == Material.LEATHER_LEGGINGS ||
                        material == Material.LEATHER_BOOTS) {

                    String colorHex = section.getString("color", null);
                    if (colorHex != null) {
                        try {
                            LeatherArmorMeta armorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
                            armorMeta.setColor(Color.fromRGB(Integer.parseInt(colorHex, 16)));
                            itemStack.setItemMeta(armorMeta);
                        } catch (NumberFormatException e) {
                            Bukkit.getLogger().warning("颜色 '" + colorHex + "' 不是有效的十六进制值。");
                        }
                    }
                }
                return itemStack;
            } else {
                Bukkit.getLogger().warning("无法识别的物品类型: " + itemType);
            }
        }
        return new ItemStack(Material.AIR); // 返回默认值
    }

    /**
     * 根据物品类型创建装备项.
     *
     * @param itemType 物品类型字符串
     * @return 创建的装备项
     */
    private ItemStack createEquipment(String itemType) {
        if (itemType != null && !itemType.equalsIgnoreCase("null")) { // 处理 null 和字符串 "null"
            Material material = Material.getMaterial(itemType);
            if (material != null) {
                return new ItemStack(material);
            }
        }
        return new ItemStack(Material.AIR); // 返回一个空气物品作为默认值
    }

    /**
     * 将颜色代码转换为实际颜色.
     *
     * @param nameText 包含颜色代码的文本
     * @return 转换后的文本
     */
    private String translateColorCodes(String nameText) {
        return ChatColor.translateAlternateColorCodes('&', nameText);
    }

    // Getter 方法
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getHealth() {
        return health;
    }

    public boolean isBoss() {
        return isBoss;
    }

    public boolean isBaby() {
        return isBaby;
    }

    public ItemStack getHandEquipment() {
        return handEquipment; // 更新为 ItemStack
    }

    public ItemStack getHeadItem() {
        return headItem; // 更新为 ItemStack
    }

    public String getHeadSkin() {
        return headSkin;
    }

    public ItemStack getChestplateEquipment() {
        return chestplateEquipment;
    }

    public ItemStack getLeggingsEquipment() {
        return leggingsEquipment;
    }

    public ItemStack getBootsEquipment() {
        return bootsEquipment;
    }

    public double getMoveSpeed() {
        return moveSpeed;
    }

    public double getKillCoin() {
        return killCoin;
    }
}
