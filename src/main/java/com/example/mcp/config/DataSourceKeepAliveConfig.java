package com.example.mcp.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据源连接池保活配置
 * 每 3 分钟对所有数据源执行一次心跳检测，防止长时间空闲后连接被数据库服务端或中间网络设备断开
 */
@Configuration
@EnableScheduling
public class DataSourceKeepAliveConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceKeepAliveConfig.class);

    private final DynamicRoutingDataSource dynamicRoutingDataSource;
    private final Map<String, AtomicInteger> failCount = new ConcurrentHashMap<>();

    public DataSourceKeepAliveConfig(DataSource dataSource) {
        this.dynamicRoutingDataSource = (DynamicRoutingDataSource) dataSource;
    }

    /**
     * 每 3 分钟对所有数据源做心跳检测
     */
    @Scheduled(fixedRate = 180_000, initialDelay = 60_000)
    public void keepAlive() {
        Map<String, DataSource> dataSources = dynamicRoutingDataSource.getDataSources();
        for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
            String name = entry.getKey();
            try (Connection conn = entry.getValue().getConnection()) {
                if (conn.isValid(5)) {
                    failCount.remove(name);
                } else {
                    log.warn("数据源 [{}] 心跳检测: 连接无效", name);
                }
            } catch (Exception e) {
                int fails = failCount.computeIfAbsent(name, k -> new AtomicInteger(0)).incrementAndGet();
                if (fails <= 3) {
                    log.warn("数据源 [{}] 心跳检测失败 (第{}次): {}", name, fails, e.getMessage());
                } else {
                    log.error("数据源 [{}] 连续 {} 次心跳失败，请检查数据库状态", name, fails);
                }
            }
        }
    }
}
