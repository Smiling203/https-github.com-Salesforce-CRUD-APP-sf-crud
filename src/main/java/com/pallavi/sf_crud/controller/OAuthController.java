package com.pallavi.sf_crud.controller;

import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import com.pallavi.sf_crud.config.SalesforceSession;

@RestController
@RequestMapping("/oauth")
public class OAuthController {

    @Value("${salesforce.client-id}")
    private String clientId;

    @Value("${salesforce.client-secret}")
    private String clientSecret;

    @Value("${salesforce.redirect-uri}")
    private String redirectUri;

    @Value("${salesforce.auth-url}")
    private String authUrl;

    @Value("${salesforce.token-url}")
    private String tokenUrl;

    private final SalesforceSession session;
    private final WebClient webClient = WebClient.builder().build();

    public OAuthController(SalesforceSession session) {
        this.session = session;
    }

    // Step 1: redirect browser to Salesforce login/consent screen
    @GetMapping("/login")
    public RedirectView login() {
        String codeVerifier = createCodeVerifier();
        session.setCodeVerifier(codeVerifier);
        String url = UriComponentsBuilder.fromUriString(authUrl)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("code_challenge", createCodeChallenge(codeVerifier))
                .queryParam("code_challenge_method", "S256")
                .build()
                .encode()
                .toUriString();
        return new RedirectView(url);
    }

    // Step 2: Salesforce redirects back here with ?code=xxxx
    @GetMapping("/callback")
    public Object callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription) {
        if (error != null) {
            String message = errorDescription == null ? error : error + ": " + errorDescription;
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Salesforce login failed", "details", message));
        }
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Salesforce did not return an authorization code."));
        }
        Map<String, Object> response = webClient.post()
                .uri(tokenUrl)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("code", code)
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("redirect_uri", redirectUri)
                        .with("code_verifier", session.getCodeVerifier()))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        session.setAccessToken((String) response.get("access_token"));
        session.setInstanceUrl((String) response.get("instance_url"));

        return new RedirectView("/index.html?loggedIn=true");
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("loggedIn", session.isLoggedIn());
    }

    private String createCodeVerifier() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String createCodeChallenge(String verifier) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
