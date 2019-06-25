package com.zheng.nie.shardingjdbc.config;

import com.dangdang.ddframe.rdb.sharding.api.ShardingDataSourceFactory;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 *
 *
 * 逻辑表（LogicTable）即数据分片的逻辑表，对于水平拆分的数据库(表)，同一类表的总称。
 * 例：订单数据根据订单ID取模拆分为16张表,分别是t_order_0到t_order_15，
 * 他们的逻辑表名为t_order；实际表（ActualTable）是指在分片的数据库中真实存在的物理表。即这个示例中的t_order_0到t_order_15。
 *
 *
 如果SQL中分表列order_id条件为where order_id in(?, ?)，那么shardingValue的type为LIST，那么分表逻辑走doInSharding()；
 如果SQL中分表列order_id条件为where order_id between in(?, ?)，那么shardingValue的type为RANGE，那么分表逻辑走doBetweenSharding()；
 *
 *
 * @author: niezheng1
 * @Date: 2018/11/19 20:07
 */
@Configuration
@MapperScan(basePackages = "com.zheng.nie.shardingjdbc.mapper", sqlSessionTemplateRef  = "test1SqlSessionTemplate")
public class DataSourceConfig {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);


    /**
     * 配置数据源0，数据源的名称最好要有一定的规则，方便配置分库的计算规则
     * @return
     */
    @Bean(name="dataSource0")
    @ConfigurationProperties(prefix = "spring.datasource.test1")
    public DataSource dataSource0(){
        logger.error("dataSource0 is init");
        return DataSourceBuilder.create().build();
    }
    /**
     * 配置数据源1，数据源的名称最好要有一定的规则，方便配置分库的计算规则
     * @return
     */
    @Bean(name="dataSource1")
    @ConfigurationProperties(prefix = "spring.datasource.test2")
    public DataSource dataSource1(){
        logger.error("dataSource1 is init");
        return DataSourceBuilder.create().build();
    }

    /**
     * 配置数据源规则，即将多个数据源交给sharding-jdbc管理，并且可以设置默认的数据源，
     * 当表没有配置分库规则时会使用默认的数据源
     * @param dataSource0
     * @param dataSource1
     * @return
     */
    @Bean(name = "dataSourceRule")
    public DataSourceRule dataSourceRule(@Qualifier("dataSource0") DataSource dataSource0,
                                         @Qualifier("dataSource1") DataSource dataSource1){
        /**
         * 设置数据库源
         */
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("dataSource0", dataSource0);
        dataSourceMap.put("dataSource1", dataSource1);
        /**设置默认库，两个库以上时必须设置默认库，默认库的数据源名称必须是dataSourceMap 的key之一**/
        DataSourceRule dataSourceRule = new DataSourceRule(dataSourceMap,"dataSource0");
        return dataSourceRule;
    }

    /**
     * 配置数据源策略和表策略，具体策略需要自己实现
     * @param dataSourceRule
     * @return
     */
    @Bean(name = "shardingRule")
    public ShardingRule shardingRule(@Qualifier("dataSourceRule") DataSourceRule dataSourceRule){
        //具体分库分表策略
        TableRule orderTableRule = TableRule
                //逻辑表明
                .builder("t_order")
                //所有物理表名称
                .actualTables(Arrays.asList("t_order_0", "t_order_1"))
                //分表策略ID
                .tableShardingStrategy(new TableShardingStrategy("order_id", new ModuloTableShardingAlgorithm()))
                .dataSourceRule(dataSourceRule)
                .build();

        //绑定表策略，在查询时会使用主表策略计算路由的数据源，因此需要约定绑定表策略的表的规则需要一致，可以一定程度提高效率
        List<BindingTableRule> bindingTableRules = new ArrayList<BindingTableRule>();
        bindingTableRules.add(new BindingTableRule(Arrays.asList(orderTableRule)));
        return ShardingRule.builder()
                .dataSourceRule(dataSourceRule)
                .tableRules(Arrays.asList(orderTableRule))
                .bindingTableRules(bindingTableRules)
                //分库策略
                .databaseShardingStrategy(new DatabaseShardingStrategy("user_id", new ModuloDatabaseShardingAlgorithm()))
                //分表策略
                .tableShardingStrategy(new TableShardingStrategy("order_id", new ModuloTableShardingAlgorithm()))
                .build();
    }

    /**
     * 创建sharding-jdbc的数据源DataSource，MybatisAutoConfiguration会使用此数据源
     * @param shardingRule
     * @return
     * @throws SQLException
     */
    @Bean(name="dataSource")
    public DataSource shardingDataSource(@Qualifier("shardingRule") ShardingRule shardingRule) throws SQLException {
        return ShardingDataSourceFactory.createDataSource(shardingRule);
    }

    /**
     * 需要手动配置事务管理器
     * @param dataSource
     * @return
     */
    @Bean
    public DataSourceTransactionManager transactitonManager(@Qualifier("dataSource") DataSource dataSource){
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "test1SqlSessionFactory")
    @Primary
    public SqlSessionFactory testSqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mybatis/mapper/*.xml"));
        return bean.getObject();
    }

    @Bean(name = "test1SqlSessionTemplate")
    @Primary
    public SqlSessionTemplate testSqlSessionTemplate(@Qualifier("test1SqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}


