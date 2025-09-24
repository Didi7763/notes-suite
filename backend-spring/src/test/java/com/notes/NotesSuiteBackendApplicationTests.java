package com.notes;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class NotesSuiteBackendApplicationTests {

    @Test
    void contextLoads() {
        // Test que l'application démarre correctement
    }
}




