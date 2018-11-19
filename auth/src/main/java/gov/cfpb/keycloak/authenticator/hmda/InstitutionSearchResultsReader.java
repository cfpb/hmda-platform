package gov.cfpb.keycloak.authenticator.hmda;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class InstitutionSearchResultsReader implements MessageBodyReader<InstitutionSearchResults> {

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return false;
    }

    @Override
    public InstitutionSearchResults readFrom(
            Class<InstitutionSearchResults> clazz,
            Type type,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> multivaluedMap,
            InputStream inputStream
    ) throws IOException, WebApplicationException {

        if(!mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
            throw new WebApplicationException("'"+mediaType+"' media type is not supported");
        }

        return new ObjectMapper().readValue(inputStream, InstitutionSearchResults.class);
    }

}
