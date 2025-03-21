package top.brmc.ampura16.mobarena.command;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import top.brmc.ampura16.mobarena.Main;

import java.io.File;

/**
 * 处理生成怪物的命令.
 */
public class SpawnMobCommand {

    private final Main plugin;
    private final boolean enableShowEnemyDisplayName;

    /**
     * 构造函数，初始化 CommandSpawnMob 实例.
     *
     * @param plugin 主插件实例，提供插件的配置和数据文件
     */
    public SpawnMobCommand(Main plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.enableShowEnemyDisplayName = config.getBoolean("enable-show-enemy-display-name", false); // 默认值为 false
    }

    /**
     * 生成指定名称的怪物.
     *
     * @param player 玩家对象，用于接收消息和生成怪物
     * @param mobName 要生成的怪物名称
     */
    public void spawnMob(Player player, String mobName) {
        // 创建配置文件的路径
        File mobsFolder = new File(plugin.getDataFolder(), "Mobs");
        File mobConfigFile = new File(mobsFolder, mobName + ".yml");

        // 检查配置文件是否存在
        if (!mobConfigFile.exists()) {
            player.sendMessage(ChatColor.RED + "找不到该怪物配置文件: " + mobName + ".yml");
            return;
        }

        // 加载配置文件
        FileConfiguration mobConfig = YamlConfiguration.loadConfiguration(mobConfigFile);

        // 使用 mobName 作为根节点名来获取属性
        String name = ChatColor.translateAlternateColorCodes('&', mobConfig.getString(mobName + ".name")); // 转换颜色代码
        String type = mobConfig.getString(mobName + ".type");
        double health = mobConfig.getDouble(mobName + ".health");

        // 确保类型字符串有效
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(type);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "无法识别的怪物类型: " + type);
            return;
        }

        // 生成怪物
        LivingEntity mob = (LivingEntity) player.getWorld().spawnEntity(player.getLocation(), entityType);
        // 根据配置决定是否显示名称
        if (enableShowEnemyDisplayName) {
            mob.setCustomName(name);
            mob.setCustomNameVisible(true); // 显示名称
        } else {
            mob.setCustomNameVisible(false); // 不显示名称
        }
        // 设置血量
        mob.setHealth(health);

        // 设置装备
        if (mobConfig.contains(mobName + ".equipments")) {
            ConfigurationSection equipments = mobConfig.getConfigurationSection(mobName + ".equipments");
            if (equipments != null) {
                // 处理装备
                setMobEquipment(mob, equipments);
            }
        }

        player.sendMessage(ChatColor.GREEN + "已生成怪物: " + name);
    }

    /**
     * 设置怪物的装备.
     *
     * @param mob 怪物对象
     * @param equipments 装备配置节
     */
    private void setMobEquipment(LivingEntity mob, ConfigurationSection equipments) {
        // 处理手上的物品
        if (equipments.contains("hand")) {
            String handItem = equipments.getString("hand");
            if (handItem != null) {
                setItemInHand(mob, handItem);
            }
        }
        // 处理头盔
        if (equipments.contains("head")) {
            String headItem = equipments.getString("head");
            if (headItem != null) {
                setHelmet(mob, headItem);
            }
        }
        // 处理胸甲
        if (equipments.contains("chestplate")) {
            String chestplateItem = equipments.getString("chestplate");
            if (chestplateItem != null) {
                setChestplate(mob, chestplateItem);
            }
        }
        // 处理护腿
        if (equipments.contains("leggings")) {
            String leggingsItem = equipments.getString("leggings");
            if (leggingsItem != null) {
                setLeggings(mob, leggingsItem);
            }
        }
        // 处理靴子
        if (equipments.contains("boots")) {
            String bootsItem = equipments.getString("boots");
            if (bootsItem != null) {
                setBoots(mob, bootsItem);
            }
        }
    }

    private void setItemInHand(LivingEntity mob, String item) {
        Material material = Material.getMaterial(item.toUpperCase());
        if (material != null) {
            ItemStack hand = new ItemStack(material);
            mob.getEquipment().setItemInMainHand(hand);
        } else {
            System.out.println("无法识别的物品类型: " + item);
        }
    }

    private void setHelmet(LivingEntity mob, String item) {
        Material material = Material.getMaterial(item.toUpperCase());
        if (material != null) {
            ItemStack head = new ItemStack(material);
            mob.getEquipment().setHelmet(head);
        } else {
            System.out.println("无法识别的物品类型: " + item);
        }
    }

    private void setChestplate(LivingEntity mob, String item) {
        Material material = Material.getMaterial(item.toUpperCase());
        if (material != null) {
            ItemStack chestplate = new ItemStack(material);
            mob.getEquipment().setChestplate(chestplate);
        } else {
            System.out.println("无法识别的物品类型: " + item);
        }
    }

    private void setLeggings(LivingEntity mob, String item) {
        Material material = Material.getMaterial(item.toUpperCase());
        if (material != null) {
            ItemStack leggings = new ItemStack(material);
            mob.getEquipment().setLeggings(leggings);
        } else {
            System.out.println("无法识别的物品类型: " + item);
        }
    }

    private void setBoots(LivingEntity mob, String item) {
        Material material = Material.getMaterial(item.toUpperCase());
        if (material != null) {
            ItemStack boots = new ItemStack(material);
            mob.getEquipment().setBoots(boots);
        } else {
            System.out.println("无法识别的物品类型: " + item);
        }
    }
}
