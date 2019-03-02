package com.github.prkaspars.racefeed.message;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {

  @Test
  @DisplayName("distance should be zero when coordinates are equal")
  void distanceZero() {
    Location a = new Location(10, 10);
    Location b = new Location(10, 10);

    assertEquals(0.0, a.distance(b));
  }

  @Test
  @DisplayName("distance should be zero when coordinates are equal")
  void distance() {
    Location a = new Location(10, 10);
    Location b = new Location(11, 11);

    assertEquals(155941.21480117145, a.distance(b));
  }
}
