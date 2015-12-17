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

package org.jboss.errai.processor.testcase;

import org.jboss.errai.databinding.client.api.Bindable;

@Bindable
public class BoundModelClass {

  private String property1;
  private String property2;
  private String property3;
  private String property4;
  private String property5;
  private BoundModelClass property6;

  public String getProperty1() {
    return property1;
  }
  public void setProperty1(String property1) {
    this.property1 = property1;
  }
  public String getProperty2() {
    return property2;
  }
  public void setProperty2(String property2) {
    this.property2 = property2;
  }
  public String getProperty3() {
    return property3;
  }
  public void setProperty3(String property3) {
    this.property3 = property3;
  }
  public String getProperty4() {
    return property4;
  }
  public void setProperty4(String property4) {
    this.property4 = property4;
  }
  public String getProperty5() {
    return property5;
  }
  public void setProperty5(String property5) {
    this.property5 = property5;
  }
  public BoundModelClass getProperty6() {
    return property6;
  }
  public void setProperty6(BoundModelClass property6) {
    this.property6 = property6;
  }
}
