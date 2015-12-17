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

package org.jboss.errai.ui.nav.client.local;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.nav.client.local.testpages.PageWithTransitionAnchor;

import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point for the transition anchor test.  Needed so that the transition
 * anchor gets attached and thus its href is set.
 *
 * @author eric.wittmann@redhat.com
 */
@EntryPoint
public class TransitionAnchorTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private PageWithTransitionAnchor page;

  @PostConstruct
  public void setup() {
    root.add(page);
  }

  public PageWithTransitionAnchor getPage() {
    return page;
  }

}
