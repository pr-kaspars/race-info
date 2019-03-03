package com.github.prkaspars.racefeed.service;

import com.github.prkaspars.racefeed.model.Body;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RankingTest {

  @Test
  @DisplayName("putState should return false when bucket is not full")
  public void putState() {
    Ranking ranking = new Ranking();
    assertFalse(ranking.putState(new Body(1).new State(1, 1, 1)).isPresent());
  }

  @Test
  @DisplayName("putState should return false when bucket is full")
  public void putStateFullBucket() {
    Ranking ranking = new Ranking();
    for (int i = 1; i < 6; i++) {
      assertFalse(ranking.putState(new Body(i).new State(1, 1, 1)).isPresent());
    }
    assertTrue(ranking.putState(new Body(6).new State(1, 1, 1)).isPresent());
  }

  @Test
  @DisplayName("putState should return false when bucket is full")
  public void putStateSortedList() {
    Ranking ranking = new Ranking();
    for (int i = 1; i < 6; i++) {
      ranking.putState(new Body(i).new State(1, i * 2, 1));
    }
    List<Body.State> result = ranking.putState(new Body(6).new State(1, 6 * 2, 1)).get();
    assertEquals(6, result.size());
    for (int i = 0; i < 6; i++) {
      int j = 6 - i;
      assertEquals(j * 2, result.get(i).getDistance());
      assertEquals(j, result.get(i).getId());
    }
  }
}
