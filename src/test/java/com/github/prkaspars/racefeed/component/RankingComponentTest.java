package com.github.prkaspars.racefeed.component;

import com.github.prkaspars.racefeed.model.Car;
import com.github.prkaspars.racefeed.util.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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

  @Test
  @DisplayName("overtakes should return empty list on initial call")
  public void overtakesInitial() {
    List<Car.State> s1 = List.of(
      new Car(2).new State(1, 8, 10),
      new Car(1).new State(1, 4, 10),
      new Car(3).new State(1, 4, 10)
    );
    RankingComponent ranking = new RankingComponent(l -> l.size() == 6);
    assertTrue(ranking.overtakes(s1).isEmpty());
  }

  @Test
  @DisplayName("overtakes should return overtakes tuples")
  public void overtakes() {
    List<Car.State> s1 = new ArrayList<>(List.of(
      new Car(5).new State(1, 6, 10),
      new Car(6).new State(1, 5, 10),
      new Car(2).new State(1, 4, 10),
      new Car(1).new State(1, 3, 10),
      new Car(3).new State(1, 2, 10),
      new Car(4).new State(1, 1, 10)
    ));
    List<Car.State> s2 = new ArrayList<>(List.of(
      new Car(5).new State(2, 20, 10),
      new Car(4).new State(2, 19, 10),
      new Car(1).new State(2, 18, 10),
      new Car(6).new State(2, 17, 10),
      new Car(2).new State(2, 16, 10),
      new Car(3).new State(2, 15, 10)
    ));

    RankingComponent ranking = new RankingComponent(l -> l.size() == 6);
    assertTrue(ranking.overtakes(s1).isEmpty());
    List<Tuple<Integer, Integer, String>> tuples = ranking.overtakes(s2);
    assertEquals(6, tuples.size());

    assertEquals(new Tuple<>(4, 3, "dramatic"), tuples.get(0));
    assertEquals(new Tuple<>(4, 1, "dramatic"), tuples.get(1));
    assertEquals(new Tuple<>(4, 2, "dramatic"), tuples.get(2));
    assertEquals(new Tuple<>(4, 6, "dramatic"), tuples.get(3));
    assertEquals(new Tuple<>(1, 2, "dramatic"), tuples.get(4));
    assertEquals(new Tuple<>(1, 6, "dramatic"), tuples.get(5));
  }
}
