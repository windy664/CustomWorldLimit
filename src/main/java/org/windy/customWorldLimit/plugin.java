package org.windy.customWorldLimit;

import com.mojang.logging.LogUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.windy.customWorldLimit.BlockPlaceListener;
import org.windy.customWorldLimit.MySQL;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class plugin extends JavaPlugin {

    private static final Logger LOGGER = LogUtils.getLogger();
    private MySQL mySQL;
    private Map<String, Integer> blockLimits;
    public Boolean DEBUG = true;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        String mySQLHost = getConfig().getString("mySQLHost", "127.0.0.1");
        String mySQLPort = getConfig().getString("mySQLPort", "3306");
        String mySQLUsername = getConfig().getString("mySQLUsername");
        String mySQLPassword = getConfig().getString("mySQLPassword");
        String mySQLDatabase = getConfig().getString("mySQLDatabase");
        DEBUG = getConfig().getBoolean("debug", false);
        mySQL = new MySQL(mySQLHost, mySQLPort, mySQLUsername, mySQLPassword, mySQLDatabase);

        try {
            mySQL.connect();  // 连接数据库
         //   mySQL.dropExistingTable();  // 先删除旧的表格
            mySQL.createTable();  // 创建表格

        } catch (SQLException e) {
            LOGGER.warn("无法连接到MySQL数据库！");
            LOGGER.error("连接MySQL时发生异常：", e);
        }

        // 加载 JSON 配置
        loadBlockLimits();
        registerForgeEvent(new BlockPlaceListener(mySQL, this));  // 注册监听器
    }

    @Override
    public void onDisable() {
        // 在关闭时可以关闭 MySQL 连接（视需要而定）
        try {
            mySQL.disconnect();
        } catch (SQLException e) {
            LOGGER.warn("无法断开与MySQL数据库的连接！");
        }
        unregisterAllForgeEvents();
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    // 从 JSON 文件加载方块数量限制
    private void loadBlockLimits() {
        File file = new File(getDataFolder(), "block_limits.json");

        // 确保文件存在
        if (!file.exists()) {
            saveResource("block_limits.json", false);  // 如果没有文件则使用默认
        }

        // 使用 Gson 解析 JSON 文件
        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            // 使用 TypeToken 来指明目标类型 Map<String, Integer>
            blockLimits = gson.fromJson(reader, new TypeToken<Map<String, Integer>>(){}.getType());
            LOGGER.info("已从 JSON 配置加载方块数量限制。");
        } catch (IOException e) {
            LOGGER.error("加载 JSON 配置文件时失败。", e);
        }
    }

    // 获取方块数量限制
    public Map<String, Integer> getBlockLimits() {
        return blockLimits;  // 直接返回从 JSON 加载的限制
    }

    // 获取方块数量限制，支持模糊匹配
    /*
    public int getBlockLimit(String blockType) {
        if (blockLimits != null) {
            // 模糊匹配，遍历配置中的所有方块类型
            for (Map.Entry<String, Integer> entry : blockLimits.entrySet()) {
                if (blockType.contains(entry.getKey())) {
                    return entry.getValue();  // 直接返回匹配的整数值
                }
            }
        }
        LOGGER.info("方块类型: {} 未找到匹配的配置，返回默认限制 -1", blockType);
        return -1;  // 如果没有找到匹配的方块类型，返回 -1
    }

     */
    public Map.Entry<String, Integer> getBlockLimit(String blockType) {
        if (blockLimits != null) {
            // 模糊匹配，遍历配置中的所有方块类型
            for (Map.Entry<String, Integer> entry : blockLimits.entrySet()) {
                if (blockType.contains(entry.getKey())) {
                    return entry;  // 返回匹配的方块类型和对应的限制值
                }
            }
        }
        LOGGER.info("方块类型: {} 未找到匹配的配置，返回默认限制 -1", blockType);
        return null;  // 如果没有找到匹配的方块类型，返回 null
    }

}
