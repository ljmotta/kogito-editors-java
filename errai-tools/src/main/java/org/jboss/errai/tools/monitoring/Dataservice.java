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

package org.jboss.errai.tools.monitoring;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.util.BusToolsCli;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Class.forName;
import static java.lang.String.valueOf;
import static java.sql.DriverManager.getConnection;

public class Dataservice implements Attachable {
  Connection c;

  public Dataservice() {
    try {
      forName("org.hsqldb.jdbcDriver").newInstance();
      c = getConnection("jdbc:hsqldb:file:monitordb", "sa", "");
      createDB();
    }
    catch (Throwable t) {
      t.printStackTrace();
      throw new RuntimeException("error: " + t);
    }
  }

  private void createDB() throws SQLException {
    c.createStatement().execute("DROP TABLE MONITORDB IF EXISTS");

    c.createStatement().execute("CREATE CACHED TABLE MONITORDB ( " +
        "TM BIGINT NOT NULL, " +
        "EVENT_ID INT GENERATED BY DEFAULT AS IDENTITY (START WITH 0) PRIMARY KEY, " +
        "EVENT_TYPE INT NOT NULL," +
        "SUBEVENT_TYPE INT," +
        "BUS_ID VARCHAR(150) NOT NULL, " +
        "TO_BUS_ID VARCHAR(150) NOT NULL, " +
        "SERVICE_NAME VARCHAR(150) NOT NULL, " +
        "MESSAGE_OBJ OBJECT)");
  }

  public static class Record {
    private int eventId;
    private int eventType;
    private int subEventId;
    private long time;
    private String fromBus;
    private String toBus;
    private String service;
    private Object message;

    public Record(long time, int eventId, int eventType, int subEventId, String fromBus, String toBus, String service,
        Object message) {
      this.eventId = eventId;
      this.eventType = eventType;
      this.subEventId = subEventId;
      this.time = time;
      this.fromBus = fromBus;
      this.toBus = toBus;
      this.service = service;
      this.message = message;
    }

    public int getEventId() {
      return eventId;
    }

    public String getFromBus() {
      return fromBus;
    }

    public String getToBus() {
      return toBus;
    }

    public void setToBus(String toBus) {
      this.toBus = toBus;
    }

    public long getTime() {
      return time;
    }

    public void setTime(long time) {
      this.time = time;
    }

    public int getSubEventId() {
      return subEventId;
    }

    public void setSubEventId(int subEventId) {
      this.subEventId = subEventId;
    }

    public String getService() {
      return service;
    }

    public Object getMessage() {
      return message;
    }
  }

  public void storeRecord(long time, String fromBus, String toBus, String service, Message message) {
    try {
      PreparedStatement stmt =
          c.prepareStatement("INSERT INTO MONITORDB (EVENT_TYPE, TM, BUS_ID, TO_BUS_ID, SERVICE_NAME, MESSAGE_OBJ) VALUES (?, ?, ?, ?, ?, ?)");
      stmt.setInt(1, EventType.MESSAGE.ordinal());
      stmt.setLong(2, time);
      stmt.setString(3, fromBus);
      stmt.setString(4, toBus);
      stmt.setString(5, service);
      stmt.setString(6, BusToolsCli.encodeMessage(message));
      stmt.execute();
    }
    catch (Throwable e) {
      e.printStackTrace();
    }
  }

  public void storeBusEvent(long time, SubEventType subEventType, String fromBus, String toBus, String service,
      Object message) {
    try {
      PreparedStatement stmt =
          c.prepareStatement("INSERT INTO MONITORDB (EVENT_TYPE, SUBEVENT_TYPE, TM, BUS_ID, TO_BUS_ID, SERVICE_NAME, MESSAGE_OBJ) VALUES (?, ?, ?, ?, ?, ?, ?)");
      stmt.setInt(1, EventType.BUS_EVENT.ordinal());
      stmt.setInt(2, subEventType.ordinal());
      stmt.setLong(3, time);
      stmt.setString(4, fromBus);
      stmt.setString(5, toBus);
      stmt.setString(6, service == null ? "N/A" : service);
      stmt.setObject(7, message);
      stmt.execute();
    }
    catch (Throwable e) {
      throw new RuntimeException("error", e);
    }
  }

  public void storeError(long time, String busId, String service, Throwable error) {
    try {
      PreparedStatement stmt =
          c.prepareStatement("INSERT INTO MONITORDB (EVENT_TYPE, TM, BUS_ID, SERVICE_NAME, MESSAGE_OBJ) VALUES (?, ?, ?, ?, ?)");
      stmt.setInt(1, EventType.ERROR.ordinal());
      stmt.setLong(2, time);
      stmt.setString(3, busId);
      stmt.setString(4, service);
      stmt.setObject(5, error);
      stmt.execute();
    }
    catch (Throwable e) {
      throw new RuntimeException("error", e);
    }
  }

  public List<Record> getAllMessages(EventType type, String busId, String service) {
    try {
      PreparedStatement stmt =
          c.prepareStatement("SELECT * FROM MONITORDB WHERE EVENT_TYPE=?" + (busId != null ? " AND TO_BUS_ID=?" : "")
              + (service != null ? " AND SERVICE_NAME LIKE ?" : ""));

      stmt.setInt(1, type.ordinal());

      int x = 2;
      if (busId != null) {
        stmt.setString(x++, busId);
      }

      if (service != null) {
        stmt.setString(x, service);
      }

      if (stmt.execute()) {
        ResultSet results = stmt.getResultSet();
        ArrayList<Record> records = new ArrayList<Record>(100);
        while (results.next()) {
          String messageResult = results.getString(8);
          Object message = (messageResult == null) ? null : UiHelper.decodeAndDemarshall(valueOf(messageResult));
          records.add(new Record(results.getLong(1), results.getInt(2), results.getInt(3), results.getInt(4),
              results.getString(5), results.getString(6), results.getString(7), message));
        }
        return records;
      }
      return null;
    }
    catch (Throwable e) {
      throw new RuntimeException("error", e);
    }
  }

  public void attach(final ActivityProcessor proc) {
    proc.registerEvent(EventType.MESSAGE, new MessageMonitor() {
      public void monitorEvent(MessageEvent event) {
        if (!event.isReplay()) {
          storeRecord(event.getTime(), event.getFromBus(), event.getToBus(), event.getSubject(), (Message) event
              .getContents());
        }
      }
    });

    proc.registerEvent(EventType.BUS_EVENT, new MessageMonitor() {
      public void monitorEvent(MessageEvent event) {
        if (!event.isReplay()) {
          storeBusEvent(event.getTime(), event.getSubType(), event.getFromBus(), event.getToBus(), event.getSubject(),
              event.getContents());
        }
      }
    });

    proc.registerEvent(EventType.REPLAY_MESSAGES, new MessageMonitor() {
      public void monitorEvent(MessageEvent event) {
        for (Record r : getAllMessages(EventType.MESSAGE, event.getFromBus(), event.getSubject())) {
          proc.notifyEvent(r.time, EventType.values()[r.eventType], SubEventType.values()[r.subEventId], r.fromBus,
              r.toBus, r.service, (Message) r.message, null, true);
        }
      }
    });

    proc.registerEvent(EventType.REPLAY_BUS_EVENTS, new MessageMonitor() {
      public void monitorEvent(MessageEvent event) {
        for (Record r : getAllMessages(EventType.BUS_EVENT, "Server", event.getSubject())) {
          proc.notifyEvent(r.time, EventType.values()[r.eventType], SubEventType.values()[r.subEventId], r.fromBus,
              r.toBus, r.service, (Message) r.message, null, true);
        }
      }
    });
  }
}
