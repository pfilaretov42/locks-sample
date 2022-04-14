package pro.filaretov.java.concurrency.locks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SimpleTokenHolderTest {

    @Mock
    private Supplier<Token> tokenSupplier;

    private SimpleTokenHolder tokenHolder;
    private Token token;

    @BeforeEach
    void setUp() {
        token = new Token(UUID.randomUUID().toString(), Instant.now().plusSeconds(20));
        when(tokenSupplier.get()).thenReturn(token);

        Clock clock = Clock.systemUTC();
        tokenHolder = new SimpleTokenHolder(tokenSupplier, clock);
    }

    @Test
    void getToken() {
        String token1 = tokenHolder.getToken();
        String token2 = tokenHolder.getToken();

        assertEquals(token1, token2);

        verify(tokenSupplier).get();
    }
}