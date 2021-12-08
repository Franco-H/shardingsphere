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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.impl;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InsertCipherNameTokenGeneratorTest {

    @InjectMocks
    private InsertCipherNameTokenGenerator tokenGenerator;
    
    /**
     * test isGenerateSQLTokenForEncrypt with insert statement context.
     */
    @Test
    public void isGenerateSQLTokenForEncryptTest() {
        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final InsertStatement insertStatement = mock(InsertStatement.class);
        final InsertColumnsSegment insertColumnsSegment = mock(InsertColumnsSegment.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getInsertColumns()).thenReturn(Optional.of(insertColumnsSegment));
        when(insertColumnsSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));

        assertTrue(tokenGenerator.isGenerateSQLTokenForEncrypt(insertStatementContext));
    }
    
    /**
     * test insert cipher column name token generator for update context.
     */
    @Test
    public void isGenerateSQLTokenForEncryptForNonInsertTest() {
        final UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class);

        assertFalse(tokenGenerator.isGenerateSQLTokenForEncrypt(updateStatementContext));
    }

    /**
     * test insert cipher column name token generator for update context with logic and cipher columns.
     */
    @Test
    public void generateSQLTokensTest() {
        final IdentifierValue idf = new IdentifierValue("idf");
        final IdentifierValue idfc = new IdentifierValue("idfc");
        Map<String, String> map = new HashMap<>();
        map.put("idfc", "col1");

        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final InsertStatement insertStatement = mock(InsertStatement.class);
        final InsertColumnsSegment insertColumnsSegment = mock(InsertColumnsSegment.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final SimpleTableSegment tableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getInsertColumns()).thenReturn(Optional.of(insertColumnsSegment));
        when(insertColumnsSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
        when(insertStatement.getTable()).thenReturn(tableSegment);
        when(tableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(idf);
        when(encryptRule.getLogicAndCipherColumns(anyString())).thenReturn(map);
        when(columnSegment.getIdentifier()).thenReturn(idfc);

        tokenGenerator.setEncryptRule(encryptRule);

        final Collection<SubstitutableColumnNameToken> tokens = tokenGenerator.generateSQLTokens(insertStatementContext);
        assertNotNull(tokens);
        assertEquals(1, tokens.size());
    }
}
