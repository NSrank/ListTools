package org.plugin.listtools;

import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 白名单管理器
 * 提供线程安全的白名单操作接口
 */
public class WhitelistManager {
    private final ConfigManager configManager;
    private final Logger logger;
    private final Set<String> whitelistCache;
    private final ReadWriteLock lock;

    public WhitelistManager(ConfigManager configManager, Logger logger) {
        this.configManager = configManager;
        this.logger = logger;
        this.whitelistCache = ConcurrentHashMap.newKeySet();
        this.lock = new ReentrantReadWriteLock();
        
        // 初始化缓存
        refreshCache();
    }

    /**
     * 刷新白名单缓存
     */
    public void refreshCache() {
        lock.writeLock().lock();
        try {
            whitelistCache.clear();
            List<String> whitelist = configManager.getWhitelist();
            whitelistCache.addAll(whitelist);
            logger.debug("白名单缓存已刷新，共 {} 个玩家", whitelistCache.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 检查玩家是否在白名单中
     * 严格匹配玩家名，区分大小写
     */
    public boolean isWhitelisted(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return false;
        }
        
        lock.readLock().lock();
        try {
            return whitelistCache.contains(playerName);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 添加玩家到白名单
     * @param playerName 玩家名
     * @return 是否成功添加（如果已存在则返回false）
     */
    public boolean addPlayer(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return false;
        }
        
        playerName = playerName.trim();
        
        lock.writeLock().lock();
        try {
            if (whitelistCache.contains(playerName)) {
                return false; // 已存在
            }
            
            // 添加到缓存
            whitelistCache.add(playerName);
            
            // 更新配置文件
            List<String> whitelist = new ArrayList<>(whitelistCache);
            Collections.sort(whitelist); // 排序以保持一致性
            configManager.setWhitelist(whitelist);
            configManager.saveConfig();
            
            logger.info("玩家 {} 已添加到白名单", playerName);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 从白名单中移除玩家
     * @param playerName 玩家名
     * @return 是否成功移除（如果不存在则返回false）
     */
    public boolean removePlayer(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return false;
        }
        
        playerName = playerName.trim();
        
        lock.writeLock().lock();
        try {
            if (!whitelistCache.contains(playerName)) {
                return false; // 不存在
            }
            
            // 从缓存中移除
            whitelistCache.remove(playerName);
            
            // 更新配置文件
            List<String> whitelist = new ArrayList<>(whitelistCache);
            Collections.sort(whitelist); // 排序以保持一致性
            configManager.setWhitelist(whitelist);
            configManager.saveConfig();
            
            logger.info("玩家 {} 已从白名单中移除", playerName);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 获取白名单列表的副本
     * @return 排序后的白名单列表
     */
    public List<String> getWhitelistCopy() {
        lock.readLock().lock();
        try {
            List<String> result = new ArrayList<>(whitelistCache);
            Collections.sort(result);
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 获取白名单大小
     */
    public int getWhitelistSize() {
        lock.readLock().lock();
        try {
            return whitelistCache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 清空白名单
     */
    public void clearWhitelist() {
        lock.writeLock().lock();
        try {
            whitelistCache.clear();
            configManager.setWhitelist(new ArrayList<>());
            configManager.saveConfig();
            logger.info("白名单已清空");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 批量添加玩家到白名单
     * @param playerNames 玩家名列表
     * @return 成功添加的玩家数量
     */
    public int addPlayers(List<String> playerNames) {
        if (playerNames == null || playerNames.isEmpty()) {
            return 0;
        }
        
        lock.writeLock().lock();
        try {
            int addedCount = 0;
            for (String playerName : playerNames) {
                if (playerName != null && !playerName.trim().isEmpty()) {
                    String trimmedName = playerName.trim();
                    if (!whitelistCache.contains(trimmedName)) {
                        whitelistCache.add(trimmedName);
                        addedCount++;
                    }
                }
            }
            
            if (addedCount > 0) {
                // 更新配置文件
                List<String> whitelist = new ArrayList<>(whitelistCache);
                Collections.sort(whitelist);
                configManager.setWhitelist(whitelist);
                configManager.saveConfig();
                logger.info("批量添加了 {} 个玩家到白名单", addedCount);
            }
            
            return addedCount;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 批量移除玩家从白名单
     * @param playerNames 玩家名列表
     * @return 成功移除的玩家数量
     */
    public int removePlayers(List<String> playerNames) {
        if (playerNames == null || playerNames.isEmpty()) {
            return 0;
        }
        
        lock.writeLock().lock();
        try {
            int removedCount = 0;
            for (String playerName : playerNames) {
                if (playerName != null && !playerName.trim().isEmpty()) {
                    String trimmedName = playerName.trim();
                    if (whitelistCache.remove(trimmedName)) {
                        removedCount++;
                    }
                }
            }
            
            if (removedCount > 0) {
                // 更新配置文件
                List<String> whitelist = new ArrayList<>(whitelistCache);
                Collections.sort(whitelist);
                configManager.setWhitelist(whitelist);
                configManager.saveConfig();
                logger.info("批量移除了 {} 个玩家从白名单", removedCount);
            }
            
            return removedCount;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 检查白名单是否为空
     */
    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return whitelistCache.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }
}
