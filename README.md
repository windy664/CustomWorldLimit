<div align="center">

<img src="assets/banner.svg" alt="CustomWorldLimit" width="100%">

# CustomWorldLimit

**MySQL 驱动的世界方块放置限制** —— 按世界统计玩家放置行为，超限拦截，数据集中存库。

![Platform](https://img.shields.io/badge/Platform-Spigot%20%2F%20Mohist-62bda8?style=flat-square&logo=minecraft&logoColor=white)
![Minecraft](https://img.shields.io/badge/MC-1.20.x-8a9a5b?style=flat-square)
![Java](https://img.shields.io/badge/Java-17-f89820?style=flat-square&logo=openjdk&logoColor=white)
![Storage](https://img.shields.io/badge/Storage-MySQL-4479a1?style=flat-square&logo=mysql&logoColor=white)

[功能](#-功能) · [配置](#️-配置) · [构建](#-构建)

</div>

## ✨ 功能

- **按世界限制方块放置**：`BlockPlaceListener` 拦截指定世界的放置行为，规则由配置与数据库共同决定
- **MySQL 持久化**：玩家放置记录写入 MySQL（`MySQL.java`），跨重启保留，适合多服共享一套限制数据
- **面向混合端**：构建时引入 Mohist 1.20.1 开发依赖，兼容 Mod + 插件混合环境

## ⚙️ 配置

`config.yml`：

```yaml
mySQLHost: 127.0.0.1
mySQLPort: 3306
mySQLUsername: world_block_limit
mySQLPassword: "********"
mySQLDatabase: world_block_limit
debug: false
```

## 🔨 构建

```bash
./gradlew build
```

> 数据库账号密码请自行修改，不要使用示例值上生产。
