package pro.filaretov.java.concurrency.locks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadWriteLockTokenHolderTest {

    @Mock
    private Supplier<Token> tokenSupplier;

    @Mock
    private Clock clock;

    @InjectMocks
    private ReadWriteLockTokenHolder tokenHolder;

    @BeforeEach
    void setUp() {
        when(tokenSupplier.get()).thenReturn(new Token(UUID.randomUUID().toString(), Instant.now().plusSeconds(20)));
        when(clock.instant()).thenReturn(Instant.now());
    }

    @Test
    void shouldCreateOnlyOneTokenInSingleThread() {
        String token1 = tokenHolder.getToken();
        String token2 = tokenHolder.getToken();

        assertEquals(token1, token2);

        verify(tokenSupplier).get();
    }

    @SneakyThrows
    @Test
    void shouldCreateOnlyOneTokenWithThreads() {
        Runnable runnable = () -> tokenHolder.getToken();

        Thread thread1 = new Thread(runnable);
        Thread thread2 = new Thread(runnable);

        // Problem: thread1 may already complete execution by the time thread2 starts execution,
        // so not really a multi-threaded test
        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        verify(tokenSupplier).get();
    }

    @SneakyThrows
    @Test
    void shouldCreateOnlyOneTokenWithCountDown() {
        int threadsCount = 10;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(threadsCount);
        Runnable runnable = () -> {
            try {
                startSignal.await();
                tokenHolder.getToken();
                doneSignal.countDown();
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            }
        };

        for (int i = 0; i < threadsCount; i++) {
            new Thread(runnable).start();
        }

        startSignal.countDown();
        doneSignal.await();

        verify(tokenSupplier).get();
    }

}