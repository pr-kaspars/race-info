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

  /**
   * Returns existing Car instance or creates new and puts in map.
   *
   * @param index car index
   * @return the Car
   */
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

  /**
   * Accepts CarCoordinates and create put new Car.State in queue.
   *
   * @param coordinates CarCoordinates message
   */
  void acceptCarCoordinates(CarCoordinates coordinates) {
    Car car = getCar(coordinates.getCarIndex());
    Car.State state = car.displace(coordinates.getTimestamp(), coordinates.getLocation());
    queueComponent.offerState(state);
  }

  /**
   * Accepts Car.State and creates speed and position CarStatus messages and overtake Events if any happened.
   *
   * @param state car state
   */
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

  /**
   * Returns Runnable that takes Car.State messages from queue and calls State consumer.
   *
   * @return the Car.State consumer
   */
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

  /**
   * Returns Runnable that takes CarStatus messages from queue and publishes them.
   *
   * @return the CarStatus publisher
   */
  MessagePublisher<CarStatus> carStatusMessagePublisher() {
    return new MessagePublisher<>(queueComponent::takeStatus, m -> {
      try {
        messageService.publishCarStatus(m);
      } catch (MqttException | JsonProcessingException e) {
        logger.warn("Could not publish CarStatus message", e);
      }
    });
  }

  /**
   * Returns Runnable that takes Event messages from queue and publishes them.
   *
   * @return the Event publisher
   */
  MessagePublisher<Event> eventMessagePublisher() {
    return new MessagePublisher<>(queueComponent::takeEvent, m -> {
      try {
        messageService.publishEvent(m);
      } catch (MqttException | JsonProcessingException e) {
        logger.warn("Could not publish Event message", e);
      }
    });
  }

  /**
   * Initialises all queue subscribers and message publishers.
   *
   * @param event initialized or refreshed event
   */
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
