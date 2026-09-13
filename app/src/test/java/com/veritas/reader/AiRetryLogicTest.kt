package com.veritas.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicInteger

class AiRetryLogicTest {

    @Test
    fun testApiExceptionRetryableStatus() {
        val error429 = GeminiStudyService.ApiException(429, "Rate limit", 5)
        assertTrue(error429.isRetryable)
        assertEquals(5L, error429.retryAfterSeconds)

        val error500 = GeminiStudyService.ApiException(500, "Internal Server Error")
        assertTrue(error500.isRetryable)

        val error503 = GeminiStudyService.ApiException(503, "Service Unavailable")
        assertTrue(error503.isRetryable)

        val error401 = GeminiStudyService.ApiException(401, "Unauthorized")
        assertFalse(error401.isRetryable)

        val error400 = GeminiStudyService.ApiException(400, "Bad Request")
        assertFalse(error400.isRetryable)

        val error403 = GeminiStudyService.ApiException(403, "Forbidden")
        assertFalse(error403.isRetryable)
    }

    @Test
    fun testExecuteWithRetrySuccessOnFirstAttempt() {
        val attempts = AtomicInteger(0)
        val result = GeminiStudyService.executeWithRetry(maxAttempts = 3, initialDelayMs = 10, maxDelayMs = 50) {
            attempts.incrementAndGet()
            "Success"
        }
        assertEquals("Success", result)
        assertEquals(1, attempts.get())
    }

    @Test
    fun testExecuteWithRetryRecoversAfterTransientFailure() {
        val attempts = AtomicInteger(0)
        val result = GeminiStudyService.executeWithRetry(maxAttempts = 3, initialDelayMs = 10, maxDelayMs = 50) {
            val count = attempts.incrementAndGet()
            if (count < 2) {
                throw GeminiStudyService.ApiException(429, "Rate limit", null)
            }
            "Recovered"
        }
        assertEquals("Recovered", result)
        assertEquals(2, attempts.get())
    }

    @Test
    fun testExecuteWithRetryRecoversAfterSocketTimeout() {
        val attempts = AtomicInteger(0)
        val result = GeminiStudyService.executeWithRetry(maxAttempts = 3, initialDelayMs = 10, maxDelayMs = 50) {
            val count = attempts.incrementAndGet()
            if (count < 2) {
                throw SocketTimeoutException("Read timed out")
            }
            "RecoveredTimeout"
        }
        assertEquals("RecoveredTimeout", result)
        assertEquals(2, attempts.get())
    }

    @Test
    fun testExecuteWithRetryFailsImmediatelyOnNonRetryableError() {
        val attempts = AtomicInteger(0)
        try {
            GeminiStudyService.executeWithRetry(maxAttempts = 3, initialDelayMs = 10, maxDelayMs = 50) {
                attempts.incrementAndGet()
                throw GeminiStudyService.ApiException(401, "Invalid API key")
            }
            fail("Expected ApiException(401) to be thrown immediately")
        } catch (e: GeminiStudyService.ApiException) {
            assertEquals(401, e.statusCode)
            assertEquals(1, attempts.get())
        }
    }

    @Test
    fun testExecuteWithRetryExhaustsAllAttempts() {
        val attempts = AtomicInteger(0)
        try {
            GeminiStudyService.executeWithRetry(maxAttempts = 3, initialDelayMs = 10, maxDelayMs = 50) {
                attempts.incrementAndGet()
                throw GeminiStudyService.ApiException(503, "Service unavailable")
            }
            fail("Expected ApiException(503) after exhausting retries")
        } catch (e: GeminiStudyService.ApiException) {
            assertEquals(503, e.statusCode)
            assertEquals(3, attempts.get())
        }
    }
}
