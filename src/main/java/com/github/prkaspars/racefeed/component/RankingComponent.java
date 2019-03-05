package com.github.prkaspars.racefeed.component;

import com.github.prkaspars.racefeed.model.Car;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import static java.util.Comparator.comparingDouble;

@Component
public class RankingComponent {
  private List<Car.State> stateBucket = new CopyOnWriteArrayList<>();
  Predicate<List<Car.State>> predicate;

  public RankingComponent(@Qualifier("rankingPredicate") Predicate<List<Car.State>> predicate) {
    this.predicate = predicate;
  }

  public Optional<List<Car.State>> putState(Car.State state) {
    stateBucket.add(state);
    if (predicate.negate().test(stateBucket)) {
      return Optional.empty();
    }

    List<Car.State> leaderBoard = stateBucket;
    stateBucket = new CopyOnWriteArrayList<>();
    leaderBoard.sort(comparingDouble(Car.State::getDistance).reversed());
    return Optional.of(leaderBoard);
  }
}
