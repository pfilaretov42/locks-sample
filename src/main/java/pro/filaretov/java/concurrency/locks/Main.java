package pro.filaretov.java.concurrency.locks;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.SneakyThrows;

public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        MyServer server = new MyServer();

        Supplier<Token> tokenSupplier = () ->
            new Token(UUID.randomUUID().toString(), Instant.now().plus(5, ChronoUnit.SECONDS));
        TokenHolder tokenHolder = new SimpleTokenHolder(tokenSupplier, Clock.systemUTC());

        for (int i = 0; i <= 100; i++) {
            String token = tokenHolder.getToken();
            server.doStuff(token);

            Thread.sleep(500);
        }
    }
}
