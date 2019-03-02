package com.github.prkaspars.racefeed.message;

public class Event {
  private long timestamp;
  private String text;

  public Event(long timestamp, String text) {
    this.timestamp = timestamp;
    this.text = text;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public String getText() {
    return text;
  }
}
