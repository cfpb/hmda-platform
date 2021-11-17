package gov.cfpb.keycloak.authenticator.hmda;

import org.jboss.logging.Logger;

import javax.net.ssl.*;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;


public class InstitutionServiceHmdaApiImpl implements InstitutionService {

    private static final Logger logger = Logger.getLogger(InstitutionServiceHmdaApiImpl.class);

    private WebTarget apiClient;

    public InstitutionServiceHmdaApiImpl(String apiUri, Boolean validateSsl) {
        this.apiClient = buildClient(apiUri, validateSsl);
    }

    private WebTarget buildClient(String apiUri, Boolean validateSsl) {
        ClientBuilder apiClientBuilder = ClientBuilder.newBuilder();

        // Special handling for dealing with untrusted HTTPS calls
        if(!validateSsl) {
            try {
                TrustManager[] tm = new TrustManager[] {
                        new X509TrustManager() {
                            @Override public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}
                            @Override public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}
                            @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                        }};

                HostnameVerifier hv = new HostnameVerifier() {
                    @Override public boolean verify(String s, SSLSession sslSession) { return true; }
                };

                SSLContext sslCtx = SSLContext.getInstance("TLS");
                sslCtx.init(null, tm, new SecureRandom());

                apiClientBuilder.sslContext(sslCtx).hostnameVerifier(hv).build();
            } catch (NoSuchAlgorithmException |KeyManagementException ex) {
                throw new RuntimeException(ex);
            }

            logger.warn("SSL validation is disabled.  This should not be enabled in a production environemnt.");
        }

        apiClient = apiClientBuilder.build()
                .register(InstitutionSearchResultsReader.class)
                .target(apiUri)
                .path("institutions");

        return apiClient;
    }

    @Override
    public List<Institution> findInstitutionsByDomain(String domain) throws InstitutionServiceException {
        try {
            Response response = apiClient.queryParam("domain", domain).request(MediaType.APPLICATION_JSON_TYPE).get();
            Response.Status status = Response.Status.fromStatusCode(response.getStatus());

            switch (status) {
                case OK:
                    return response.readEntity(InstitutionSearchResults.class).getInstitutions();

                case NOT_FOUND:
                    return new ArrayList<>();

                default:
                    // TODO: Include details from error JSON message, if available.
                    logger.error("Unexpected response from institution search API while querying by domain=\""+domain+"\".  " +
                            "HTTP Status: " + status.getStatusCode() + " - " + status.getReasonPhrase());
                    throw new InstitutionServiceException("Error occurred while searching for institutions with domain '" + domain + "'");
            }

        } catch (Exception e) {
            throw new InstitutionServiceException("Error occurred while searching for institutions with domain \"" + domain + "\"", e);
        }
    }

}
