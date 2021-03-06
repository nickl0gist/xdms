package pl.com.xdms.domain.user;

/**
 * Created on 16.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public enum RoleEnum {

    ADMIN_ROLE ("ADMIN_ROLE"),
    USER_ROLE ("USER_ROLE"),
    GUEST_ROLE ("GUEST_ROLE"),
    PLANNER_ROLE ("PLANNER_ROLE"),
    TRAFFIC_ROLE ("TRAFFIC_ROLE");

    private String code;

    RoleEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
