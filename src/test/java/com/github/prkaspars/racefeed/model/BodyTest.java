package com.github.prkaspars.racefeed.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BodyTest {

  @Test
  @DisplayName("displace should create state with no speed when called the first time")
  public void stateInitial() {
    Body.State state = new Body(4).displace(9, 3.0);
    assertEquals(4, state.getId());
    assertEquals(9, state.getTime());
    assertEquals(3.0, state.getDistance());
    assertEquals(0.0, state.getSpeed());
  }

  @Test
  @DisplayName("displace should create state with speed when called more than once")
  public void stateSecond() {
    Body body = new Body(4);
    body.displace(1000, 10);
    Body.State state = body.displace(2000, 10);
    assertEquals(4, state.getId());
    assertEquals(2000, state.getTime());
    assertEquals(20.0, state.getDistance());
    assertEquals(10.0, state.getSpeed());
  }

  @Test
  @DisplayName("getSpeedMph should return rounded speed in mph")
  public void getSpeedMph() {
    Body.State state = new Body(4).new State(1, 1, 500);
    assertEquals(1118, state.getSpeedMph());
  }
}
