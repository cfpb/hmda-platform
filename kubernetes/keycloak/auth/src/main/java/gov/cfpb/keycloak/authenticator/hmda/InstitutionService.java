package gov.cfpb.keycloak.authenticator.hmda;

import java.util.List;

public interface InstitutionService {

    public List<Institution> findInstitutionsByDomain(String domain) throws InstitutionServiceException;

}
