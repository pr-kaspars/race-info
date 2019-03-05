package com.github.prkaspars.racefeed.component;

import com.github.prkaspars.racefeed.model.Car;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RankingComponentTest {

  @Test
  @DisplayName("putState should return false when bucket is not full")
  public void putState() {
    RankingComponent ranking = new RankingComponent(l -> l.size() == 6);
    assertFalse(ranking.putState(new Car(1).new State(1, 1, 1)).isPresent());
  }

  @Test
  @DisplayName("putState should return false when bucket is full")
  public void putStateFullBucket() {
    RankingComponent ranking = new RankingComponent(l -> l.size() == 6);
    for (int i = 1; i < 6; i++) {
      assertFalse(ranking.putState(new Car(i).new State(1, 1, 1)).isPresent());
    }
    assertTrue(ranking.putState(new Car(6).new State(1, 1, 1)).isPresent());
  }

  @Test
  @DisplayName("putState should return false when bucket is full")
  public void putStateSortedList() {
    RankingComponent ranking = new RankingComponent(l -> l.size() == 6);
    for (int i = 1; i < 6; i++) {
      ranking.putState(new Car(i).new State(1, i * 2, 1));
    }
    List<Car.State> result = ranking.putState(new Car(6).new State(1, 6 * 2, 1)).get();
    assertEquals(6, result.size());
    for (int i = 0; i < 6; i++) {
      int j = 6 - i;
      assertEquals(j * 2, result.get(i).getDistance());
      assertEquals(j, result.get(i).getId());
    }
  }
}
