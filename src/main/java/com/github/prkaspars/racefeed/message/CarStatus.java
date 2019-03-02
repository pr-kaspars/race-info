package com.github.prkaspars.racefeed.message;

public class CarStatus {
  public static final String TYPE_SPEED = "SPEED";
  public static final String TYPE_POSITION = "POSITION";

  private String type;
  private int carIndex;
  private int value;
  private long timestamp;

  public static CarStatus newSpeedStatus(int carIndex, int value, long timestamp) {
    return new CarStatus(TYPE_SPEED, carIndex, value, timestamp);
  }

  public static CarStatus newPositionStatus(int carIndex, int value, long timestamp) {
    return new CarStatus(TYPE_POSITION, carIndex, value, timestamp);
  }

  private CarStatus(String type, int carIndex, int value, long timestamp) {
    this.type = type;
    this.carIndex = carIndex;
    this.value = value;
    this.timestamp = timestamp;
  }

  public String getType() {
    return type;
  }

  public int getCarIndex() {
    return carIndex;
  }

  public int getValue() {
    return value;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
