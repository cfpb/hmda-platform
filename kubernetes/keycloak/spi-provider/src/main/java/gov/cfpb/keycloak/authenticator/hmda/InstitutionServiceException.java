package gov.cfpb.keycloak.authenticator.hmda;

/**
 * Created by keelerh on 2/8/17.
 */
public class InstitutionServiceException extends Exception {

    public InstitutionServiceException(String message) {
        super(message);
    }

    public InstitutionServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
