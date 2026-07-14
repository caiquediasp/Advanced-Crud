package com.caique.AdvancedCrud.shared.errorlog;

import com.caique.AdvancedCrud.TestcontainersConfiguration;
import com.caique.AdvancedCrud.shared.config.RabbitConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ErrorLogFlowIntegrationTest {

    private static final String CRITICAL_ERROR_PATH = "/api/v1/test/critical-error";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ErrorLogPublisher errorLogPublisher;

    @Autowired
    private ErrorLogRepository errorLogRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void unexpectedError_returns500AndPersistsErrorLogThroughRabbit() throws Exception {
        mockMvc.perform(get(CRITICAL_ERROR_PATH).with(jwt()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(findByRequestPath(CRITICAL_ERROR_PATH)).hasSize(1));

        ErrorLog saved = findByRequestPath(CRITICAL_ERROR_PATH).getFirst();
        assertThat(saved.getExceptionType()).isEqualTo("java.lang.IllegalStateException");
        assertThat(saved.getMessage()).contains("Forced critical error");
        assertThat(saved.getStackTrace()).isNotBlank();
    }

    @Test
    void duplicateEventId_consumerPersistsOnlyOnce() throws Exception {
        UUID eventId = UUID.randomUUID();
        CriticalErrorEvent event = new CriticalErrorEvent(
                eventId,
                "java.lang.RuntimeException",
                "duplicate event test",
                "/duplicate-test",
                "fake stack trace",
                Instant.now()
        );

        errorLogPublisher.publish(event);
        errorLogPublisher.publish(event);

        await().atMost(Duration.ofSeconds(10))
                .until(() -> errorLogRepository.existsByEventId(eventId));

        Thread.sleep(1000);

        long persisted = errorLogRepository.findAll().stream()
                .filter(log -> eventId.equals(log.getEventId()))
                .count();
        assertThat(persisted).isEqualTo(1);
        assertThat(rabbitTemplate.receive(RabbitConfig.ERROR_DLQ)).isNull();
    }

    @Test
    void poisonMessage_goesToDeadLetterQueue() {
        rabbitTemplate.convertAndSend(
                RabbitConfig.ERROR_EXCHANGE,
                RabbitConfig.ERROR_ROUTING_KEY,
                "not a valid event");

        Message deadLetter = rabbitTemplate.receive(RabbitConfig.ERROR_DLQ, 15000);

        assertThat(deadLetter).isNotNull();
        assertThat(new String(deadLetter.getBody())).contains("not a valid event");
    }

    private List<ErrorLog> findByRequestPath(String requestPath) {
        return errorLogRepository.findAll().stream()
                .filter(log -> requestPath.equals(log.getRequestPath()))
                .toList();
    }
}
