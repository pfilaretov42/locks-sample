package pro.filaretov.java.concurrency.locks;

public interface TokenHolder {

    String getToken();
    void invalidateToken(String token);

}
