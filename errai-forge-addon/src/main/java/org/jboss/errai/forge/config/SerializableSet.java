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

package org.jboss.errai.forge.config;

import java.util.HashSet;
import java.util.Iterator;

/**
 * For serializing and deserializing sets of simple string.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class SerializableSet extends HashSet<String> {

  private static final long serialVersionUID = -4968825975745239833L;
  public static final String delimeter = ",";

  /**
   * @return The serialized {@link String} representation of this set. Note that
   *         ',' is used to separate values, but values are <b>not</b> escaped.
   */
  public String serialize() {
    final StringBuilder builder = new StringBuilder();

    final Iterator<String> iterator = iterator();
    if (iterator.hasNext())
      builder.append(iterator.next());

    while (iterator.hasNext()) {
      builder.append(delimeter).append(iterator.next());
    }

    return builder.toString();
  }

  /**
   * Deserialize the given {@link String} into a new {@link SerializableSet}.
   * 
   * @param serialized
   *          A {@link String} generated by a call to
   *          {@link SerializableSet#serialize() serialize}.
   * @return A new {@link SerializableSet}.
   */
  public static SerializableSet deserialize(final String serialized) {
    final SerializableSet set = new SerializableSet();
    final String[] items = serialized.replaceAll("\\[|\\]", "").split(delimeter);

    for (int i = 0; i < items.length; i++) {
      if (!items[i].equals(""))
        set.add(items[i].trim());
    }

    return set;
  }

}
