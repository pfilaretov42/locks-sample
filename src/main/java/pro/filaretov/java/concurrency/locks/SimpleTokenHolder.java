package pro.filaretov.java.concurrency.locks;

import java.time.Clock;
import java.util.Objects;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleTokenHolder implements TokenHolder {

    private final Supplier<Token> tokenSupplier;
    private final Clock clock;

    private Token currentToken;

    @Override
    public String getToken() {
        if (currentToken == null || clock.instant().isAfter(currentToken.getValidUntil())) {
            currentToken = tokenSupplier.get();
        }

        return currentToken.getValue();
    }

    @Override
    public void invalidateToken(String token) {
        if (currentToken != null && Objects.equals(currentToken.getValue(), token)) {
            currentToken = null;
        }
    }
}
