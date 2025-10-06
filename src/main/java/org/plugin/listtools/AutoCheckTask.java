package org.plugin.listtools;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * 自动检查任务
 * 定期检查在线玩家的白名单状态，踢出未授权玩家
 */
public class AutoCheckTask {
    private final ConfigManager configManager;
    private final WhitelistManager whitelistManager;
    private final ProxyServer proxyServer;
    private final Logger logger;
    private ScheduledTask currentTask;

    public AutoCheckTask(ConfigManager configManager, WhitelistManager whitelistManager, 
                        ProxyServer proxyServer, Logger logger) {
        this.configManager = configManager;
        this.whitelistManager = whitelistManager;
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    /**
     * 启动自动检查任务
     */
    public void start() {
        // 停止现有任务
        stop();
        
        long interval = configManager.getAutoCheckInterval();
        
        currentTask = proxyServer.getScheduler()
            .buildTask(this, this::performCheck)
            .repeat(interval, TimeUnit.MILLISECONDS)
            .schedule();
        
        logger.info("自动检查任务已启动，检查间隔: {}ms", interval);
    }

    /**
     * 停止自动检查任务
     */
    public void stop() {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
            logger.info("自动检查任务已停止");
        }
    }

    /**
     * 重启自动检查任务
     * 用于配置更新后重新应用新的检查间隔
     */
    public void restart() {
        logger.info("重启自动检查任务");
        start();
    }

    /**
     * 执行检查逻辑
     */
    private void performCheck() {
        try {
            // 检查白名单系统是否启用
            if (!configManager.isEnabled()) {
                logger.debug("白名单系统已禁用，跳过自动检查");
                return;
            }

            logger.debug("开始执行自动白名单检查");
            
            String kickMessage = configManager.getKickMessage();
            Component kickComponent = Component.text(kickMessage, NamedTextColor.RED);
            
            int kickedCount = 0;
            int totalPlayers = proxyServer.getPlayerCount();
            
            // 检查所有在线玩家
            for (Player player : proxyServer.getAllPlayers()) {
                String playerName = player.getUsername();
                
                if (!whitelistManager.isWhitelisted(playerName)) {
                    // 玩家不在白名单中，踢出
                    player.disconnect(kickComponent);
                    kickedCount++;
                    logger.info("自动检查：踢出未授权玩家 {}", playerName);
                }
            }
            
            if (kickedCount > 0) {
                logger.info("自动检查完成：检查了 {} 个玩家，踢出了 {} 个未授权玩家", 
                          totalPlayers, kickedCount);
            } else {
                logger.debug("自动检查完成：检查了 {} 个玩家，无需踢出", totalPlayers);
            }
            
        } catch (Exception e) {
            logger.error("执行自动检查时发生错误", e);
        }
    }

    /**
     * 手动执行一次检查
     * @return 踢出的玩家数量
     */
    public int performManualCheck() {
        if (!configManager.isEnabled()) {
            logger.debug("白名单系统已禁用，跳过手动检查");
            return 0;
        }

        logger.info("执行手动白名单检查");
        
        String kickMessage = configManager.getKickMessage();
        Component kickComponent = Component.text(kickMessage, NamedTextColor.RED);
        
        int kickedCount = 0;
        
        for (Player player : proxyServer.getAllPlayers()) {
            String playerName = player.getUsername();
            
            if (!whitelistManager.isWhitelisted(playerName)) {
                player.disconnect(kickComponent);
                kickedCount++;
                logger.info("手动检查：踢出未授权玩家 {}", playerName);
            }
        }
        
        logger.info("手动检查完成：踢出了 {} 个未授权玩家", kickedCount);
        return kickedCount;
    }

    /**
     * 获取当前任务状态
     */
    public boolean isRunning() {
        return currentTask != null;
    }

    /**
     * 获取下次执行时间（毫秒）
     * @return 距离下次执行的毫秒数，如果任务未运行则返回-1
     */
    public long getNextExecutionTime() {
        if (!isRunning()) {
            return -1;
        }
        
        // Velocity的ScheduledTask没有直接获取下次执行时间的方法
        // 这里返回配置的间隔时间作为估计
        return configManager.getAutoCheckInterval();
    }

    /**
     * 获取任务统计信息
     */
    public TaskStats getStats() {
        return new TaskStats(
            isRunning(),
            configManager.getAutoCheckInterval(),
            proxyServer.getPlayerCount(),
            whitelistManager.getWhitelistSize()
        );
    }

    /**
     * 任务统计信息类
     */
    public static class TaskStats {
        private final boolean running;
        private final long interval;
        private final int onlinePlayers;
        private final int whitelistSize;

        public TaskStats(boolean running, long interval, int onlinePlayers, int whitelistSize) {
            this.running = running;
            this.interval = interval;
            this.onlinePlayers = onlinePlayers;
            this.whitelistSize = whitelistSize;
        }

        public boolean isRunning() {
            return running;
        }

        public long getInterval() {
            return interval;
        }

        public int getOnlinePlayers() {
            return onlinePlayers;
        }

        public int getWhitelistSize() {
            return whitelistSize;
        }

        @Override
        public String toString() {
            return String.format("TaskStats{running=%s, interval=%dms, onlinePlayers=%d, whitelistSize=%d}",
                               running, interval, onlinePlayers, whitelistSize);
        }
    }
}
