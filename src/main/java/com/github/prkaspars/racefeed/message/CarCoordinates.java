package com.github.prkaspars.racefeed.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CarCoordinates {
  private long timestamp;
  private int carIndex;
  private Location location;

  @JsonCreator
  public CarCoordinates(@JsonProperty("timestamp") long timestamp, @JsonProperty("carIndex") int carIndex, @JsonProperty("location") Location location) {
    this.timestamp = timestamp;
    this.carIndex = carIndex;
    this.location = location;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public int getCarIndex() {
    return carIndex;
  }

  public Location getLocation() {
    return location;
  }
}
