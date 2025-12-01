package kilic.yunus.stores.exception;

/**
 * Exception thrown when store data cannot be loaded or found.
 */
public class StoreDataException extends RuntimeException {

    public StoreDataException(String message) {
        super(message);
    }

    public StoreDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
