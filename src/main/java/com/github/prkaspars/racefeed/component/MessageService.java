package com.github.prkaspars.racefeed.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.prkaspars.racefeed.message.CarCoordinates;
import com.github.prkaspars.racefeed.message.CarStatus;
import com.github.prkaspars.racefeed.message.Event;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Abstraction layer on top of MQTT client.
 */
@Component
public class MessageService {
  private static final String TOPIC_CAR_COORDINATES = "carCoordinates";
  private static final String TOPIC_CAR_STATUS = "carStatus";
  private static final String TOPIC_EVENTS = "events";

  private IMqttClient iMqttClient;
  private ObjectMapper objectMapper;

  @Autowired
  public MessageService(IMqttClient iMqttClient, ObjectMapper objectMapper) {
    this.iMqttClient = iMqttClient;
    this.objectMapper = objectMapper;
  }

  /**
   * Subscribe to carCoordinates topic.
   *
   * @param consumer consumer that will accept messages
   * @throws MqttException if there was an error registering the subscription.
   */
  public void subscribeToCarCoordinates(Consumer<CarCoordinates> consumer) throws MqttException {
    iMqttClient.subscribe(TOPIC_CAR_COORDINATES, 0, (t, m) ->
      consumer.accept(objectMapper.readValue(m.getPayload(), CarCoordinates.class))
    );
  }

  /**
   * Publishes CarStatus message to queue.
   *
   * @param carStatus message to publish
   * @throws MqttException           when a problem with storing the message
   * @throws JsonProcessingException when a problem serialising message
   */
  public void publishCarStatus(CarStatus carStatus) throws MqttException, JsonProcessingException {
    iMqttClient.publish(TOPIC_CAR_STATUS, new MqttMessage(objectMapper.writeValueAsBytes(carStatus)));
  }

  /**
   * Publishes Event message to queue.
   *
   * @param event message to publish
   * @throws MqttException           when a problem with storing the message
   * @throws JsonProcessingException when a problem serialising message
   */
  public void publishEvent(Event event) throws MqttException, JsonProcessingException {
    iMqttClient.publish(TOPIC_EVENTS, new MqttMessage(objectMapper.writeValueAsBytes(event)));
  }
}
