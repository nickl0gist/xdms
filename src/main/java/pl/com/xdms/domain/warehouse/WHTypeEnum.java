package pl.com.xdms.domain.warehouse;

/**
 * Created on 22.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public enum WHTypeEnum {

    CC("Consolidation Centre"),
    XD("Cross Dock");

    private String code;

    WHTypeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
