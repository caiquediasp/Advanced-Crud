package com.caique.AdvancedCrud.shared.errorLog;

import com.caique.AdvancedCrud.shared.config.RabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ErrorLogConsumer {

    private final ErrorLogRepository errorLogRepository;

    public ErrorLogConsumer(ErrorLogRepository errorLogRepository) {
        this.errorLogRepository = errorLogRepository;
    }

    @RabbitListener(queues = RabbitConfig.ERROR_QUEUE)
    public void handleErrorLogEvent(CriticalErrorEvent event) {
        if(errorLogRepository.existsByEventId(event.eventId())) {
            log.warn("Duplicate error event ignored: {}", event.eventId());
            return;
        }

        ErrorLog errorLog = new ErrorLog(
                event.eventId(),
                event.exceptionType(),
                event.message(),
                event.requestPath(),
                event.stackTrace(),
                event.occuredAt()
        );

        errorLogRepository.save(errorLog);
    }
}
