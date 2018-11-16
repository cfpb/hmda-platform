package gov.cfpb.keycloak.authenticator.hmda;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.*;
import org.keycloak.authentication.forms.RegistrationPage;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ConfiguredProvider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.validation.Validation;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;


public class HmdaValidInstitutionsFormAction implements FormAction, FormActionFactory, ConfiguredProvider {

    public static final String PROVIDER_ID = "registration-institution-action";
    public static final String FIELD_INSTITUTIONS = "user.attributes.institutions";
    public static final String MISSING_INSTITUTION_MESSAGE = "missingInstitutionMessage";
    public static final String INVALID_INSTITUTION_MESSAGE = "invalidInstitutionMessage";
    public static final String UNKNOWN_EMAIL_DOMAIN_MESSAGE = "unknownEmailDomainMessage";
    public static final String INSTITUTION_ERROR_MESSAGE = "institutionErrorMessage";

    private InstitutionService institutionService;

    private static final Logger logger = Logger.getLogger(HmdaValidInstitutionsFormAction.class);

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {
    }

    @Override
    public void validate(ValidationContext context) {

        String domain = null;

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        List<FormMessage> errors = new ArrayList<>();

        // Get email field.  If not populated, there is no point in proceeding.  The email field check is
        // already checked by core Keycloak, so there's no need for a dupe context.error() here.
        String emailFieldVal = formData.getFirst(RegistrationPage.FIELD_EMAIL);
        if(Validation.isBlank(emailFieldVal)) {
            context.success();

            return;
        }

        // Get the institutions field, returning with error if not set.
        String instFieldVal = formData.getFirst(FIELD_INSTITUTIONS);
        if (Validation.isBlank(instFieldVal)) {
            errors.add(new FormMessage(FIELD_INSTITUTIONS, MISSING_INSTITUTION_MESSAGE));
            context.validationError(formData, errors);
            context.error(Errors.INVALID_REGISTRATION);

            return;
        }

        Set<String> userInstIds = new HashSet<>(Arrays.asList(instFieldVal.split(",")));

        try {
            domain = emailFieldVal.split("@")[1];

            List<Institution> domainInsts = institutionService.findInstitutionsByDomain(domain);

            logger.info(domainInsts.size() + " institution(s) found for domain \"" + domain + "\"");

            if (domainInsts.isEmpty()) {
                errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, UNKNOWN_EMAIL_DOMAIN_MESSAGE, domain));
                context.validationError(formData, errors);
                context.error(Errors.INVALID_REGISTRATION);

                return;
            }

            // Get a set of institutionId(s) for a given domain
            Set<String> domainInstIds = new HashSet<>(domainInsts.size());
            for (Institution domainInst : domainInsts) {
                domainInstIds.add(domainInst.getId());
            }

            // Add error for every institution submitted not associated with domain
            if (!domainInsts.containsAll(userInstIds)) {
                Set<String> invalidInstIds = new HashSet<>(userInstIds);
                invalidInstIds.removeAll(domainInstIds);

                for (String invalidInstId : invalidInstIds) {
                    logger.warn("User submitted invalid institution ID " + invalidInstId + " for domain \"" + domain + "\"");
                    errors.add(new FormMessage(FIELD_INSTITUTIONS, INVALID_INSTITUTION_MESSAGE, invalidInstId, domain));
                }
            }

        } catch (Exception e) {
            logger.error("Error occurred while validating institution(s) " + userInstIds + " against \"" + domain + "\" domain", e);
            errors.add(new FormMessage(FIELD_INSTITUTIONS, INSTITUTION_ERROR_MESSAGE, domain));
        }

        if (errors.isEmpty()) {
            context.success();
        } else {
            context.validationError(formData, errors);
            context.error(Errors.INVALID_REGISTRATION);
        }

    }

    @Override
    public void success(FormContext form) {
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public FormAction create(KeycloakSession keycloakSession) {
        return this;
    }

    @Override
    public void init(Config.Scope scope) {
        String uri = scope.get("institutionSearchUri");
        Boolean validateSsl = scope.getBoolean("institutionSearchValidateSsl", true);

        logger.info("Initializing institution search: uri=" + uri + ", validateSsl=" + validateSsl);

        institutionService = new InstitutionServiceHmdaApiImpl(uri, validateSsl);
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {
    }


    // FormActionFactor methods
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Institution Validation";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        AuthenticationExecutionModel.Requirement[] choices = {
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.DISABLED
        };

        return choices;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Adds Institution verification per email domain";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        //TODO: Fill in uri and validateSsl here
        return null;
    }

}
