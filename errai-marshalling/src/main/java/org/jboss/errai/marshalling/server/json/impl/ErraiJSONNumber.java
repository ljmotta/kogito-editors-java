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

package org.jboss.errai.marshalling.server.json.impl;

import org.jboss.errai.marshalling.client.api.json.EJNumber;

/**
 * @author Mike Brock
 */
public class ErraiJSONNumber implements EJNumber {
  private final Number number;

  public ErraiJSONNumber(final Number number) {
    this.number = number;
  }

  @Override
  public double doubleValue() {
    return number.doubleValue();
  }

  @Override
  public int intValue() {
    return number.intValue();
  }

  @Override
  public short shortValue() {
    return number.shortValue();
  }

  @Override
  public byte byteValue() {
    return number.byteValue();
  }

  @Override
  public float floatValue() {
    return number.floatValue();
  }
}
