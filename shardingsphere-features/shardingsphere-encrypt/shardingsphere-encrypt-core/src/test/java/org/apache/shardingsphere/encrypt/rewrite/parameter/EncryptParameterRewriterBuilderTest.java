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

package org.apache.shardingsphere.encrypt.rewrite.parameter;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class EncryptParameterRewriterBuilderTest {
    
    @Test
    public void getParameterReWritersTest() {
        ShardingSphereSchema shardingSphereSchema = mock(ShardingSphereSchema.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);

        EncryptParameterRewriterBuilder encryptParameterRewriterBuilder = new EncryptParameterRewriterBuilder(encryptRule, true);

        final Collection<ParameterRewriter> parameterReWriters = encryptParameterRewriterBuilder.getParameterRewriters(shardingSphereSchema);
        assertEquals(4, parameterReWriters.size());
    }
}
