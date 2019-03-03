package com.github.prkaspars.racefeed.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.prkaspars.racefeed.message.CarCoordinates;
import com.github.prkaspars.racefeed.message.CarStatus;
import com.github.prkaspars.racefeed.message.Event;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.function.Consumer;

public class MessageClient {
  private static final String TOPIC_CAR_COORDINATES = "carCoordinates";
  private static final String TOPIC_CAR_STATUS = "carStatus";
  private static final String TOPIC_EVENTS = "events";

  private IMqttClient iMqttClient;
  private ObjectMapper objectMapper;

  public MessageClient(IMqttClient iMqttClient, ObjectMapper objectMapper) {
    this.iMqttClient = iMqttClient;
    this.objectMapper = objectMapper;
  }

  public void subscribeToCarCoordinates(Consumer<CarCoordinates> consumer) throws MqttException {
    iMqttClient.subscribe(TOPIC_CAR_COORDINATES, 0, (t, m) ->
      consumer.accept(objectMapper.readValue(m.getPayload(), CarCoordinates.class))
    );
  }

  public void publishCarStatus(CarStatus carStatus) throws MqttException, JsonProcessingException {
    iMqttClient.publish(TOPIC_CAR_STATUS, new MqttMessage(objectMapper.writeValueAsBytes(carStatus)));
  }

  public void publishEvent(Event event) throws MqttException, JsonProcessingException {
    iMqttClient.publish(TOPIC_EVENTS, new MqttMessage(objectMapper.writeValueAsBytes(event)));
  }
}
