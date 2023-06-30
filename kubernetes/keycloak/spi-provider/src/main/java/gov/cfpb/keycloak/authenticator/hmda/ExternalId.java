package gov.cfpb.keycloak.authenticator.hmda;

import java.util.Objects;

/**
 * Created by keelerh on 1/25/17.
 */
public class ExternalId {

    private ExternalIdType externalIdType;
    private String value;

    public ExternalId() {
    }

    public ExternalIdType getExternalIdType() {
        return externalIdType;
    }

    public void setExternalIdType(ExternalIdType externalIdType) {
        this.externalIdType = externalIdType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ExternalId{" +
                "externalIdType=" + externalIdType +
                ", value='" + value + '\'' +
                '}';
    }
}
