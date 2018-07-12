/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy;

import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.util.EventBusInstance;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.frontend.ShardingProxy;
import io.shardingsphere.proxy.yaml.YamlProxyConfiguration;
import io.shardingsphere.transaction.xa.XaTransactionListener;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Sharding-Proxy Bootstrap.
 *
 * @author zhangliang
 * @author wangkai
 * @author panjuan
 */
public final class Bootstrap {
    
    private static final int DEFAULT_PORT = 3307;
    
    private static final String CONFIG_YAML = "/conf/config.yaml";
    
    /**
     * Main Entrance.
     * 
     * @param args startup arguments
     * @throws InterruptedException interrupted exception
     * @throws MalformedURLException URL exception
     */
    public static void main(final String[] args) throws InterruptedException, MalformedURLException {
        initializeRuleRegistry();
        EventBusInstance.getInstance().register(new XaTransactionListener());
        new ShardingProxy().start(getPort(args));
    }
    
    private static int getPort(final String[] args) {
        if (0 == args.length) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(args[0]);
        } catch (final NumberFormatException ex) {
            return DEFAULT_PORT;
        }
    }
    
    private static void initializeRuleRegistry() {
        YamlProxyConfiguration yamlProxyConfig;
        try {
            yamlProxyConfig = YamlProxyConfiguration.unmarshal(new File(Bootstrap.class.getResource(CONFIG_YAML).getFile()));
            yamlProxyConfig.init();
        } catch (final IOException ex) {
            throw new ShardingException(ex);
        }
        RuleRegistry.getInstance().init(yamlProxyConfig);
    }
}
