package sibw.stream.twitter;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import sibw.sibw.Sibw;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

public class ServiceFactory {

    public static Sibw getInstance(String rootUrl, String p12path) {
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();

        GoogleCredential.Builder credentialBuilder = new GoogleCredential.Builder();
        credentialBuilder.setTransport(httpTransport);
        credentialBuilder.setJsonFactory(jsonFactory);

        // NOTE: use service account EMAIL (not client id)
        credentialBuilder.setServiceAccountId(Constants.CLIENT_EMAIL);
        credentialBuilder.setServiceAccountScopes(Collections.singleton(Constants.EMAIL_SCOPE));
        try {
            File p12file = new File(p12path);
            credentialBuilder.setServiceAccountPrivateKeyFromP12File(p12file);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        GoogleCredential credential = credentialBuilder.build();

        Sibw.Builder builder = new Sibw.Builder(new NetHttpTransport(), new JacksonFactory(), credential);
        builder.setApplicationName(Constants.APP_NAME);
        builder.setRootUrl(rootUrl);

        final Sibw service = builder.build();
        return service;
    }
}
