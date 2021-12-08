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
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.InsertColumnsToken;
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
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssistQueryAndPlainInsertColumnsTokenGeneratorTest {

    @InjectMocks
    private AssistQueryAndPlainInsertColumnsTokenGenerator tokenGenerator;

    /**
     * test isGenerateSQLTokenForEncrypt method for insert statement context.
     */
    @Test
    public void isGenerateSQLTokenForEncryptUsingInsertTest() {
        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final InsertStatement insertStatement = mock(InsertStatement.class);
        final InsertColumnsSegment insertColumnsSegment = mock(InsertColumnsSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getInsertColumns()).thenReturn(Optional.of(insertColumnsSegment));
        when(insertStatementContext.useDefaultColumns()).thenReturn(false);

        final boolean actualValue = tokenGenerator.isGenerateSQLTokenForEncrypt(insertStatementContext);
        assertTrue(actualValue);
    }

    /**
     * test isGenerateSQLTokenForEncrypt method for update statement context.
     */
    @Test
    public void isGenerateSQLTokenForEncryptUsingUpdateTest() {
        final UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class);

        final boolean actualValue = tokenGenerator.isGenerateSQLTokenForEncrypt(updateStatementContext);
        assertFalse(actualValue);
    }

    /**
     * Assist query and plain insert columns token generator.
     */
    @Test
    public void generateSQLTokensTest() {
        IdentifierValue idf = new IdentifierValue("table1");
        IdentifierValue idfc = new IdentifierValue("col1");

        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final InsertStatement insertStatement = mock(InsertStatement.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final EncryptTable encryptTable = mock(EncryptTable.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getTable()).thenReturn(simpleTableSegment);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(idf);
        when(encryptRule.findEncryptTable(anyString())).thenReturn(Optional.of(encryptTable));
        when(insertStatement.getColumns()).thenReturn(Collections.singletonList(columnSegment));
        when(columnSegment.getIdentifier()).thenReturn(idfc);
        when(encryptTable.findAssistedQueryColumn(anyString())).thenReturn(Optional.of("aqColumn"));
        when(encryptTable.findPlainColumn(anyString())).thenReturn(Optional.of("fpColumn"));

        tokenGenerator.setEncryptRule(encryptRule);

        final Collection<InsertColumnsToken> tokens = tokenGenerator.generateSQLTokens(insertStatementContext);
        assertNotNull(tokens);
        assertEquals(1, tokens.size());
    }
}
