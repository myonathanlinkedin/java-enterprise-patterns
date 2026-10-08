package com.lamport;

import java.util.Objects;

/**
 * Represents a logical event in the distributed system.
 */
public class Event {
    private final String processId;
    private final String description;
    private int timestamp;

    public Event(String processId, String description) {
        this.processId = Objects.requireNonNull(processId, "processId cannot be null");
        this.description = Objects.requireNonNull(description, "description cannot be null");
        this.timestamp = 0;
    }

    public String getProcessId() {
        return processId;
    }

    public String getDescription() {
        return description;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("Event{process='%s', desc='%s', ts=%d}", processId, description, timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return timestamp == event.timestamp &&
               Objects.equals(processId, event.processId) &&
               Objects.equals(description, event.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processId, description, timestamp);
    }
}

/**
 * Represents a message sent between processes.
 */
public class Message {
    private final String senderId;
    private final String receiverId;
    private final int timestamp;
    private final String payload;

    public Message(String senderId, String receiverId, int timestamp, String payload) {
        this.senderId = Objects.requireNonNull(senderId, "senderId cannot be null");
        this.receiverId = Objects.requireNonNull(receiverId, "receiverId cannot be null");
        this.timestamp = timestamp;
        this.payload = Objects.requireNonNull(payload, "payload cannot be null");
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return String.format("Message{from='%s', to='%s', ts=%d, payload='%s'}",
                senderId, receiverId, timestamp, payload);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return timestamp == message.timestamp &&
               Objects.equals(senderId, message.senderId) &&
               Objects.equals(receiverId, message.receiverId) &&
               Objects.equals(payload, message.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderId, receiverId, timestamp, payload);
    }
}

/**
 * Represents a process in the distributed system.
 */
public class Process {
    private final String id;
    private int clock;

    public Process(String id) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.clock = 0;
    }

    public String getId() {
        return id;
    }

    public int getClock() {
        return clock;
    }

    public void setClock(int clock) {
        this.clock = clock;
    }

    @Override
    public String toString() {
        return String.format("Process{id='%s', clock=%d}", id, clock);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Process process = (Process) o;
        return clock == process.clock && Objects.equals(id, process.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, clock);
    }
}
