package pl.com.xdms.domain.tpa;

/**
 * Created on 05.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public enum TPAEnum {

    CLOSED ("CLOSED"),
    DELAYED ("DELAYED"),
    IN_PROGRESS ("IN_PROGRESS"),
    BUFFER ("BUFFER");

    private String code;

    TPAEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
