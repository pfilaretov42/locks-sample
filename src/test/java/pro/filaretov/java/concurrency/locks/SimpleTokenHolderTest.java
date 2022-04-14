package pro.filaretov.java.concurrency.locks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SimpleTokenHolderTest {

    @Mock
    private Supplier<Token> tokenSupplier;

    @Mock
    private Clock clock;

    @InjectMocks
    private SimpleTokenHolder tokenHolder;

    @BeforeEach
    void setUp() {
        when(tokenSupplier.get()).thenReturn(new Token(UUID.randomUUID().toString(), Instant.now().plusSeconds(20)));
        when(clock.instant()).thenReturn(Instant.now());
    }

    @Test
    void shouldCreateOnlyOneToken() {
        String token1 = tokenHolder.getToken();
        String token2 = tokenHolder.getToken();

        assertEquals(token1, token2);

        verify(tokenSupplier).get();
    }
}