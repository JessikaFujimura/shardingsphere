/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sqltranslator.rule;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqltranslator.api.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.exception.SQLTranslationException;
import org.apache.shardingsphere.sqltranslator.spi.SQLTranslator;

/**
 * SQL translator rule.
 */
public final class SQLTranslatorRule implements GlobalRule {
    
    @Getter
    private final SQLTranslatorRuleConfiguration configuration;
    
    private final SQLTranslator translator;
    
    private final boolean useOriginalSQLWhenTranslatingFailed;
    
    public SQLTranslatorRule(final SQLTranslatorRuleConfiguration ruleConfig) {
        configuration = ruleConfig;
        translator = TypedSPILoader.getService(SQLTranslator.class, ruleConfig.getType());
        useOriginalSQLWhenTranslatingFailed = ruleConfig.isUseOriginalSQLWhenTranslatingFailed();
    }
    
    /**
     * Translate SQL.
     * 
     * @param sql to be translated SQL
     * @param sqlStatementContext SQL statement context
     * @param storageType storage type
     * @param database database
     * @param globalRuleMetaData global rule meta data
     * @return translated SQL
     */
    public String translate(final String sql, final SQLStatementContext sqlStatementContext, final DatabaseType storageType, final ShardingSphereDatabase database,
                            final RuleMetaData globalRuleMetaData) {
        DatabaseType protocolType = database.getProtocolType();
        if (protocolType.equals(storageType) || null == storageType) {
            return sql;
        }
        try {
            return translator.translate(sql, sqlStatementContext, storageType, database, globalRuleMetaData);
        } catch (final SQLTranslationException ex) {
            if (useOriginalSQLWhenTranslatingFailed) {
                return sql;
            }
            throw ex;
        }
    }
}
