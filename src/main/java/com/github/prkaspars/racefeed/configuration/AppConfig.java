package com.github.prkaspars.racefeed.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.prkaspars.racefeed.message.CarStatus;
import com.github.prkaspars.racefeed.message.Event;
import com.github.prkaspars.racefeed.model.Car;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

@Configuration
@EnableConfigurationProperties(MqttConfig.class)
public class AppConfig {

  @Autowired
  private MqttConfig mqttConfig;

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public IMqttClient mqttClient() throws MqttException {
    MqttConnectOptions options = new MqttConnectOptions();
    options.setAutomaticReconnect(true);
    options.setCleanSession(true);

    MqttClient client = new MqttClient(mqttConfig.getServerURI(), mqttConfig.getClientId());
    client.connect(options);
    return client;
  }

  @Bean
  public Executor executor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(6);
    executor.setMaxPoolSize(6 + 2);
    executor.setThreadNamePrefix("ex");
    executor.initialize();
    return executor;
  }

  @Bean
  public TransferQueue<Car.State> states() {
    return new LinkedTransferQueue<>();
  }

  @Bean
  public TransferQueue<CarStatus> statuses() {
    return new LinkedTransferQueue<>();
  }

  @Bean
  public TransferQueue<Event> events() {
    return new LinkedTransferQueue<>();
  }
}
