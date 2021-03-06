/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.infinispan.processor.idempotent;

import org.infinispan.commons.api.BasicCache;
import org.infinispan.commons.api.BasicCacheContainer;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.jgroups.util.Util.assertFalse;
import static org.jgroups.util.Util.assertTrue;

public class InfinispanIdempotentRepositoryTest {

    public static final GlobalConfiguration GLOBAL_CONFIGURATION = new GlobalConfigurationBuilder().globalJmxStatistics().allowDuplicateDomains(true).build();

    protected BasicCacheContainer basicCacheContainer;
    protected InfinispanIdempotentRepository idempotentRepository;
    protected String cacheName = "test";

    @Before
    public void setUp() throws Exception {
        basicCacheContainer = new DefaultCacheManager(GLOBAL_CONFIGURATION);
        basicCacheContainer.start();
        idempotentRepository = InfinispanIdempotentRepository.infinispanIdempotentRepository(basicCacheContainer, cacheName);
    }

    @After
    public void tearDown() throws Exception {
        basicCacheContainer.stop();
    }

    @Test
    public void addsNewKeysToCache() throws Exception {
        assertTrue(idempotentRepository.add("One"));
        assertTrue(idempotentRepository.add("Two"));

        assertTrue(getCache().containsKey("One"));
        assertTrue(getCache().containsKey("Two"));
    }

    @Test
    public void skipsAddingSecondTimeTheSameKey() throws Exception {
        assertTrue(idempotentRepository.add("One"));
        assertFalse(idempotentRepository.add("One"));
    }

    @Test
    public void containsPreviouslyAddedKey() throws Exception {
        assertFalse(idempotentRepository.contains("One"));

        idempotentRepository.add("One");

        assertTrue(idempotentRepository.contains("One"));
    }

    @Test
    public void removesAnExistingKey() throws Exception {
        idempotentRepository.add("One");

        assertTrue(idempotentRepository.remove("One"));

        assertFalse(idempotentRepository.contains("One"));
    }

    @Test
    public void doesntRemoveMissingKey() throws Exception {
        assertFalse(idempotentRepository.remove("One"));
    }

    private BasicCache<Object, Object> getCache() {
        return basicCacheContainer.getCache(cacheName);
    }
}
