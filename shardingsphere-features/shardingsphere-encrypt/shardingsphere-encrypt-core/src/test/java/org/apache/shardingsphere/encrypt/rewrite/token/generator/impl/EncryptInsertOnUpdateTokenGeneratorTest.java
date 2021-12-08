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

import org.apache.shardingsphere.encrypt.rewrite.token.pojo.EncryptAssignmentToken;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptInsertOnUpdateTokenGeneratorTest {

    @InjectMocks
    private EncryptInsertOnUpdateTokenGenerator tokenGenerator;

    /**
     * test isGenerateSQLTokenForEncrypt with insert statement context.
     */
    @Test
    public void isGenerateSQLTokenForEncrypt() {
        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final MySQLInsertStatement insertStatement = mock(MySQLInsertStatement.class);
        final OnDuplicateKeyColumnsSegment columnsSegment = mock(OnDuplicateKeyColumnsSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getOnDuplicateKeyColumns()).thenReturn(Optional.of(columnsSegment));

        final boolean actual = tokenGenerator.isGenerateSQLTokenForEncrypt(insertStatementContext);
        assertTrue(actual);
    }

    /**
     * test insert on update values token generator for encrypt with duplicate columns.
     */
    @Test
    public void generateSQLTokensWithDuplicateColumnSegmentTest() {

        IdentifierValue idf = new IdentifierValue("table1");
        IdentifierValue idfc = new IdentifierValue("col");

        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final MySQLInsertStatement insertStatement = mock(MySQLInsertStatement.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment = mock(OnDuplicateKeyColumnsSegment.class);
        final AssignmentSegment assignmentSegment = mock(AssignmentSegment.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);
        final LiteralExpressionSegment literalExpressionSegment = mock(LiteralExpressionSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getTable()).thenReturn(simpleTableSegment);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(idf);
        when(insertStatement.getOnDuplicateKeyColumns()).thenReturn(Optional.of(onDuplicateKeyColumnsSegment));
        when(onDuplicateKeyColumnsSegment.getColumns()).thenReturn(Collections.singletonList(assignmentSegment));
        when(assignmentSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
        when(assignmentSegment.getValue()).thenReturn(literalExpressionSegment);
        when(literalExpressionSegment.getLiterals()).thenReturn(new Object());
        when(columnSegment.getIdentifier()).thenReturn(idfc);
        when(insertStatementContext.getSchemaName()).thenReturn("schema1");
        when(encryptRule.findEncryptor(anyString(), anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));
        when(encryptRule.getCipherColumn(anyString(), anyString())).thenReturn("cCol");
        when(encryptRule.getEncryptValues(anyString(), anyString(), anyString(), anyList())).thenReturn(Collections.singletonList(new Object()));
        when(encryptRule.findAssistedQueryColumn(anyString(), anyString())).thenReturn(Optional.of("aqCol"));
        when(encryptRule.getEncryptAssistedQueryValues(anyString(), anyString(), anyString(), anyList())).thenReturn(Collections.singletonList(new Object()));
        when(encryptRule.findPlainColumn(anyString(), anyString())).thenReturn(Optional.of("pCol"));

        tokenGenerator.setEncryptRule(encryptRule);

        final Collection<EncryptAssignmentToken> actualResult = tokenGenerator.generateSQLTokens(insertStatementContext);
        assertNotNull(actualResult);
        assertEquals(1, actualResult.size());
    }

    /**
     * test insert on update values token generator for encrypt without duplicate columns.
     */
    @Test
    public void generateSQLTokensWithEmptyDuplicateColumnSegmentTest() {

        IdentifierValue idf = new IdentifierValue("table1");
        IdentifierValue idfc = new IdentifierValue("col");

        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final MySQLInsertStatement insertStatement = mock(MySQLInsertStatement.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment = mock(OnDuplicateKeyColumnsSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getTable()).thenReturn(simpleTableSegment);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(idf);
        when(insertStatement.getOnDuplicateKeyColumns()).thenReturn(Optional.of(onDuplicateKeyColumnsSegment));
        when(onDuplicateKeyColumnsSegment.getColumns()).thenReturn(Collections.emptyList());

        final Collection<EncryptAssignmentToken> actualResult = tokenGenerator.generateSQLTokens(insertStatementContext);
        assertNotNull(actualResult);
        assertEquals(0, actualResult.size());
    }
    
    /**
     * test insert on update values token generator for encrypt with parameter marker expressions.
     */
    @Test
    public void generateSQLTokensWithParameterMarkerExpressionSegmentTest() {

        IdentifierValue idf = new IdentifierValue("table1");
        IdentifierValue idfc = new IdentifierValue("col");

        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final MySQLInsertStatement insertStatement = mock(MySQLInsertStatement.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment = mock(OnDuplicateKeyColumnsSegment.class);
        final AssignmentSegment assignmentSegment = mock(AssignmentSegment.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);
        final ParameterMarkerExpressionSegment parameterMarkerExpressionSegment = mock(ParameterMarkerExpressionSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getTable()).thenReturn(simpleTableSegment);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(idf);
        when(insertStatement.getOnDuplicateKeyColumns()).thenReturn(Optional.of(onDuplicateKeyColumnsSegment));
        when(onDuplicateKeyColumnsSegment.getColumns()).thenReturn(Collections.singletonList(assignmentSegment));
        when(assignmentSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
        when(assignmentSegment.getValue()).thenReturn(parameterMarkerExpressionSegment);
        when(columnSegment.getIdentifier()).thenReturn(idfc);
        when(insertStatementContext.getSchemaName()).thenReturn("schema1");
        when(encryptRule.findEncryptor(anyString(), anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));
        when(encryptRule.getCipherColumn(anyString(), anyString())).thenReturn("cCol");
        when(encryptRule.findAssistedQueryColumn(anyString(), anyString())).thenReturn(Optional.of("aqCol"));

        tokenGenerator.setEncryptRule(encryptRule);

        final Collection<EncryptAssignmentToken> actualResult = tokenGenerator.generateSQLTokens(insertStatementContext);
        assertNotNull(actualResult);
        assertEquals(1, actualResult.size());
    }

    /**
     * test insert on update values token generator for encrypt without parameter marker expressions.
     */
    @Test
    public void generateSQLTokensWithoutParameterMarkerExpressionSegmentAndLiteralExpressionSegmentTest() {

        IdentifierValue idf = new IdentifierValue("table1");
        IdentifierValue idfc = new IdentifierValue("col");

        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final MySQLInsertStatement insertStatement = mock(MySQLInsertStatement.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment = mock(OnDuplicateKeyColumnsSegment.class);
        final AssignmentSegment assignmentSegment = mock(AssignmentSegment.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);
        final SimpleExpressionSegment simpleExpressionSegment = mock(SimpleExpressionSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getTable()).thenReturn(simpleTableSegment);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(idf);
        when(insertStatement.getOnDuplicateKeyColumns()).thenReturn(Optional.of(onDuplicateKeyColumnsSegment));
        when(onDuplicateKeyColumnsSegment.getColumns()).thenReturn(Collections.singletonList(assignmentSegment));
        when(assignmentSegment.getColumns()).thenReturn(Collections.singletonList(columnSegment));
        when(assignmentSegment.getValue()).thenReturn(simpleExpressionSegment);
        when(columnSegment.getIdentifier()).thenReturn(idfc);
        when(insertStatementContext.getSchemaName()).thenReturn("schema1");
        when(encryptRule.findEncryptor(anyString(), anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));

        tokenGenerator.setEncryptRule(encryptRule);

        final Collection<EncryptAssignmentToken> actualResult = tokenGenerator.generateSQLTokens(insertStatementContext);
        assertNotNull(actualResult);
        assertEquals(0, actualResult.size());
    }
}
