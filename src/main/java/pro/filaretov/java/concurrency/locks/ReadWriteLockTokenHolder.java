package pro.filaretov.java.concurrency.locks;

import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReadWriteLockTokenHolder implements TokenHolder {

    private final Supplier<Token> tokenSupplier;
    private final Clock clock;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private Token currentToken;

    @Override
    public String getToken() {
        Predicate<Token> tokenIsExpired = token -> token == null || clock.instant().isAfter(token.getValidUntil());
        Token token = updateTokenIf(tokenIsExpired, tokenSupplier);
        return token.getValue();
    }

    @Override
    public void invalidateToken(String tokenString) {
        Predicate<Token> tokenIsInvalid = token -> token != null && Objects.equals(token.getValue(), tokenString);
        updateTokenIf(tokenIsInvalid, () -> null);
    }

    private Token updateTokenIf(Predicate<Token> shouldUpdateToken, Supplier<Token> supplier) {
        lock.readLock().lock();
        try {
            if (!shouldUpdateToken.test(currentToken)) {
                return currentToken;
            }
        } finally {
            lock.readLock().unlock();
        }

        // no token, need write lock to write it
        lock.writeLock().lock();
        try {
            if (shouldUpdateToken.test(currentToken)) {
                currentToken = supplier.get();
            }
            return currentToken;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
