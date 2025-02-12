package org.windy.customWorldLimit;


import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Map;

public class BlockPlaceListener {

    private final MySQL mySQL;
    private final plugin plugin;

    // 构造函数
    public BlockPlaceListener(MySQL mySQL, plugin plugin) {
        this.mySQL = mySQL;
        this.plugin = plugin;
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {

        String name = event.getEntity().getBukkitEntity().getName();

        Player player = Bukkit.getPlayer(name);

        String worldName = player.getWorld().getName().toString().replace(":", "_");




        // 获取放置的方块状态
        BlockState blockState = event.getPlacedBlock();
        // 获取方块的实际类型
        String blockType = blockState.toString();

        log(name+"放置的方块是：" + blockType);

        // 获取配置文件中的方块数量限制
        Map.Entry<String, Integer> blockLimitEntry = plugin.getBlockLimit(blockType);
        if (blockLimitEntry == null) {
            return;  // 如果没有找到配置项，则跳过
        }

        String matchedBlockType = blockLimitEntry.getKey();  // 获取匹配的方块类型
        int blockLimit = blockLimitEntry.getValue();  // 获取匹配的方块数量限制

        try {
            // 查询当前世界中该方块的数量
            int currentCount = mySQL.getBlockCount(worldName, matchedBlockType);
            // 如果当前方块数量超过限制，取消事件
            if (currentCount >= blockLimit) {
                player.sendMessage("你不能放这个了 "+currentCount+"/"+blockLimit);
                event.setCanceled(true);
                return;
            }
            if (blockLimit != -1) {
                player.sendMessage("当前放置 "+currentCount+"/"+blockLimit);
            }
            // 更新数据库中的方块数量
            mySQL.insertOrUpdateBlockCount(worldName, matchedBlockType, currentCount + 1);
        } catch (SQLException e) {
            plugin.getLogger().info("更新数据发生错误: " + e.getMessage());
        }
    }
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        String name = event.getPlayer().getBukkitEntity().getName();

        Player player = Bukkit.getPlayer(name);

        String worldName = player.getWorld().getName().toString().replace(":", "_");

        // 获取破坏的方块状态
        BlockState blockState = event.getState();
        // 获取方块的实际类型
        String blockType = blockState.toString();
        plugin.getLogger().info(name+"挖掉的方块是：" + blockType);

        Map.Entry<String, Integer> blockLimitEntry = plugin.getBlockLimit(blockType);
        if (blockLimitEntry == null) {
            return;  // 如果没有找到配置项，则跳过
        }

        String matchedBlockType = blockLimitEntry.getKey();  // 获取匹配的方块类型


        try {
            // 查询当前世界中该方块的数量
            int currentCount = plugin.getMySQL().getBlockCount(worldName, matchedBlockType);

            // 如果当前方块数量为 0，则不允许破坏该方块
            if (currentCount <= 0) {
                return;
            }
            // 更新数据库中的方块数量，减去 1
            plugin.getMySQL().insertOrUpdateBlockCount(worldName, matchedBlockType, currentCount - 1);
        } catch (SQLException e) {
            plugin.getLogger().info("Error while updating the database: " + e.getMessage());
        }
    }

    // 提取中括号中的世界名称
    private String extractWorldName(String worldName) {
        if (worldName.contains("[")) {
            return worldName.substring(worldName.indexOf('[') + 1, worldName.indexOf(']'));
        }
        return worldName; // 如果没有中括号，返回原始名称
    }

    private void log(String message){
        if(plugin.DEBUG){
            plugin.getLogger().info(message);
        }
    }
}
