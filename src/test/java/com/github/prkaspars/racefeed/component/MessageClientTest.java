package com.github.prkaspars.racefeed.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.prkaspars.racefeed.message.CarCoordinates;
import com.github.prkaspars.racefeed.message.CarStatus;
import com.github.prkaspars.racefeed.message.Event;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class MessageClientTest {
  private IMqttClient iMqttClient;
  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setUp() {
    iMqttClient = mock(IMqttClient.class);
  }

  @Test
  @DisplayName("publishCarStatus should serialize bean and publish message to the `carStatus` topic")
  public void publishCarStatus() throws MqttException, JsonProcessingException {
    ArgumentCaptor<MqttMessage> argumentCaptor = ArgumentCaptor.forClass(MqttMessage.class);
    CarStatus carStatus = CarStatus.newSpeedStatus(1, 1, 1);
    MessageClient client = new MessageClient(iMqttClient, objectMapper);
    client.publishCarStatus(carStatus);

    verify(iMqttClient, times(1)).publish(eq("carStatus"), argumentCaptor.capture());
    assertEquals("{\"type\":\"SPEED\",\"carIndex\":1,\"value\":1,\"timestamp\":1}", new String(argumentCaptor.getValue().getPayload()));
  }

  @Test
  @DisplayName("publishEvent should serialize bean and publish message to the `events` topic")
  public void publishEvent() throws MqttException, JsonProcessingException {
    ArgumentCaptor<MqttMessage> argumentCaptor = ArgumentCaptor.forClass(MqttMessage.class);
    MessageClient client = new MessageClient(iMqttClient, objectMapper);
    client.publishEvent(new Event(999, "foo"));

    verify(iMqttClient, times(1)).publish(eq("events"), argumentCaptor.capture());
    assertEquals("{\"timestamp\":999,\"text\":\"foo\"}", new String(argumentCaptor.getValue().getPayload()));
  }

  @Test
  @DisplayName("subscribeToCarCoordinates should subscribe to `carCoordinates` topic")
  @SuppressWarnings("unchecked")
  public void subscribeToCarCoordinates() throws Exception {
    MessageClient client = new MessageClient(iMqttClient, objectMapper);
    client.subscribeToCarCoordinates(m -> {
    });

    verify(iMqttClient, times(1)).subscribe(eq("carCoordinates"), eq(0), any(IMqttMessageListener.class));
  }

  @Test
  @DisplayName("subscribeToCarCoordinates should call apply method on consumer")
  @SuppressWarnings("unchecked")
  public void subscribeToCarCoordinatesConsumer() throws Exception {
    ArgumentCaptor<IMqttMessageListener> listenerArgumentCaptor = ArgumentCaptor.forClass(IMqttMessageListener.class);
    ArgumentCaptor<CarCoordinates> carCoordinatesArgumentCaptor = ArgumentCaptor.forClass(CarCoordinates.class);
    Consumer<CarCoordinates> consumer = mock(Consumer.class);

    MessageClient client = new MessageClient(iMqttClient, objectMapper);
    client.subscribeToCarCoordinates(consumer);

    verify(iMqttClient, times(1)).subscribe(eq("carCoordinates"), eq(0), listenerArgumentCaptor.capture());
    String msg = "{\"timestamp\":1541693114862,\"carIndex\":2,\"location\":{\"lat\":51.349937311969725,\"long\":-0.544958142167281}}";
    listenerArgumentCaptor.getValue().messageArrived("carCoordinates", new MqttMessage(msg.getBytes()));

    verify(consumer, times(1)).accept(carCoordinatesArgumentCaptor.capture());
    CarCoordinates carCoordinates = carCoordinatesArgumentCaptor.getValue();
    assertEquals(1541693114862L, carCoordinates.getTimestamp());
    assertEquals(2, carCoordinates.getCarIndex());
    assertEquals(51.349937311969725, carCoordinates.getLocation().getLatitude());
    assertEquals(-0.544958142167281, carCoordinates.getLocation().getLongitude());
  }
}
