/*
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
package com.gemstone.gemfire.test.dunit.cache.internal;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

import com.gemstone.gemfire.cache.AttributesFactory;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheException;
import com.gemstone.gemfire.cache.CacheExistsException;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.CacheTransactionManager;
import com.gemstone.gemfire.cache.ExpirationAttributes;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionAttributes;
import com.gemstone.gemfire.cache.RegionExistsException;
import com.gemstone.gemfire.cache.TimeoutException;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.PoolManager;
import com.gemstone.gemfire.cache30.CacheSerializableRunnable;
import com.gemstone.gemfire.distributed.internal.DistributionMessageObserver;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.internal.FileUtil;
import com.gemstone.gemfire.internal.cache.GemFireCacheImpl;
import com.gemstone.gemfire.internal.cache.InternalRegionArguments;
import com.gemstone.gemfire.internal.cache.LocalRegion;
import com.gemstone.gemfire.internal.cache.xmlcache.CacheCreation;
import com.gemstone.gemfire.internal.cache.xmlcache.CacheXmlGenerator;
import com.gemstone.gemfire.internal.logging.LogService;
import com.gemstone.gemfire.test.dunit.Assert;
import com.gemstone.gemfire.test.dunit.DistributedTestCase;
import com.gemstone.gemfire.test.dunit.Host;
import com.gemstone.gemfire.test.dunit.IgnoredException;
import com.gemstone.gemfire.test.dunit.Invoke;
import com.gemstone.gemfire.test.dunit.LogWriterUtils;
import com.gemstone.gemfire.test.dunit.VM;
import com.gemstone.gemfire.test.dunit.Wait;
import com.gemstone.gemfire.test.dunit.WaitCriterion;
import com.gemstone.gemfire.test.dunit.internal.JUnit3DistributedTestCase;
import com.gemstone.gemfire.test.dunit.internal.JUnit4DistributedTestCase;
import org.apache.logging.log4j.Logger;

/**
 * The abstract superclass of tests that require the creation of a
 * {@link Cache}.
 */
public abstract class JUnit3CacheTestCase extends JUnit3DistributedTestCase implements CacheTestFixture {

  private final JUnit4CacheTestCase delegate = new JUnit4CacheTestCase(this);

  public JUnit3CacheTestCase(final String name) {
    super(name);
  }

  /**
   * Creates the <code>Cache</code> for this test that is not connected
   * to other members
   */
  public final Cache createLonerCache() {
    return delegate.createLonerCache();
  }

  /**
   * Sets this test up with a CacheCreation as its cache.
   * Any existing cache is closed. Whoever calls this must also call finishCacheXml
   */
  public static final synchronized void beginCacheXml() {
    JUnit4CacheTestCase.beginCacheXml();
  }

  /**
   * Finish what beginCacheXml started. It does this be generating a cache.xml
   * file and then creating a real cache using that cache.xml.
   */
  public final void finishCacheXml(final String name) {
    delegate.finishCacheXml(name);
  }

  /**
   * Finish what beginCacheXml started. It does this be generating a cache.xml
   * file and then creating a real cache using that cache.xml.
   */
  public final void finishCacheXml(final String name, final boolean useSchema, final String xmlVersion) {
    delegate.finishCacheXml(name, useSchema, xmlVersion);
  }

  /**
   * Return a cache for obtaining regions, created lazily.
   */
  public final Cache getCache() {
    return delegate.getCache();
  }

  public final Cache getCache(final CacheFactory factory) {
    return delegate.getCache(factory);
  }

  public final Cache getCache(final boolean client) {
    return delegate.getCache(client);
  }

  public final Cache getCache(final boolean client, final CacheFactory factory) {
    return delegate.getCache(client, factory);
  }

  /**
   * creates a client cache from the factory if one does not already exist
   * @since 6.5
   */
  public final ClientCache getClientCache(final ClientCacheFactory factory) {
    return delegate.getClientCache(factory);
  }

  /**
   * same as {@link #getCache()} but with casting
   */
  public final GemFireCacheImpl getGemfireCache() { // TODO: remove?
    return delegate.getGemfireCache();
  }

  public static synchronized final boolean hasCache() {
    return JUnit4CacheTestCase.hasCache();
  }

  /**
   * Return current cache without creating one.
   */
  public static synchronized final Cache basicGetCache() {
    return JUnit4CacheTestCase.basicGetCache();
  }

//  public static synchronized final void disconnectFromDS() {
//    JUnit4CacheTestCase.disconnectFromDS();
//  }

  /** Close the cache */
  public static synchronized final void closeCache() {
    JUnit4CacheTestCase.closeCache();
  }

  /** Closed the cache in all VMs. */
  protected final void closeAllCache() {
    delegate.closeAllCache();
  }

  @Override
  public final void preTearDown() throws Exception {
    delegate.preTearDown();
  }

  @Override
  public void preTearDownCacheTestCase() throws Exception {
  }

  @Override
  public void postTearDownCacheTestCase() throws Exception {
  }

  /**
   * Local destroy all root regions and close the cache.
   */
  protected final synchronized static void remoteTearDown() {
    JUnit4CacheTestCase.remoteTearDown();
  }

  /**
   * Returns a region with the given name and attributes
   */
  public final Region createRegion(final String name, final RegionAttributes attributes) throws CacheException {
    return delegate.createRegion(name, attributes);
  }

  public final Region createRegion(final String name, final String rootName, final RegionAttributes attributes) throws CacheException {
    return delegate.createRegion(name, rootName, attributes);
  }

  public final Region getRootRegion() {
    return delegate.getRootRegion();
  }

  public final Region getRootRegion(final String rootName) {
    return delegate.getRootRegion(rootName);
  }

  protected final Region createRootRegion(final RegionAttributes attributes) throws RegionExistsException, TimeoutException {
    return delegate.createRootRegion(attributes);
  }

  public final Region createRootRegion(final String rootName, final RegionAttributes attributes) throws RegionExistsException, TimeoutException {
    return delegate.createRootRegion(rootName, attributes);
  }

  public final Region createExpiryRootRegion(final String rootName, final RegionAttributes attributes) throws RegionExistsException, TimeoutException {
    return delegate.createExpiryRootRegion(rootName, attributes);
  }

  /**
   * @deprecated Please use {@link IgnoredException#addIgnoredException(String)} instead.
   */
  @Deprecated
  public CacheSerializableRunnable addExceptionTag1(final String exceptionStringToIgnore) {
    return delegate.addExceptionTag1(exceptionStringToIgnore);
  }

  /**
   * @deprecated Please use {@link IgnoredException#remove()} instead.
   */
  @Deprecated
  public CacheSerializableRunnable removeExceptionTag1(final String exceptionStringToIgnore) {
    return delegate.removeExceptionTag1(exceptionStringToIgnore);
  }

  public static final File getDiskDir() {
    return JUnit4CacheTestCase.getDiskDir();
  }

  /**
   * Return a set of disk directories
   * for persistence tests. These directories
   * will be automatically cleaned up
   * on test case closure.
   */
  public static final File[] getDiskDirs() {
    return JUnit4CacheTestCase.getDiskDirs();
  }

  public static final void cleanDiskDirs() throws IOException {
    JUnit4CacheTestCase.cleanDiskDirs();
  }
}
