package gov.cfpb.keycloak.authenticator.hmda;

/**
 * Created by keelerh on 3/29/17.
 */
public class ExternalIdType {

    private String code;
    private String name;

    public ExternalIdType() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ExternalIdType{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
