package org.plugin.listtools;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

/**
 * 玩家连接事件监听器
 * 处理玩家登录前的白名单检查
 */
public class PlayerConnectionListener {
    private final ConfigManager configManager;
    private final WhitelistManager whitelistManager;
    private final Logger logger;

    public PlayerConnectionListener(ConfigManager configManager, WhitelistManager whitelistManager, Logger logger) {
        this.configManager = configManager;
        this.whitelistManager = whitelistManager;
        this.logger = logger;
    }

    /**
     * 处理玩家登录前事件
     * 检查白名单状态，拒绝未授权玩家
     */
    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        // 检查白名单系统是否启用
        if (!configManager.isEnabled()) {
            logger.debug("白名单系统已禁用，允许玩家 {} 连接", event.getUsername());
            return;
        }

        String playerName = event.getUsername();
        
        // 检查玩家是否在白名单中
        if (!whitelistManager.isWhitelisted(playerName)) {
            // 玩家不在白名单中，拒绝连接
            String kickMessage = configManager.getKickMessage();
            Component kickComponent = Component.text(kickMessage, NamedTextColor.RED);
            
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(kickComponent));
            
            logger.info("拒绝玩家 {} 连接：不在白名单中", playerName);
            return;
        }

        // 玩家在白名单中，允许连接
        logger.debug("允许玩家 {} 连接：在白名单中", playerName);
    }

    /**
     * 处理玩家成功连接到服务器事件
     * 用于记录和统计
     */
    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        if (!configManager.isEnabled()) {
            return;
        }

        String playerName = event.getPlayer().getUsername();
        String serverName = event.getServer().getServerInfo().getName();
        
        logger.debug("白名单玩家 {} 已连接到服务器 {}", playerName, serverName);
    }
}
