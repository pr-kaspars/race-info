package com.github.prkaspars.racefeed.component;

import com.github.prkaspars.racefeed.message.CarStatus;
import com.github.prkaspars.racefeed.model.Car;

import java.util.List;
import java.util.concurrent.TransferQueue;
import java.util.function.Consumer;

import static com.github.prkaspars.racefeed.message.CarStatus.newPositionStatus;
import static com.github.prkaspars.racefeed.message.CarStatus.newSpeedStatus;

public class StateConsumer implements Consumer<Car.State> {
  private Ranking ranking;
  private TransferQueue<CarStatus> statuses;

  public StateConsumer(TransferQueue<CarStatus> statuses, Ranking ranking) {
    this.ranking = ranking;
    this.statuses = statuses;
  }

  @Override
  public void accept(Car.State state) {
    statuses.offer(newSpeedStatus(state.getId(), state.getSpeedMph(), state.getTime()));
    int[] cars = ranking.putState(state).stream().flatMap(List::stream).mapToInt(Car.State::getId).toArray();
    for (int i = 0; i < cars.length; i++) {
      statuses.offer(newPositionStatus(cars[i], i + 1, state.getTime()));
    }
  }
}
