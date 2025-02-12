package org.windy.customWorldLimit;

import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySQL {
    private Connection connection;
    private static final Logger LOGGER = LoggerFactory.getLogger(MySQL.class);


    // 新增的配置字段
    private String mySQLHost;
    private String mySQLPort;
    private String mySQLUsername;
    private String mySQLPassword;
    private String mySQLDatabase;

    // 构造函数接收从插件中获取的配置
    public MySQL(String mySQLHost, String mySQLPort, String mySQLUsername, String mySQLPassword, String mySQLDatabase) {
        this.mySQLHost = mySQLHost;
        this.mySQLPort = mySQLPort;
        this.mySQLUsername = mySQLUsername;
        this.mySQLPassword = mySQLPassword;
        this.mySQLDatabase = mySQLDatabase;
    }

    // 连接到 MySQL 数据库
    public void connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = "jdbc:mysql://" + mySQLHost + ":" + mySQLPort + "/" + mySQLDatabase + "?useSSL=false&serverTimezone=UTC";

            try {
                LOGGER.info("正在连接 {}...", url);
                connection = DriverManager.getConnection(url, mySQLUsername, mySQLPassword);
            } catch (SQLException e) {
                LOGGER.error("连接数据库失败：", e);
                throw e; // 抛出异常以便上层处理
            }
        }
    }

    // 确保数据库连接始终有效
    private void ensureConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();  // 如果连接关闭，则重新连接
        }
    }

    // 创建表格：统一使用一个表格来存储所有世界的方块信息
    public void createTable() throws SQLException {
        ensureConnection();  // 确保连接有效
        String createTableSQL = "CREATE TABLE IF NOT EXISTS block_limits (" +
                "world_name VARCHAR(128) CHARACTER SET utf8," +  // 使用 utf8 字符集，缩短字段长度
                "block_type VARCHAR(128) CHARACTER SET utf8," +  // 使用 utf8 字符集，缩短字段长度
                "block_count INT," +
                "PRIMARY KEY (world_name, block_type)) CHARACTER SET utf8;";  // 修改索引字段长度
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
        }
    }

    public void dropExistingTable() throws SQLException {
        ensureConnection();  // 确保连接有效
        String dropTableSQL = "DROP TABLE IF EXISTS block_limits;";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(dropTableSQL);
            LOGGER.info("[CustomWorldLimits] Dropped existing block_limits table.");
        }
    }

    // 插入或更新方块数量
    public void insertOrUpdateBlockCount(String worldName, String blockType, int count) throws SQLException {
        ensureConnection();  // 确保连接有效
        try {
            String sql = "INSERT INTO block_limits (world_name, block_type, block_count) " +
                    "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE block_count = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, worldName);
                pstmt.setString(2, blockType);
                pstmt.setInt(3, count);
                pstmt.setInt(4, count);


                pstmt.executeUpdate();

            }
        } catch (SQLException e) {
            LOGGER.error("错误： {}  {}:", worldName, blockType, e);
            throw e;
        }
    }

    // 获取某世界某种方块的数量
    public int getBlockCount(String worldName, String blockType) throws SQLException {
        ensureConnection();  // 确保连接有效
        String sql = "SELECT block_count FROM block_limits WHERE world_name = ? AND block_type = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, worldName);
            pstmt.setString(2, blockType);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("block_count");
                }
                return 0; // 如果没有记录，返回0
            }
        } catch (SQLException e) {
            LOGGER.error("无法获取 {} 和 {}:", worldName, blockType, e);
            throw e;
        }
    }

    // 关闭连接
    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            LOGGER.info("数据库连接已断开！");
        }
    }
}
