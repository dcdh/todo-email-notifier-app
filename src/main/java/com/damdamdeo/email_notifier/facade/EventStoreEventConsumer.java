package com.damdamdeo.email_notifier.facade;

import com.damdamdeo.email_notifier.domain.*;
import io.smallrye.reactive.messaging.kafka.KafkaMessage;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class EventStoreEventConsumer {

    private final static Logger LOGGER = Logger.getLogger(EventStoreEventConsumer.class.getName());

    final EmailNotifier emailNotifier;

    public EventStoreEventConsumer(final EmailNotifier emailNotifier) {
        this.emailNotifier = Objects.requireNonNull(emailNotifier);
    }

    @Incoming("email")
    public CompletionStage<Void> onMessage(final KafkaMessage<String, String> message) {
        LOGGER.log(Level.INFO, "Consuming email kafka topic");
        this.emailNotifier.notify(message.getKey(), message.getPayload());
        return message.ack();
    }

}
