package com.github.prkaspars.racefeed.component;

import com.github.prkaspars.racefeed.model.Car;
import com.github.prkaspars.racefeed.util.Tuple;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import static java.util.Comparator.comparingDouble;

@Component
public class RankingComponent {
  private List<Car.State> positions;
  private List<Car.State> stateBucket = new CopyOnWriteArrayList<>();
  private Predicate<List<Car.State>> predicate;

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

  synchronized List<Tuple<Integer, Integer, String>> overtakes(List<Car.State> p) {
    if (positions == null) {
      positions = p;
      return Collections.emptyList();
    }

    int l = positions.size();
    Deque<Car.State> deque = new LinkedList<>();
    List<Tuple<Integer, Integer, String>> result = new LinkedList<>();
    Car.State s, e;
    for (int i = 0, j = 0; i < l; i++, j = i) {
      e = p.get(i);
      if (e.isIdEqual(positions.get(i))) {
        continue;
      }
      while (j < l && !e.isIdEqual(positions.get(j))) {
        deque.push(positions.get(j++));
      }
      if (j < l) {
        positions.add(i, positions.remove(j));
      }
      while (!deque.isEmpty() && (s = deque.poll()) != null) {
        if (e.delta(s) > 12) {
          result.add(new Tuple<>(e.getId(), s.getId(), "dramatic"));
        }
      }
    }

    positions = p;
    return result;
  }
}
