package com.caique.AdvancedCrud.shared.errorlog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CriticalErrorTestController {

    @GetMapping("/api/v1/test/critical-error")
    public void throwCriticalError() {
        throw new IllegalStateException("Forced critical error for integration test");
    }
}
