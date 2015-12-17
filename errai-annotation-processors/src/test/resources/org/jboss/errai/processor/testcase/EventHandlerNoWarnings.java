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

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.SinkNative;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.dom.client.Element;

@Templated
public class EventHandlerNoWarnings extends Composite {

  @DataField TextBox myTextBox;
  
  @DataField Element someBasicElement;

  /** Correct usage for events on a child widget: event handler refers to existing field in this class. */
  @EventHandler("myTextBox")
  void onKeyPressed(KeyPressEvent e) {
    // no op
  }

  /** Correct usage for events on self: event handler defaults to this templated widget. */
  @EventHandler
  void onKeyPressedOnSelf(KeyPressEvent e) {
    // no op
  }

  /** Correct usage of SinkNative: event handler refers to existing element in template. */
  @SinkNative(Event.ONMOUSEOVER)
  @EventHandler("image-with-sinknative")
  void onMouseOver(Event e) {
    // no op
  }
  
  /** Correct usage of SinkNative: event handler refers to existing element in template. */
  @EventHandler("someBasicElement")
  void onBasicElementClick(ClickEvent e) {
    // no op
  }
}
