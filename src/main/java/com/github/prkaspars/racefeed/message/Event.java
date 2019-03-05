package com.github.prkaspars.racefeed.message;

public class Event {
  private long timestamp;
  private String text;

  public static Event newDramaticOvertake(long timestamp, int c1, int c2) {
    return new Event(timestamp, String.format("Car %d races ahead of Car %d in a dramatic overtake.", c1, c2));
  }

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
