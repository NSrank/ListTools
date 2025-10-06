package org.plugin.listtools;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * ListTools主命令处理器
 * 处理/listtools命令及其子命令
 */
public class ListToolsCommand implements SimpleCommand {
    private final ConfigManager configManager;
    private final WhitelistManager whitelistManager;
    private final ProxyServer proxyServer;
    private final Logger logger;

    public ListToolsCommand(ConfigManager configManager, WhitelistManager whitelistManager, 
                           ProxyServer proxyServer, Logger logger) {
        this.configManager = configManager;
        this.whitelistManager = whitelistManager;
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        // 检查权限
        if (!hasPermission(source)) {
            source.sendMessage(Component.text("你没有权限使用此命令！", NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            sendHelp(source);
            return;
        }

        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "whitelist":
                handleWhitelistCommand(source, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "reload":
                handleReloadCommand(source);
                break;
            case "status":
                handleStatusCommand(source);
                break;
            default:
                sendHelp(source);
                break;
        }
    }

    /**
     * 处理白名单相关命令
     */
    private void handleWhitelistCommand(CommandSource source, String[] args) {
        if (args.length == 0) {
            sendWhitelistHelp(source);
            return;
        }

        String action = args[0].toLowerCase();
        
        switch (action) {
            case "add":
                handleWhitelistAdd(source, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "remove":
                handleWhitelistRemove(source, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "list":
                handleWhitelistList(source);
                break;
            default:
                sendWhitelistHelp(source);
                break;
        }
    }

    /**
     * 处理添加白名单命令
     */
    private void handleWhitelistAdd(CommandSource source, String[] args) {
        if (args.length == 0) {
            source.sendMessage(Component.text("用法: /listtools whitelist add <玩家名>", NamedTextColor.RED));
            return;
        }

        String playerName = args[0];
        
        if (whitelistManager.addPlayer(playerName)) {
            source.sendMessage(Component.text("成功将玩家 " + playerName + " 添加到白名单", NamedTextColor.GREEN));
            logger.info("{} 将玩家 {} 添加到白名单", getSourceName(source), playerName);
        } else {
            source.sendMessage(Component.text("玩家 " + playerName + " 已经在白名单中", NamedTextColor.YELLOW));
        }
    }

    /**
     * 处理移除白名单命令
     */
    private void handleWhitelistRemove(CommandSource source, String[] args) {
        if (args.length == 0) {
            source.sendMessage(Component.text("用法: /listtools whitelist remove <玩家名>", NamedTextColor.RED));
            return;
        }

        String playerName = args[0];
        
        if (whitelistManager.removePlayer(playerName)) {
            source.sendMessage(Component.text("成功将玩家 " + playerName + " 从白名单中移除", NamedTextColor.GREEN));
            logger.info("{} 将玩家 {} 从白名单中移除", getSourceName(source), playerName);
            
            // 检查在线玩家，踢出没有白名单的玩家
            kickUnauthorizedPlayers(source);
        } else {
            source.sendMessage(Component.text("玩家 " + playerName + " 不在白名单中", NamedTextColor.YELLOW));
        }
    }

    /**
     * 处理查看白名单命令
     */
    private void handleWhitelistList(CommandSource source) {
        List<String> whitelist = whitelistManager.getWhitelistCopy();
        
        if (whitelist.isEmpty()) {
            source.sendMessage(Component.text("白名单为空", NamedTextColor.YELLOW));
            return;
        }

        source.sendMessage(Component.text("白名单 (" + whitelist.size() + " 个玩家):", NamedTextColor.AQUA));
        String playerList = String.join(" , ", whitelist);
        source.sendMessage(Component.text(playerList, NamedTextColor.WHITE));
    }

    /**
     * 处理重载配置命令
     */
    private void handleReloadCommand(CommandSource source) {
        configManager.reloadConfig();
        whitelistManager.refreshCache();
        source.sendMessage(Component.text("配置文件已重载", NamedTextColor.GREEN));
        logger.info("{} 重载了配置文件", getSourceName(source));
    }

    /**
     * 处理状态查看命令
     */
    private void handleStatusCommand(CommandSource source) {
        boolean enabled = configManager.isEnabled();
        int whitelistSize = whitelistManager.getWhitelistSize();
        long autoCheckInterval = configManager.getAutoCheckInterval();
        
        source.sendMessage(Component.text("=== ListTools 状态 ===", NamedTextColor.GOLD));
        source.sendMessage(Component.text("白名单系统: " + (enabled ? "启用" : "禁用"), 
                                        enabled ? NamedTextColor.GREEN : NamedTextColor.RED));
        source.sendMessage(Component.text("白名单玩家数量: " + whitelistSize, NamedTextColor.AQUA));
        source.sendMessage(Component.text("自动检查间隔: " + formatInterval(autoCheckInterval), NamedTextColor.AQUA));
        source.sendMessage(Component.text("在线玩家数量: " + proxyServer.getPlayerCount(), NamedTextColor.AQUA));
    }

    /**
     * 踢出未授权的在线玩家
     */
    private void kickUnauthorizedPlayers(CommandSource source) {
        if (!configManager.isEnabled()) {
            return;
        }

        String kickMessage = configManager.getKickMessage();
        Component kickComponent = Component.text(kickMessage, NamedTextColor.RED);
        
        int kickedCount = 0;
        for (Player player : proxyServer.getAllPlayers()) {
            if (!whitelistManager.isWhitelisted(player.getUsername())) {
                player.disconnect(kickComponent);
                kickedCount++;
                logger.info("踢出未授权玩家: {}", player.getUsername());
            }
        }
        
        if (kickedCount > 0) {
            source.sendMessage(Component.text("已踢出 " + kickedCount + " 个未授权玩家", NamedTextColor.YELLOW));
        }
    }

    /**
     * 发送帮助信息
     */
    private void sendHelp(CommandSource source) {
        source.sendMessage(Component.text("=== ListTools 命令帮助 ===", NamedTextColor.GOLD));
        source.sendMessage(Component.text("/listtools whitelist add <玩家名> - 添加玩家到白名单", NamedTextColor.AQUA));
        source.sendMessage(Component.text("/listtools whitelist remove <玩家名> - 从白名单移除玩家", NamedTextColor.AQUA));
        source.sendMessage(Component.text("/listtools whitelist list - 查看白名单", NamedTextColor.AQUA));
        source.sendMessage(Component.text("/listtools reload - 重载配置文件", NamedTextColor.AQUA));
        source.sendMessage(Component.text("/listtools status - 查看插件状态", NamedTextColor.AQUA));
    }

    /**
     * 发送白名单命令帮助
     */
    private void sendWhitelistHelp(CommandSource source) {
        source.sendMessage(Component.text("=== 白名单命令帮助 ===", NamedTextColor.GOLD));
        source.sendMessage(Component.text("/listtools whitelist add <玩家名> - 添加玩家到白名单", NamedTextColor.AQUA));
        source.sendMessage(Component.text("/listtools whitelist remove <玩家名> - 从白名单移除玩家", NamedTextColor.AQUA));
        source.sendMessage(Component.text("/listtools whitelist list - 查看白名单", NamedTextColor.AQUA));
    }

    /**
     * 检查命令源是否有权限
     */
    private boolean hasPermission(CommandSource source) {
        // 控制台总是有权限
        if (!(source instanceof Player)) {
            return true;
        }
        
        // 检查玩家权限
        return source.hasPermission("listtools.admin");
    }

    /**
     * 获取命令源名称
     */
    private String getSourceName(CommandSource source) {
        if (source instanceof Player) {
            return ((Player) source).getUsername();
        }
        return "控制台";
    }

    /**
     * 格式化时间间隔显示
     */
    private String formatInterval(long millis) {
        long seconds = millis / 1000;
        if (seconds < 60) {
            return seconds + "秒";
        } else if (seconds < 3600) {
            return (seconds / 60) + "分钟";
        } else {
            return (seconds / 3600) + "小时";
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        // 检查权限，没有权限则不提供补全
        if (!hasPermission(source)) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        if (args.length == 1) {
            // 第一级子命令建议
            List<String> suggestions = Arrays.asList("whitelist", "reload", "status");
            String input = args[0].toLowerCase();

            // 过滤匹配的建议
            return CompletableFuture.completedFuture(
                suggestions.stream()
                    .filter(cmd -> cmd.startsWith(input))
                    .toList()
            );
        } else if (args.length == 2 && "whitelist".equalsIgnoreCase(args[0])) {
            // 白名单子命令建议
            List<String> suggestions = Arrays.asList("add", "remove", "list");
            String input = args[1].toLowerCase();

            return CompletableFuture.completedFuture(
                suggestions.stream()
                    .filter(cmd -> cmd.startsWith(input))
                    .toList()
            );
        } else if (args.length == 3 && "whitelist".equalsIgnoreCase(args[0])) {
            String action = args[1].toLowerCase();
            String input = args[2];

            if ("remove".equals(action)) {
                // 移除命令建议白名单中的玩家
                return CompletableFuture.completedFuture(
                    whitelistManager.getWhitelistCopy().stream()
                        .filter(player -> player.toLowerCase().startsWith(input.toLowerCase()))
                        .toList()
                );
            } else if ("add".equals(action)) {
                // 添加命令建议在线玩家（排除已在白名单中的）
                List<String> onlinePlayers = new ArrayList<>();
                for (Player player : proxyServer.getAllPlayers()) {
                    String playerName = player.getUsername();
                    if (!whitelistManager.isWhitelisted(playerName) &&
                        playerName.toLowerCase().startsWith(input.toLowerCase())) {
                        onlinePlayers.add(playerName);
                    }
                }
                return CompletableFuture.completedFuture(onlinePlayers);
            }
        }

        return CompletableFuture.completedFuture(new ArrayList<>());
    }
}
