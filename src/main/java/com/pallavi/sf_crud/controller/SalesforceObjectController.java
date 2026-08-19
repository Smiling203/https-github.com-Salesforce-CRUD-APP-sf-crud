package com.pallavi.sf_crud.controller;

import com.pallavi.sf_crud.config.SalesforceSession;
import com.pallavi.sf_crud.service.SalesforceApiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SalesforceObjectController {

    private final SalesforceApiService sfService;
    private final SalesforceSession session;

    public SalesforceObjectController(SalesforceApiService sfService, SalesforceSession session) {
        this.sfService = sfService;
        this.session = session;
    }

    private ResponseEntity<?> requireLogin() {
        if (!session.isLoggedIn()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not logged in. Call /oauth/login first."));
        }
        return null;
    }

    @GetMapping("/{objectName}/fields")
    public ResponseEntity<?> fields(@PathVariable String objectName) {
        ResponseEntity<?> guard = requireLogin();
        if (guard != null) return guard;
        return ResponseEntity.ok(sfService.getFields(objectName));
    }

    @GetMapping("/{objectName}")
    public ResponseEntity<?> list(@PathVariable String objectName,
                                   @RequestParam(defaultValue = "20") int limit,
                                   @RequestParam(defaultValue = "0") int offset) {
        ResponseEntity<?> guard = requireLogin();
        if (guard != null) return guard;
        return ResponseEntity.ok(sfService.listRecords(objectName, limit, offset));
    }

    @PostMapping("/{objectName}")
    public ResponseEntity<?> create(@PathVariable String objectName, @RequestBody Map<String, Object> body) {
        ResponseEntity<?> guard = requireLogin();
        if (guard != null) return guard;
        return ResponseEntity.ok(sfService.createRecord(objectName, body));
    }

    @PatchMapping("/{objectName}/{id}")
    public ResponseEntity<?> update(@PathVariable String objectName, @PathVariable String id,
                                     @RequestBody Map<String, Object> body) {
        ResponseEntity<?> guard = requireLogin();
        if (guard != null) return guard;
        sfService.updateRecord(objectName, id, body);
        return ResponseEntity.ok(Map.of("status", "updated"));
    }

    @DeleteMapping("/{objectName}/{id}")
    public ResponseEntity<?> delete(@PathVariable String objectName, @PathVariable String id) {
        ResponseEntity<?> guard = requireLogin();
        if (guard != null) return guard;
        sfService.deleteRecord(objectName, id);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
}
