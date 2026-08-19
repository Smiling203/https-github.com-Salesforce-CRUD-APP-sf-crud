package com.pallavi.sf_crud.service;

import com.pallavi.sf_crud.config.SalesforceSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class SalesforceApiService {

    @Value("${salesforce.api-version}")
    private String apiVersion;

    private final SalesforceSession session;
    private final WebClient webClient = WebClient.builder().build();

    private static final Map<String, List<String>> OBJECT_FIELDS = Map.of(
            "Account",     List.of("Id", "Name", "Industry", "Phone", "Website", "BillingCity"),
            "Opportunity", List.of("Id", "Name", "StageName", "Amount", "CloseDate", "Probability"),
            "Lead",        List.of("Id", "Name", "Company", "Status", "Email", "Phone"),
            "Contact",     List.of("Id", "FirstName", "LastName", "Email", "Phone", "Title"),
            "Case",        List.of("Id", "Subject", "Status", "Priority", "Origin", "CaseNumber")
    );

    public SalesforceApiService(SalesforceSession session) {
        this.session = session;
    }

    public List<String> getFields(String objectName) {
        return OBJECT_FIELDS.getOrDefault(objectName, List.of("Id", "Name"));
    }

    private String baseUrl() {
        return session.getInstanceUrl() + "/services/data/" + apiVersion;
    }

    private WebClient.RequestHeadersSpec<?> auth(WebClient.RequestHeadersSpec<?> spec) {
        return spec.header("Authorization", "Bearer " + session.getAccessToken());
    }

    public Map<String, Object> listRecords(String objectName, int limit, int offset) {
        String fields = String.join(",", getFields(objectName));
        String soql = "SELECT " + fields + " FROM " + objectName
                + " LIMIT " + limit + " OFFSET " + offset;
        // Pass a URI object to WebClient so the SOQL query is encoded exactly once.
        URI uri = UriComponentsBuilder.fromUriString(baseUrl() + "/query")
                .queryParam("q", soql)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        return (Map<String, Object>) auth(webClient.get().uri(uri))
                .retrieve().bodyToMono(Map.class).block();
    }

    public Map<String, Object> createRecord(String objectName, Map<String, Object> fieldValues) {
        return (Map<String, Object>) auth(webClient.post()
                        .uri(baseUrl() + "/sobjects/" + objectName)
                        .header("Content-Type", "application/json")
                        .bodyValue(fieldValues))
                .retrieve().bodyToMono(Map.class).block();
    }

    public void updateRecord(String objectName, String id, Map<String, Object> fieldValues) {
        auth(webClient.patch()
                        .uri(baseUrl() + "/sobjects/" + objectName + "/" + id)
                        .header("Content-Type", "application/json")
                        .bodyValue(fieldValues))
                .retrieve().toBodilessEntity().block();
    }

    public void deleteRecord(String objectName, String id) {
        auth(webClient.delete()
                        .uri(baseUrl() + "/sobjects/" + objectName + "/" + id))
                .retrieve().toBodilessEntity().block();
    }
}
