package pl.com.xdms.domain.warehouse;

/**
 * Created on 22.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */

public enum WHTypeEnum {

    CC("CC"),
    XD("XD"),
    TXD("TXD");

    private String description;

    WHTypeEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
