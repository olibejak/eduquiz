package cz.cvut.fel.bp.userservice.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import cz.cvut.fel.bp.userservice.service.util.GoogleUserData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Slf4j
@Service
public class GoogleAuthService {

    @Value("${app.security.google.client-id}")
    private String googleClientId;

    public GoogleUserData extractUserDataFromGoogleToken(String tokenString) {
        return extractUserData(getValidGoogleIdTokenPayload(tokenString));
    }

    public String extractSubjectFromGoogleToken(String tokenString) {
        return extractSubject(getValidGoogleIdTokenPayload(tokenString));
    }

    private GoogleIdToken.Payload getValidGoogleIdTokenPayload(String tokenString) {
        try {
            GoogleIdToken idToken = verifyGoogleIdToken(tokenString);

            if (idToken != null) {
                return idToken.getPayload();
            } else {
                log.debug("Invalid or expired google token");
                throw new IllegalArgumentException("Invalid or expired google token");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error verifying google token", e);
            throw new RuntimeException("An error occurred while verifying google token: " + e.getMessage(), e);
        }
    }

    private GoogleIdToken verifyGoogleIdToken(String tokenString) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory()
        )
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        return verifier.verify(tokenString);
    }

    private String extractSubject(GoogleIdToken.Payload payload) {
        return payload.getSubject();
    }

    private GoogleUserData extractUserData(GoogleIdToken.Payload payload) {
        return GoogleUserData.builder()
                .name((String) payload.get("name"))
                .email(payload.getEmail())
                .oidcSubject(payload.getSubject())
                .build();
    }
}