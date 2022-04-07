package pro.filaretov.java.concurrency.locks;

import java.time.Instant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Token {
    private final String value;
    private final Instant validUntil;
}
