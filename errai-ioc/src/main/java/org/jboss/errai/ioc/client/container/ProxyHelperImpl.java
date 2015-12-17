/**
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.client.container;

/**
 * @see ProxyHelper
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ProxyHelperImpl<T> implements ProxyHelper<T> {

  private final String factoryName;

  private Context context;
  private T instance;

  public ProxyHelperImpl(final String factoryName) {
    this.factoryName = factoryName;
  }

  @Override
  public void setInstance(final T instance) {
    this.instance = instance;
  }

  @Override
  public T getInstance(final Proxy<T> proxy) {
    if (instance == null) {
      trySettingInstance();
      proxy.initProxyProperties(instance);
    }

    return instance;
  }

  private void trySettingInstance() {
    assertContextIsSet();

    if (context.isActive()) {
      instance = context.getActiveNonProxiedInstance(factoryName);
    } else {
      throw new RuntimeException("Cannot invoke method on bean from inactive " + context.getScope().getSimpleName() + " context.");
    }
  }

  @Override
  public void clearInstance() {
    instance = null;
  }

  @Override
  public void setContext(final Context context) {
    if (this.context != null) {
      throw new RuntimeException("Context can only be set once.");
    }

    this.context = context;
  }

  @Override
  public Context getContext() {
    assertContextIsSet();

    return context;
  }

  private void assertContextIsSet() {
    if (context == null) {
      throw new RuntimeException("Context has not yet been set.");
    }
  }

}
