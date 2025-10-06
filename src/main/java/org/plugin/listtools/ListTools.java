/*
 * MIT License
 *
 * Copyright (c) 2025 NSrank & Augment
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.plugin.listtools;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * ListTools 白名单系统插件主类
 *
 * 功能特性：
 * - 白名单系统：控制玩家连接权限
 * - 命令管理：提供完整的白名单管理命令
 * - 自动检查：定期检查在线玩家白名单状态
 * - 配置管理：支持YAML配置文件
 *
 * @author NSrank, Augment
 * @version 1.0-SNAPSHOT
 */
@Plugin(
        id = "listtools",
        name = "ListTools",
        version = "1.0-SNAPSHOT",
        authors = {"NSrank", "Augment"},
        description = "A comprehensive whitelist system for Velocity proxy"
)
public class ListTools {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer proxyServer;

    @Inject
    @DataDirectory
    private Path dataDirectory;

    // 核心组件
    private ConfigManager configManager;
    private WhitelistManager whitelistManager;
    private PlayerConnectionListener connectionListener;
    private AutoCheckTask autoCheckTask;
    private ListToolsCommand command;

    /**
     * 插件初始化事件
     */
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("ListTools 白名单系统正在启动...");

        try {
            // 初始化配置管理器
            configManager = new ConfigManager(dataDirectory, logger);
            configManager.loadConfig();
            logger.info("配置管理器初始化完成");

            // 初始化白名单管理器
            whitelistManager = new WhitelistManager(configManager, logger);
            logger.info("白名单管理器初始化完成，当前白名单玩家数: {}", whitelistManager.getWhitelistSize());

            // 初始化连接监听器
            connectionListener = new PlayerConnectionListener(configManager, whitelistManager, logger);
            proxyServer.getEventManager().register(this, connectionListener);
            logger.info("玩家连接监听器注册完成");

            // 初始化自动检查任务
            autoCheckTask = new AutoCheckTask(configManager, whitelistManager, proxyServer, logger);
            if (configManager.isEnabled()) {
                autoCheckTask.start();
                logger.info("自动检查任务启动完成");
            } else {
                logger.info("白名单系统已禁用，自动检查任务未启动");
            }

            // 注册命令
            command = new ListToolsCommand(configManager, whitelistManager, proxyServer, logger);
            CommandManager commandManager = proxyServer.getCommandManager();
            commandManager.register(commandManager.metaBuilder("listtools")
                    .aliases("lt", "whitelist")
                    .build(), command);
            logger.info("命令注册完成: /listtools, /lt, /whitelist");

            logger.info("===================================");
            logger.info("ListTools v1.0 已加载");
            logger.info("白名单系统启动完成！");
            logger.info("当前状态: 白名单{}, 玩家数量: {}",
                       configManager.isEnabled() ? "启用" : "禁用",
                       whitelistManager.getWhitelistSize());
            logger.info("作者：NSrank & Augment");
            logger.info("===================================");

        } catch (Exception e) {
            logger.error("ListTools 初始化失败", e);
            throw new RuntimeException("插件初始化失败", e);
        }
    }

    /**
     * 插件关闭事件
     */
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("ListTools 正在关闭...");

        try {
            // 停止自动检查任务
            if (autoCheckTask != null) {
                autoCheckTask.stop();
                logger.info("自动检查任务已停止");
            }

            // 保存配置
            if (configManager != null) {
                configManager.saveConfig();
                logger.info("配置文件已保存");
            }

            logger.info("ListTools 关闭完成");

        } catch (Exception e) {
            logger.error("ListTools 关闭时发生错误", e);
        }
    }

    /**
     * 获取配置管理器
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * 获取白名单管理器
     */
    public WhitelistManager getWhitelistManager() {
        return whitelistManager;
    }

    /**
     * 获取自动检查任务
     */
    public AutoCheckTask getAutoCheckTask() {
        return autoCheckTask;
    }

    /**
     * 获取插件数据目录
     */
    public Path getDataDirectory() {
        return dataDirectory;
    }

    /**
     * 获取日志记录器
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * 获取代理服务器实例
     */
    public ProxyServer getProxyServer() {
        return proxyServer;
    }
}
