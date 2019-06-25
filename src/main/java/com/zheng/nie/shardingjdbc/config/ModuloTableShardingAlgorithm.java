package com.zheng.nie.shardingjdbc.config;

import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.SingleKeyTableShardingAlgorithm;
import com.google.common.collect.Range;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * @author: niezheng1
 * @Date: 2018/11/19 20:10
 */
@Component
public class ModuloTableShardingAlgorithm implements SingleKeyTableShardingAlgorithm<Long> {

    /**
     * 如果SQL中分表列order_id条件为where order_id=?，那么shardingValue的type为SINGLE，分表逻辑走doEqualSharding()；
     *   // 分析前提，假设预期分到两个表中[t_order_0,t_order_1]，
     *   且执行的SQL为SELECT o.* FROM t_order o where o.order_id=1001 AND o.user_id=10，那么分表列order_id的值为1001
     * @param tableNames
     * @param shardingValue
     * @return
     */
    @Override
    public String doEqualSharding(Collection<String> tableNames, ShardingValue<Long> shardingValue) {
        for (String each : tableNames) {
            if (each.endsWith(shardingValue.getValue() % 2 + "")) {
                return each;
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     *   // 从这里可知，doInSharding()和doEqualSharding()的区别就是doInSharding()时分表列有多个值（shardingValue.getValues()），
     *   例如order_id的值为[1001,1002]，遍历这些值，然后每个值按照doEqualSharding()的逻辑计算表名
     * @param tableNames
     * @param shardingValue
     * @return
     */
    @Override
    public Collection<String> doInSharding(Collection<String> tableNames, ShardingValue<Long> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(tableNames.size());
        for (Long value : shardingValue.getValues()) {
            for (String tableName : tableNames) {
                if (tableName.endsWith(value % 2 + "")) {
                    result.add(tableName);
                }
            }
        }
        return result;
    }

    /**
     *   // 从这里可知，doBetweenSharding()和doInSharding()的区别就是doBetweenSharding()时分表列的多个值通过shardingValue.getValueRange()得到；
     *   而doInSharding()是通过shardingValue.getValues()得到；
     * @param tableNames
     * @param shardingValue
     * @return
     */
    @Override
    public Collection<String> doBetweenSharding(Collection<String> tableNames, ShardingValue<Long> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(tableNames.size());
        Range<Long> range = (Range<Long>) shardingValue.getValueRange();
        for (Long i = range.lowerEndpoint(); i <= range.upperEndpoint(); i++) {
            for (String each : tableNames) {
                if (each.endsWith(i % 2 + "")) {
                    result.add(each);
                }
            }
        }
        return result;
    }
}

