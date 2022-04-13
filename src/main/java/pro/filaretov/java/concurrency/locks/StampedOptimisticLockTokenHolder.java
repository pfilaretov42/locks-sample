package pro.filaretov.java.concurrency.locks;

import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StampedOptimisticLockTokenHolder implements TokenHolder {

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
        long stamp = lock.tryOptimisticRead();
        try {
            while (true) {
                // possibly racy read
                // TODO - clone token instead of copying reference?
                final Token token = currentToken;
                if (lock.validate(stamp)) {
                    if (shouldUpdateToken.test(token)) {
                        // convert to write lock
                        stamp = lock.tryConvertToWriteLock(stamp);
                        if (stamp != 0L) {
                            // exclusive access
                            currentToken = supplier.get();
                            return currentToken;
                        }
                    } else {
                        return token;
                    }
                }

                // TODO - if we cannot get optimistic read, we go for write lock. But maybe we can try read lock first?
                stamp = lock.writeLock();
            }
        } finally {
            if (StampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }
}
