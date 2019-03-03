package com.github.prkaspars.racefeed.service;

import com.github.prkaspars.racefeed.model.Body;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Comparator.comparingDouble;

public class Ranking {
  private List<Body.State> stateBucket = new CopyOnWriteArrayList<>();

  public Optional<List<Body.State>> putState(Body.State state) {
    stateBucket.add(state);
    if (stateBucket.size() < 6) {
      return Optional.empty();
    }

    List<Body.State> leaderBoard = stateBucket;
    stateBucket = new CopyOnWriteArrayList<>();

    leaderBoard.sort(comparingDouble(Body.State::getDistance).reversed());
    return Optional.of(leaderBoard);
  }
}
