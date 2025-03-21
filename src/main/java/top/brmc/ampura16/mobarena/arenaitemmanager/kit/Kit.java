package top.brmc.ampura16.mobarena.arenaitemmanager.kit;

import java.util.List;

/**
 * 该类表示游戏中的一个职业包（Kit）.
 */
public class Kit {
    private String kitDisplayName; // 职业包显示名称
    private String kitMaterial; // 职业包材料类型
    private List<String> lore; // 职业包描述
    private String kitShop; // 职业包商店信息
    private List<String> kitInventory; // 职业包物品列表

    /**
     * 构造一个新的 Kit 实例
     * @param kitDisplayName 职业包的显示名称
     * @param kitMaterial 职业包的材料类型
     * @param lore 职业包的描述
     * @param kitShop 职业包的商店信息
     * @param kitInventory 职业包的物品列表
     */
    public Kit(String kitDisplayName, String kitMaterial, List<String> lore, String kitShop, List<String> kitInventory) {
        this.kitDisplayName = kitDisplayName; // 初始化职业包显示名称
        this.kitMaterial = kitMaterial; // 初始化职业包材料类型
        this.lore = lore; // 初始化职业包描述
        this.kitShop = kitShop; // 初始化职业包商店信息
        this.kitInventory = kitInventory; // 初始化职业包物品列表
    }

    /**
     * 获取职业包的显示名称
     * @return 职业包显示名称
     */
    public String getKitDisplayName() {
        return kitDisplayName;
    }

    /**
     * 获取职业包的材料类型
     * @return 职业包材料类型
     */
    public String getKitMaterial() {
        return kitMaterial;
    }

    /**
     * 获取职业包的描述
     * @return 职业包描述列表
     */
    public List<String> getLore() {
        return lore;
    }

    /**
     * 获取职业包的商店信息
     * @return 职业包商店信息
     */
    public String getKitShop() {
        return kitShop;
    }

    /**
     * 获取职业包的物品列表
     * @return 职业包物品列表
     */
    public List<String> getKitInventory() {
        return kitInventory;
    }
}
