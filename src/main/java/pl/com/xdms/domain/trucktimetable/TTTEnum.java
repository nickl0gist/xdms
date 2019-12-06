package pl.com.xdms.domain.trucktimetable;

/**
 * Created on 05.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public enum TTTEnum {

    PENDING ("PENDING"),
    DELAYED ("DELAYED"),
    ARRIVED ("ARRIVED");

    private String code;

    TTTEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
