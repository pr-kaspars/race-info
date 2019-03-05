package com.github.prkaspars.racefeed.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.prkaspars.racefeed.exception.RuntimeInterruptedException;
import com.github.prkaspars.racefeed.message.CarCoordinates;
import com.github.prkaspars.racefeed.message.CarStatus;
import com.github.prkaspars.racefeed.message.Event;
import com.github.prkaspars.racefeed.model.Car;
import com.github.prkaspars.racefeed.publisher.MessagePublisher;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.prkaspars.racefeed.message.CarStatus.newPositionStatus;
import static com.github.prkaspars.racefeed.message.CarStatus.newSpeedStatus;
import static com.github.prkaspars.racefeed.message.Event.newDramaticOvertake;
import static com.github.prkaspars.racefeed.message.Event.newRegularOvertake;
import static java.lang.Thread.currentThread;

@Component
public class RaceInfoService implements ApplicationListener<ContextRefreshedEvent> {
  private static final Logger logger = LoggerFactory.getLogger(RaceInfoService.class);

  private boolean initialized = false;
  private Map<Integer, Car> cars = new ConcurrentHashMap<>();

  private ThreadPoolTaskExecutor executor;
  private MessageService messageService;
  private RankingComponent ranking;
  private QueueComponent queueComponent;

  @Autowired
  public RaceInfoService(MessageService messageService, RankingComponent ranking, ThreadPoolTaskExecutor executor, QueueComponent queueComponent) {
    this.executor = executor;
    this.messageService = messageService;
    this.ranking = ranking;
    this.queueComponent = queueComponent;
  }

  private Car getCar(int index) {
    Car car = cars.get(index);
    if (car != null) {
      return car;
    }

    synchronized (this) {
      if (!cars.containsKey(index)) {
        car = new Car(index);
        cars.put(index, car);
      } else {
        car = cars.get(index);
      }
      return car;
    }
  }

  void acceptCarCoordinates(CarCoordinates coordinates) {
    Car car = getCar(coordinates.getCarIndex());
    Car.State state = car.displace(coordinates.getTimestamp(), coordinates.getLocation());
    queueComponent.offerState(state);
  }

  void acceptState(Car.State state) {
    queueComponent.offerStatus(newSpeedStatus(state.getId(), state.getSpeedMph(), state.getTime()));

    Optional<List<Car.State>> o = ranking.putState(state);
    if (!o.isPresent()) {
      return;
    }

    List<Car.State> p = o.get();
    for (int i = 0; i < p.size(); i++) {
      queueComponent.offerStatus(newPositionStatus(p.get(i).getId(), i + 1, state.getTime()));
    }

    ranking.overtakes(p).stream()
      .map(t -> {
        if ("dramatic".equals(t.getC())) {
          return newDramaticOvertake(state.getTime(), t.getA(), t.getB());
        }
        return newRegularOvertake(state.getTime(), t.getA(), t.getB());
      })
      .forEach(queueComponent::offerEvent);
  }

  Runnable stateRunnable() {
    return () -> {
      while (!currentThread().isInterrupted()) {
        try {
          acceptState(queueComponent.takeState());
        } catch (InterruptedException e) {
          throw new RuntimeInterruptedException("Problem retrieving Car State", e);
        }
      }
    };
  }

  MessagePublisher<CarStatus> carStatusMessagePublisher() {
    return new MessagePublisher<>(queueComponent::takeStatus, m -> {
      try {
        messageService.publishCarStatus(m);
      } catch (MqttException | JsonProcessingException e) {
        logger.warn("Could not publish CarStatus message", e);
      }
    });
  }

  MessagePublisher<Event> eventMessagePublisher() {
    return new MessagePublisher<>(queueComponent::takeEvent, m -> {
      try {
        messageService.publishEvent(m);
      } catch (MqttException | JsonProcessingException e) {
        logger.warn("Could not publish Event message", e);
      }
    });
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    if (initialized) {
      return;
    }

    synchronized (this) {
      if (!initialized) {
        try {
          messageService.subscribeToCarCoordinates(this::acceptCarCoordinates);
          executor.execute(stateRunnable());
          for (int i = 0; i < 3; i++) {
            executor.execute(carStatusMessagePublisher());
            executor.execute(eventMessagePublisher());
          }
          initialized = true;
        } catch (MqttException | RuntimeInterruptedException e) {
          logger.error("Fatal messaging problem", e);
          executor.shutdown();
        }
      }
    }
  }
}
