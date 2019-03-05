package com.github.prkaspars.racefeed.model;

import com.github.prkaspars.racefeed.message.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CarTest {

  @Test
  @DisplayName("displace should create state with no speed when called the first time")
  public void displaceFirst() {
    Car.State state = new Car(4).displace(9, new Location(10, 10));
    assertEquals(4, state.getId());
    assertEquals(9, state.getTime());
    assertEquals(0.0, state.getDistance());
    assertEquals(0.0, state.getSpeed());
  }

  @Test
  @DisplayName("displace should create state with speed when called more than once")
  public void displaceSecond() {
    Car car = new Car(4);
    car.displace(0, new Location(10, 10));
    Car.State state = car.displace(3600000, new Location(11, 11));
    assertEquals(4, state.getId());
    assertEquals(3600000, state.getTime());
    assertEquals(155941.21480117145, state.getDistance());
    assertEquals(43.31700411143651, state.getSpeed());
  }

  @Test
  @DisplayName("getSpeedMph should return rounded speed in mph")
  public void getSpeedMph() {
    Car.State state = new Car(4).new State(1, 1, 500);
    assertEquals(1118, state.getSpeedMph());
  }

  @Test
  @DisplayName("isIdEqual should return true when IDs are equal")
  public void isIdEqualTrue() {
    Car.State state = new Car(4).new State(1, 1, 500);
    assertTrue(state.isIdEqual(new Car(4).new State(2, 2, 600)));
  }

  @Test
  @DisplayName("isIdEqual should return false when IDs are not equal")
  public void isIdEqualFalse() {
    Car.State state = new Car(4).new State(1, 1, 500);
    assertFalse(state.isIdEqual(new Car(5).new State(2, 2, 600)));
  }

  @Test
  @DisplayName("delta skhould return distance difference")
  public void delta() {
    Car.State state = new Car(4).new State(1, 10, 500);
    assertEquals(90.0, state.delta(new Car(5).new State(2, 100, 600)));
  }
}
