package pro.filaretov.java.concurrency.locks;

import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StampedLockTokenHolder implements TokenHolder {

    private final Supplier<Token> tokenSupplier;
    private final Clock clock;

    private final StampedLock lock = new StampedLock();

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
        long stamp = lock.readLock();
        try {
            while (shouldUpdateToken.test(currentToken)) {
                final long writeStamp = lock.tryConvertToWriteLock(stamp);
                if (writeStamp != 0) {
                    // exclusive access
                    stamp = writeStamp;
                    currentToken = supplier.get();
                    return currentToken;
                }

                // failed to convert lock, need to unlock read lock
                lock.unlockRead(stamp);
                stamp = lock.writeLock();
            }
            return currentToken;
        } finally {
            lock.unlock(stamp);
        }
    }
}
