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

package org.jboss.errai.security.server;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.client.shared.AdminMethodSharedImplService;
import org.jboss.errai.security.client.shared.UserMethodSharedImplService;

@Service
public class MethodSharedImpl implements UserMethodSharedImplService, AdminMethodSharedImplService {

  @Override
  public void someAdminService() {
  }

  @Override
  public void someUserService() {
  }

}
