package com.github.prkaspars.racefeed.component;

import com.github.prkaspars.racefeed.message.CarCoordinates;
import com.github.prkaspars.racefeed.message.Location;
import com.github.prkaspars.racefeed.model.Car;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CarCoordinatesConsumerTest {

  @Test
  @DisplayName("accept should create Car instance and put state in queue")
  public void acceptInitial() {
    TransferQueue<Car.State> states = new LinkedTransferQueue<>();
    CarCoordinatesConsumer consumer = new CarCoordinatesConsumer(states);
    consumer.accept(new CarCoordinates(123, 897, new Location(10, 10)));
    assertEquals(1, states.size());
    Car.State state = states.poll();
    assertEquals(897, state.getId());
    assertEquals(123, state.getTime());
    assertEquals(0.0, state.getDistance());
    assertEquals(0.0, state.getSpeed());
  }

  @Test
  @DisplayName("accept should get existing Car instance and put state in queue")
  public void acceptNext() {
    TransferQueue<Car.State> states = new LinkedTransferQueue<>();
    CarCoordinatesConsumer consumer = new CarCoordinatesConsumer(states);
    consumer.accept(new CarCoordinates(0, 1, new Location(10, 10)));
    consumer.accept(new CarCoordinates(3600000, 1, new Location(11, 11)));

    assertEquals(2, states.size());

    Car.State state1 = states.poll();
    assertEquals(1, state1.getId());
    assertEquals(0, state1.getTime());
    assertEquals(0.0, state1.getDistance());
    assertEquals(0.0, state1.getSpeed());

    Car.State state2 = states.poll();
    assertEquals(1, state2.getId());
    assertEquals(3600000, state2.getTime());
    assertEquals(155941.21480117145, state2.getDistance());
    assertEquals(43.31700411143651, state2.getSpeed());
  }
}
