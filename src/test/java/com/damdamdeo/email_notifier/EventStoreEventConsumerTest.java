package com.damdamdeo.email_notifier;

import com.damdamdeo.email_notifier.domain.EmailNotifier;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.builder.RequestSpecBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.util.concurrent.CompletionStage;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
public class EventStoreEventConsumerTest {

    @Inject
    KafkaEmailProducer kafkaEmailProducer;

    @Inject
    EmailNotifier emailNotifier;

    @BeforeEach
    public void setup() {
        given(new RequestSpecBuilder().setBaseUri("http://localhost").setPort(8025).build())
                .when()
                .delete("/api/v1/messages")
                .then()
                .statusCode(200);
    }

    @Test
    public void should_send_email() throws Exception {
        // Given
        final CompletionStage<Void> completionStage = emailNotifier.notify("subject", "content");
        completionStage.toCompletableFuture().get();

        // Then
        given(new RequestSpecBuilder().setBaseUri("http://localhost").setPort(8025).build())
                .when()
                .get("/api/v1/messages")
                .then()
                .statusCode(200)
                .body("[0].Content.Headers.Subject[0]", equalTo("subject"))
                .body("[0].MIME.Parts[0].MIME.Parts[0].Body", equalTo("content"));
    }

    @Test
    public void should_consume_kafka_topic() throws Exception {
        // When
        kafkaEmailProducer.produce("kafka subject", "kafka content");
        Thread.sleep(5000);// 5 sec is enough to make the test crash ie. ensure it fails after a time limit set to 2 sec in vertx.

        // Then
        given(new RequestSpecBuilder().setBaseUri("http://localhost").setPort(8025).build())
                .when()
                .get("/api/v1/messages")
                .then()
                .statusCode(200)
                .body("[0].Content.Headers.Subject[0]", equalTo("kafka subject"))
                .body("[0].MIME.Parts[0].MIME.Parts[0].Body", equalTo("kafka content"));
    }

}
