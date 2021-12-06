package org.apache.shardingsphere.encrypt.rewrite.token.generator.impl;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptPredicateRightValueTokenGeneratorTest {

    @InjectMocks
    private EncryptPredicateRightValueTokenGenerator tokenGenerator;
    
    @Test
    public void isGenerateSQLTokenForEncrypt() {
        final UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class);
        final WhereSegment whereSegment = mock(WhereSegment.class);

        when(updateStatementContext.getWhere()).thenReturn(Optional.of(whereSegment));

        final boolean actual = tokenGenerator.isGenerateSQLTokenForEncrypt(updateStatementContext);
        assertTrue(actual);
    }
    
    @Test
    public void generateSQLTokensWithNoEncryptConditions() {
        final UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);

        tokenGenerator.setEncryptRule(encryptRule);

        final Collection<SQLToken> sqlTokens = tokenGenerator.generateSQLTokens(updateStatementContext);
        assertNotNull(sqlTokens);
        assertEquals(0, sqlTokens.size());
    }
    
    @Test
    public void generateSQLTokensWithEncryptConditions() {
        Map<String, String> map = new HashMap<>();
        map.put("col1", "table");
        IdentifierValue idf = new IdentifierValue("idf");

        final UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final WhereSegment whereSegment = mock(WhereSegment.class);
        final ShardingSphereSchema shardingSphereSchema = mock(ShardingSphereSchema.class);
        final BinaryOperationExpression binaryOperationExpression = mock(BinaryOperationExpression.class);
        final TablesContext tablesContext = mock(TablesContext.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);
        final LiteralExpressionSegment literalExpressionSegment = mock(LiteralExpressionSegment.class);
        final EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);

        when(updateStatementContext.getWhere()).thenReturn(Optional.of(whereSegment));
        when(whereSegment.getExpr()).thenReturn(binaryOperationExpression);
        when(updateStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(tablesContext.findTableName(anyList(), any())).thenReturn(map);
        when(binaryOperationExpression.getLeft()).thenReturn(columnSegment);
        when(columnSegment.getQualifiedName()).thenReturn("col1");
        when(columnSegment.getIdentifier()).thenReturn(idf);
        when(encryptRule.findEncryptor(anyString(), anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));
        when(updateStatementContext.getSchemaName()).thenReturn("schema");
        when(binaryOperationExpression.getOperator()).thenReturn("=");
        when(binaryOperationExpression.getRight()).thenReturn(literalExpressionSegment);
        when(literalExpressionSegment.getLiterals()).thenReturn(new Object());

        tokenGenerator.setEncryptRule(encryptRule);
        tokenGenerator.setSchema(shardingSphereSchema);

        final Collection<SQLToken> sqlTokens = tokenGenerator.generateSQLTokens(updateStatementContext);
        assertNotNull(sqlTokens);
        assertEquals(1, sqlTokens.size());
    }
    
    @Test
    public void generateSQLTokensWithEncryptConditionsAndCipherTest() {
        Map<String, String> map = new HashMap<>();
        map.put("col1", "table");
        IdentifierValue idf = new IdentifierValue("idf");

        final UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final WhereSegment whereSegment = mock(WhereSegment.class);
        final ShardingSphereSchema shardingSphereSchema = mock(ShardingSphereSchema.class);
        final BinaryOperationExpression binaryOperationExpression = mock(BinaryOperationExpression.class);
        final TablesContext tablesContext = mock(TablesContext.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);
        final LiteralExpressionSegment literalExpressionSegment = mock(LiteralExpressionSegment.class);
        final EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);

        when(updateStatementContext.getWhere()).thenReturn(Optional.of(whereSegment));
        when(whereSegment.getExpr()).thenReturn(binaryOperationExpression);
        when(updateStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(tablesContext.findTableName(anyList(), any())).thenReturn(map);
        when(binaryOperationExpression.getLeft()).thenReturn(columnSegment);
        when(columnSegment.getQualifiedName()).thenReturn("col1");
        when(columnSegment.getIdentifier()).thenReturn(idf);
        when(encryptRule.findEncryptor(anyString(), anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));
        when(updateStatementContext.getSchemaName()).thenReturn("schema");
        when(binaryOperationExpression.getOperator()).thenReturn("=");
        when(binaryOperationExpression.getRight()).thenReturn(literalExpressionSegment);
        when(literalExpressionSegment.getLiterals()).thenReturn(new Object());
        when(encryptRule.findAssistedQueryColumn(anyString(), anyString())).thenReturn(Optional.of("aCol"));
        when(encryptRule.getEncryptAssistedQueryValues(anyString(), anyString(), anyString(), anyList())).thenReturn(Collections.singletonList(new Object()));

        tokenGenerator.setEncryptRule(encryptRule);
        tokenGenerator.setSchema(shardingSphereSchema);
        tokenGenerator.setQueryWithCipherColumn(true);

        final Collection<SQLToken> sqlTokens = tokenGenerator.generateSQLTokens(updateStatementContext);
        assertNotNull(sqlTokens);
        assertEquals(1, sqlTokens.size());
    }
}