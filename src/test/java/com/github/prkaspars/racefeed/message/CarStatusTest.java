package com.github.prkaspars.racefeed.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CarStatusTest {

  @Test
  void newSpeedStatus() {
    CarStatus status = CarStatus.newSpeedStatus(1, 1, 1);
    assertEquals("SPEED", status.getType());
    assertEquals(1, status.getCarIndex());
    assertEquals(1, status.getTimestamp());
    assertEquals(1, status.getValue());
  }

  @Test
  void newPositionStatus() {
    CarStatus status = CarStatus.newPositionStatus(1, 1, 1);
    assertEquals("POSITION", status.getType());
    assertEquals(1, status.getCarIndex());
    assertEquals(1, status.getTimestamp());
    assertEquals(1, status.getValue());
  }
}
