package com.github.prkaspars.racefeed.component;

import com.github.prkaspars.racefeed.exception.RuntimeInterruptedException;
import com.github.prkaspars.racefeed.message.CarCoordinates;
import com.github.prkaspars.racefeed.message.CarStatus;
import com.github.prkaspars.racefeed.message.Event;
import com.github.prkaspars.racefeed.message.Location;
import com.github.prkaspars.racefeed.model.Car;
import com.github.prkaspars.racefeed.util.Tuple;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RaceInfoServiceTest {
  private ThreadPoolTaskExecutor executor;
  private MessageService messageService;
  private RankingComponent ranking;
  private QueueComponent queueComponent;

  @BeforeEach
  public void setUp() {
    executor = mock(ThreadPoolTaskExecutor.class);
    messageService = mock(MessageService.class);
    ranking = mock(RankingComponent.class);
    queueComponent = mock(QueueComponent.class);
  }

  @Test
  @DisplayName("onApplicationEvent should subscribe to coordinate topic and start publishers")
  public void onApplicationEvent() throws MqttException {
    ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);
    RaceInfoService service = new RaceInfoService(messageService, ranking, executor, queueComponent);
    service.onApplicationEvent(event);

    verify(messageService, times(1)).subscribeToCarCoordinates(any());
    verify(executor, times(7)).execute(any());
  }

  @Test
  @DisplayName("onApplicationEvent should call executor shutdown when exception occurs")
  public void onApplicationEventException() throws MqttException {
    doThrow(new MqttException(1)).when(messageService).subscribeToCarCoordinates(any());
    ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);
    RaceInfoService service = new RaceInfoService(messageService, ranking, executor, queueComponent);
    service.onApplicationEvent(event);

    verify(executor, times(1)).shutdown();
  }

  @Test
  @DisplayName("carStatusMessagePublisher should publish message")
  public void carStatusMessagePublisher() throws Exception {
    CarStatus status = CarStatus.newSpeedStatus(1, 1, 1);
    when(queueComponent.takeStatus())
      .thenReturn(status)
      .thenThrow(new InterruptedException());
    RaceInfoService service = new RaceInfoService(messageService, ranking, executor, queueComponent);
    assertThrows(RuntimeInterruptedException.class, () -> service.carStatusMessagePublisher().run());
    verify(messageService, times(1)).publishCarStatus(eq(status));
  }

  @Test
  @DisplayName("carStatusMessagePublisher should publish message")
  public void eventMessagePublisher() throws Exception {
    Event event = Event.newDramaticOvertake(1, 1, 1);
    when(queueComponent.takeEvent())
      .thenReturn(event)
      .thenThrow(new InterruptedException());
    RaceInfoService service = new RaceInfoService(messageService, ranking, executor, queueComponent);
    assertThrows(RuntimeInterruptedException.class, () -> service.eventMessagePublisher().run());
    verify(messageService, times(1)).publishEvent(eq(event));
  }

  @Test
  @DisplayName("stateRunnable should take states from queue")
  public void stateRunnable() throws Exception {
    Car.State state = new Car(1).new State(1, 1, 1);
    when(queueComponent.takeState())
      .thenReturn(state)
      .thenThrow(new InterruptedException());
    when(ranking.putState(any())).thenReturn(Optional.empty());

    RaceInfoService service = new RaceInfoService(messageService, ranking, executor, queueComponent);
    assertThrows(RuntimeInterruptedException.class, () -> service.stateRunnable().run());
    verify(queueComponent, times(1)).offerStatus(any());
  }

  @Test
  @DisplayName("acceptState should put only speed status if ranking components responds with empty")
  public void acceptStateSpeed() {
    ArgumentCaptor<CarStatus> carStatusArgumentCaptor = ArgumentCaptor.forClass(CarStatus.class);
    when(ranking.putState(any())).thenReturn(Optional.empty());
    Car.State state = new Car(1).new State(10, 15, 10);
    RaceInfoService service = new RaceInfoService(messageService, ranking, executor, queueComponent);
    service.acceptState(state);
    verify(queueComponent, times(1)).offerStatus(carStatusArgumentCaptor.capture());
    CarStatus status = carStatusArgumentCaptor.getValue();
    assertEquals(10, status.getTimestamp());
    assertEquals(22, status.getValue());
    assertEquals(1, status.getCarIndex());
    assertEquals("SPEED", status.getType());
  }

  @Test
  @DisplayName("acceptState should put only speed status if ranking components responds with empty")
  public void acceptStatePosition() {
    ArgumentCaptor<CarStatus> carStatusArgumentCaptor = ArgumentCaptor.forClass(CarStatus.class);
    ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
    List<Car.State> s1 = List.of(
      new Car(2).new State(1, 8, 10),
      new Car(1).new State(1, 4, 10),
      new Car(3).new State(1, 4, 10)
    );
    List<Tuple<Integer, Integer, String>> o1 = List.of(
      new Tuple<>(2, 1, "dramatic"),
      new Tuple<>(1, 3, "foo")
    );
    when(ranking.putState(any())).thenReturn(Optional.of(s1));
    when(ranking.overtakes(s1)).thenReturn(o1);

    Car.State state = new Car(1).new State(10, 15, 10);
    RaceInfoService service = new RaceInfoService(messageService, ranking, executor, queueComponent);
    service.acceptState(state);
    verify(queueComponent, times(4)).offerStatus(carStatusArgumentCaptor.capture());
    verify(queueComponent, times(2)).offerEvent(eventArgumentCaptor.capture());

    List<CarStatus> statuses = carStatusArgumentCaptor.getAllValues();

    assertEquals(10, statuses.get(0).getTimestamp());
    assertEquals(22, statuses.get(0).getValue());
    assertEquals(1, statuses.get(0).getCarIndex());
    assertEquals("SPEED", statuses.get(0).getType());

    assertEquals(10, statuses.get(1).getTimestamp());
    assertEquals(1, statuses.get(1).getValue());
    assertEquals(2, statuses.get(1).getCarIndex());
    assertEquals("POSITION", statuses.get(2).getType());

    assertEquals(10, statuses.get(2).getTimestamp());
    assertEquals(2, statuses.get(2).getValue());
    assertEquals(1, statuses.get(2).getCarIndex());
    assertEquals("POSITION", statuses.get(2).getType());

    assertEquals(10, statuses.get(3).getTimestamp());
    assertEquals(3, statuses.get(3).getValue());
    assertEquals(3, statuses.get(3).getCarIndex());
    assertEquals("POSITION", statuses.get(3).getType());

    List<Event> events = eventArgumentCaptor.getAllValues();

    assertEquals(10, events.get(0).getTimestamp());
    assertEquals("Car 2 races ahead of Car 1 in a dramatic overtake.", events.get(0).getText());

    assertEquals(10, events.get(1).getTimestamp());
    assertEquals("Car 1 races ahead of Car 3 in a regular overtake.", events.get(1).getText());
  }

  @Test
  @DisplayName("acceptCarCoordinates should create Car instance and put state in queue")
  public void acceptCarCoordinatesInitial() {
    ArgumentCaptor<Car.State> stateArgumentCaptor = ArgumentCaptor.forClass(Car.State.class);
    RaceInfoService service = new RaceInfoService(messageService, ranking, executor, queueComponent);
    service.acceptCarCoordinates(new CarCoordinates(123, 897, new Location(10, 10)));

    verify(queueComponent, times(1)).offerState(stateArgumentCaptor.capture());

    Car.State state = stateArgumentCaptor.getValue();
    assertEquals(897, state.getId());
    assertEquals(123, state.getTime());
    assertEquals(0.0, state.getDistance());
    assertEquals(0.0, state.getSpeed());
  }

  @Test
  @DisplayName("acceptCarCoordinates should get existing Car instance and put state in queue")
  public void acceptCarCoordinatesNext() {
    ArgumentCaptor<Car.State> stateArgumentCaptor = ArgumentCaptor.forClass(Car.State.class);
    RaceInfoService service = new RaceInfoService(messageService, ranking, executor, queueComponent);
    service.acceptCarCoordinates(new CarCoordinates(0, 1, new Location(10, 10)));
    service.acceptCarCoordinates(new CarCoordinates(3600000, 1, new Location(11, 11)));

    verify(queueComponent, times(2)).offerState(stateArgumentCaptor.capture());

    List<Car.State> states = stateArgumentCaptor.getAllValues();
    Car.State state1 = states.get(0);
    assertEquals(1, state1.getId());
    assertEquals(0, state1.getTime());
    assertEquals(0.0, state1.getDistance());
    assertEquals(0.0, state1.getSpeed());

    Car.State state2 = states.get(1);
    assertEquals(1, state2.getId());
    assertEquals(3600000, state2.getTime());
    assertEquals(155941.21480117145, state2.getDistance());
    assertEquals(43.31700411143651, state2.getSpeed());
  }
}
