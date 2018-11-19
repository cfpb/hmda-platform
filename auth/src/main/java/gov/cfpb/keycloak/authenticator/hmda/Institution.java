package gov.cfpb.keycloak.authenticator.hmda;

import java.util.Objects;
import java.util.Set;

public class Institution {

    private String id;
    private String name;
    private Set<String> domains;
    private Set<ExternalId> externalIds;

    public Institution() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getDomains() {
        return domains;
    }

    public void setDomains(Set<String> domains) {
        this.domains = domains;
    }

    public Set<ExternalId> getExternalIds() {
        return externalIds;
    }

    public void setExternalIds(Set<ExternalId> externalIds) {
        this.externalIds = externalIds;
    }

    @Override
    public String toString() {
        return "Institution{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", domains=" + domains +
                ", externalIds=" + externalIds +
                '}';
    }
}
