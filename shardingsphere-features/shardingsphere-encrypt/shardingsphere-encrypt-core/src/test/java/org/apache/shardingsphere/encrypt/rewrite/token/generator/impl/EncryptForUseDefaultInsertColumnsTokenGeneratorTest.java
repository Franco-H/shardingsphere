package org.apache.shardingsphere.encrypt.rewrite.token.generator.impl;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.UseDefaultInsertColumnsToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptForUseDefaultInsertColumnsTokenGeneratorTest {

    @InjectMocks
    private EncryptForUseDefaultInsertColumnsTokenGenerator tokenGenerator;
    
    @Test
    public void isGenerateSQLTokenForEncryptTest() {
        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);

        when(insertStatementContext.useDefaultColumns()).thenReturn(true);

        final boolean actual = tokenGenerator.isGenerateSQLTokenForEncrypt(insertStatementContext);
        assertTrue(actual);
    }
    
    @Test
    public void generateSQLTokenWhenPreviousTokenArePresentTest() {
        IdentifierValue idf = new IdentifierValue("table1");

        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final InsertStatement insertStatement = mock(InsertStatement.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final UseDefaultInsertColumnsToken useDefaultInsertColumnsToken = mock(UseDefaultInsertColumnsToken.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final EncryptTable encryptTable = mock(EncryptTable.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getTable()).thenReturn(simpleTableSegment);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(idf);
        when(encryptRule.findEncryptTable(anyString())).thenReturn(Optional.of(encryptTable));
        when(insertStatementContext.getDescendingColumnNames()).thenReturn(Collections.singletonList("col1").iterator());
        when(encryptTable.findEncryptorName(anyString())).thenReturn(Optional.of("encName"));
        when(encryptTable.findPlainColumn(anyString())).thenReturn(Optional.of("pCol"));
        when(encryptTable.findAssistedQueryColumn(anyString())).thenReturn(Optional.of("aqCol"));
        when(encryptTable.getCipherColumn(anyString())).thenReturn("cCol");
        when(useDefaultInsertColumnsToken.getColumns()).thenReturn(new ArrayList<>(Arrays.asList("col1")));


        tokenGenerator.setPreviousSQLTokens(new ArrayList<>(Arrays.asList(useDefaultInsertColumnsToken)));
        tokenGenerator.setEncryptRule(encryptRule);

        final UseDefaultInsertColumnsToken token = tokenGenerator.generateSQLToken(insertStatementContext);
        assertNotNull(token);
        assertEquals(3, token.getColumns().size());
    }
    
    @Test
    public void generateSQLTokenWithoutPreviousTokenTest() {
        IdentifierValue idf = new IdentifierValue("table1");

        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        final InsertStatement insertStatement = mock(InsertStatement.class);
        final SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        final TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final EncryptTable encryptTable = mock(EncryptTable.class);
        final InsertColumnsSegment insertColumnsSegment = mock(InsertColumnsSegment.class);

        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatement.getTable()).thenReturn(simpleTableSegment);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        when(tableNameSegment.getIdentifier()).thenReturn(idf);
        when(encryptRule.findEncryptTable(anyString())).thenReturn(Optional.of(encryptTable));
        when(insertStatementContext.getDescendingColumnNames()).thenReturn(Collections.emptyIterator());
        when(insertStatement.getInsertColumns()).thenReturn(Optional.of(insertColumnsSegment));
        when(insertStatementContext.getColumnNames()).thenReturn(Collections.singletonList("col1"));


        tokenGenerator.setPreviousSQLTokens(new ArrayList<>());
        tokenGenerator.setEncryptRule(encryptRule);

        final UseDefaultInsertColumnsToken token = tokenGenerator.generateSQLToken(insertStatementContext);
        assertNotNull(token);
        assertEquals(1, token.getColumns().size());
    }
}