package pl.com.xdms.exception;

/**
 * Created on 26.10.2019
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
