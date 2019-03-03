package com.github.prkaspars.racefeed.component;

import com.github.prkaspars.racefeed.message.CarCoordinates;
import com.github.prkaspars.racefeed.model.Car;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TransferQueue;
import java.util.function.Consumer;

public class CarCoordinatesConsumer implements Consumer<CarCoordinates> {
  private Map<Integer, Car> carMap = new ConcurrentHashMap<>();
  private TransferQueue<Car.State> states;

  public CarCoordinatesConsumer(TransferQueue<Car.State> states) {
    this.states = states;
  }

  @Override
  public void accept(CarCoordinates coordinates) {
    Car car = carMap.get(coordinates.getCarIndex());
    if (car == null) {
      car = new Car(coordinates.getCarIndex());
      carMap.put(coordinates.getCarIndex(), car);
    }
    states.offer(car.displace(coordinates.getTimestamp(), coordinates.getLocation()));
  }
}
