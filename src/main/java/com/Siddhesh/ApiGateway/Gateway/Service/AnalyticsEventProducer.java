
package com.Siddhesh.ApiGateway.Gateway.Service;

import com.Siddhesh.ApiGateway.Analytics.Dtos.ApiAnomalyEvent;
import com.Siddhesh.ApiGateway.Analytics.Dtos.ApiRequestEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsEventProducer {

    private static final String REQUEST_TOPIC = "api-request-events";
    private static final String ANOMALY_TOPIC = "api-anomaly-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishRequestEvent(ApiRequestEvent event) {
        String key = String.valueOf(event.getSubscriptionId());

        try {
            kafkaTemplate.send(REQUEST_TOPIC, key, event)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error(
                                    "Failed to publish request event: eventId={}",
                                    event.getEventId(),
                                    error
                            );
                            return;
                        }

                        log.info(
                                "Published request event: eventId={}, topic={}, partition={}, offset={}",
                                event.getEventId(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    });
        } catch (Exception e) {
            log.error(
                    "Could not send request event: eventId={}",
                    event.getEventId(),
                    e
            );
        }
    }

    public void publishAnomalyEvent(ApiAnomalyEvent event) {
        String key = String.valueOf(event.getSubscriptionId());

        try {
            kafkaTemplate.send(ANOMALY_TOPIC, key, event)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error(
                                    "Failed to publish anomaly event: eventId={}",
                                    event.getEventId(),
                                    error
                            );
                            return;
                        }

                        log.info(
                                "Published anomaly event: eventId={}, topic={}, partition={}, offset={}",
                                event.getEventId(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    });
        } catch (Exception e) {
            log.error(
                    "Could not send anomaly event: eventId={}",
                    event.getEventId(),
                    e
            );
        }
    }
}
