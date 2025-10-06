package org.plugin.listtools;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 配置文件管理器
 * 负责处理config.yml的读取、写入和默认配置生成
 */
public class ConfigManager {
    private final Path configPath;
    private final Logger logger;
    private Map<String, Object> config;
    private final Yaml yaml;

    public ConfigManager(Path dataDirectory, Logger logger) {
        this.configPath = dataDirectory.resolve("config.yml");
        this.logger = logger;
        
        // 配置YAML输出格式
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        this.yaml = new Yaml(options);
    }

    /**
     * 加载配置文件
     * 如果文件不存在则创建默认配置
     */
    public void loadConfig() {
        try {
            // 确保数据目录存在
            Files.createDirectories(configPath.getParent());
            
            if (!Files.exists(configPath)) {
                createDefaultConfig();
                logger.info("已创建默认配置文件: {}", configPath);
            }
            
            try (InputStream inputStream = Files.newInputStream(configPath)) {
                config = yaml.load(inputStream);
                if (config == null) {
                    config = new HashMap<>();
                }
                logger.info("配置文件加载成功");
            }
        } catch (IOException e) {
            logger.error("加载配置文件失败", e);
            config = createDefaultConfigMap();
        }
    }

    /**
     * 保存配置文件
     */
    public void saveConfig() {
        try (FileWriter writer = new FileWriter(configPath.toFile())) {
            yaml.dump(config, writer);
            logger.debug("配置文件保存成功");
        } catch (IOException e) {
            logger.error("保存配置文件失败", e);
        }
    }

    /**
     * 创建默认配置文件
     */
    private void createDefaultConfig() {
        config = createDefaultConfigMap();
        saveConfig();
    }

    /**
     * 创建默认配置映射
     */
    private Map<String, Object> createDefaultConfigMap() {
        Map<String, Object> defaultConfig = new LinkedHashMap<>();
        defaultConfig.put("enabled", true);
        defaultConfig.put("kick_message", "你还没有白名单！快去服务器群里申请吧！(｀・ω・´)");
        defaultConfig.put("auto_check", "1h");
        defaultConfig.put("whitelist", new ArrayList<String>());
        return defaultConfig;
    }

    /**
     * 获取白名单是否启用
     */
    public boolean isEnabled() {
        return (Boolean) config.getOrDefault("enabled", true);
    }

    /**
     * 设置白名单启用状态
     */
    public void setEnabled(boolean enabled) {
        config.put("enabled", enabled);
    }

    /**
     * 获取踢出消息
     */
    public String getKickMessage() {
        return (String) config.getOrDefault("kick_message", "你还没有白名单！快去服务器群里申请吧！(｀・ω・´)");
    }

    /**
     * 设置踢出消息
     */
    public void setKickMessage(String message) {
        config.put("kick_message", message);
    }

    /**
     * 获取自动检查间隔（毫秒）
     */
    public long getAutoCheckInterval() {
        String interval = (String) config.getOrDefault("auto_check", "1h");
        return parseTimeInterval(interval);
    }

    /**
     * 设置自动检查间隔
     */
    public void setAutoCheckInterval(String interval) {
        config.put("auto_check", interval);
    }

    /**
     * 获取白名单列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getWhitelist() {
        Object whitelistObj = config.get("whitelist");
        if (whitelistObj instanceof List) {
            return new ArrayList<>((List<String>) whitelistObj);
        }
        return new ArrayList<>();
    }

    /**
     * 设置白名单列表
     */
    public void setWhitelist(List<String> whitelist) {
        config.put("whitelist", new ArrayList<>(whitelist));
    }

    /**
     * 解析时间间隔字符串为毫秒
     * 支持格式: 30s, 5m, 1h
     */
    private long parseTimeInterval(String interval) {
        if (interval == null || interval.isEmpty()) {
            return TimeUnit.HOURS.toMillis(1); // 默认1小时
        }
        
        interval = interval.toLowerCase().trim();
        
        try {
            if (interval.endsWith("s")) {
                long seconds = Long.parseLong(interval.substring(0, interval.length() - 1));
                return TimeUnit.SECONDS.toMillis(seconds);
            } else if (interval.endsWith("m")) {
                long minutes = Long.parseLong(interval.substring(0, interval.length() - 1));
                return TimeUnit.MINUTES.toMillis(minutes);
            } else if (interval.endsWith("h")) {
                long hours = Long.parseLong(interval.substring(0, interval.length() - 1));
                return TimeUnit.HOURS.toMillis(hours);
            } else {
                // 如果没有单位，默认为秒
                long seconds = Long.parseLong(interval);
                return TimeUnit.SECONDS.toMillis(seconds);
            }
        } catch (NumberFormatException e) {
            logger.warn("无效的时间间隔格式: {}, 使用默认值1小时", interval);
            return TimeUnit.HOURS.toMillis(1);
        }
    }

    /**
     * 重新加载配置文件
     */
    public void reloadConfig() {
        loadConfig();
    }
}
