======================================================================
该文件夹存储怪物配置，每个yml配置文件单独存储一个怪物的配置
为了避免插件识别错误，并且区别于Git代码阅读文档规范，此README文件格式为txt文本文档
yml文件的命名即为插件识别该怪物的命名
配置文件格式示例:
======================================================================

怪物识别名称，一级父键
NormalZombie:
  怪物显示名称，如果config.yml设置显示为true则会显示
  name: "&r普通僵尸"
  怪物种类，全大写
  type: ZOMBIE
  怪物生命值
  health: 20.0
  怪物装备
  equipments:
    手持
    hand: null
    头盔
    head: LEATHER_HELMET
    胸甲
    chestplate: null
    护腿
    leggings: null
    靴子
    boots: null
  击杀获得金币
  killCoin: 10.0