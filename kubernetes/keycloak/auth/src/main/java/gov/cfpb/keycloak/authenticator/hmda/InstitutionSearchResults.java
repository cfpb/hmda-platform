package gov.cfpb.keycloak.authenticator.hmda;

import java.util.List;


public class InstitutionSearchResults {

    private List<Institution> institutions;

    public InstitutionSearchResults() {
    }

    public List<Institution> getInstitutions() {
        return institutions;
    }

    public void setInstitutions(List<Institution> institutions) {
        this.institutions = institutions;
    }

    @Override
    public String toString() {
        return "InstitutionSearchResults{" +
                "institutions=" + institutions +
                '}';
    }
}
