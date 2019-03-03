package com.github.prkaspars.racefeed.component;

import com.github.prkaspars.racefeed.message.CarStatus;
import com.github.prkaspars.racefeed.model.Car;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StateConsumerTest {
  private Ranking ranking;
  private TransferQueue<CarStatus> statuses;

  @BeforeEach
  public void setUp() {
    ranking = mock(Ranking.class);
  }

  @Test
  @DisplayName("accept should add speed and position statuses")
  public void accept() {
    statuses = new LinkedTransferQueue<>();
    Car.State state = new Car(1).new State(10, 10, 10);
    when(ranking.putState(state)).thenReturn(Optional.of(List.of(state)));
    StateConsumer consumer = new StateConsumer(statuses, ranking);
    consumer.accept(state);
    assertEquals(2, statuses.size());

    CarStatus status1 = statuses.poll();
    assertEquals("SPEED", status1.getType());
    assertEquals(1, status1.getCarIndex());
    assertEquals(22, status1.getValue());

    CarStatus status2 = statuses.poll();
    assertEquals("POSITION", status2.getType());
    assertEquals(1, status2.getCarIndex());
    assertEquals(1, status2.getValue());
  }

  @Test
  @DisplayName("accept should add only speed to status queue when putState responds with empty")
  public void acceptEmpty() {
    statuses = new LinkedTransferQueue<>();
    Car.State state = new Car(1).new State(10, 10, 10);
    when(ranking.putState(state)).thenReturn(Optional.empty());
    StateConsumer consumer = new StateConsumer(statuses, ranking);
    consumer.accept(state);
    assertEquals(1, statuses.size());

    CarStatus status1 = statuses.poll();
    assertEquals("SPEED", status1.getType());
    assertEquals(1, status1.getCarIndex());
    assertEquals(22, status1.getValue());
  }
}
