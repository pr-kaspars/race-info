package com.github.prkaspars.racefeed.component;

import com.github.prkaspars.racefeed.model.Car;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Comparator.comparingDouble;

public class Ranking {
  private List<Car.State> stateBucket = new CopyOnWriteArrayList<>();

  public Optional<List<Car.State>> putState(Car.State state) {
    stateBucket.add(state);
    if (stateBucket.size() < 6) {
      return Optional.empty();
    }

    List<Car.State> leaderBoard = stateBucket;
    stateBucket = new CopyOnWriteArrayList<>();

    leaderBoard.sort(comparingDouble(Car.State::getDistance).reversed());
    return Optional.of(leaderBoard);
  }
}
