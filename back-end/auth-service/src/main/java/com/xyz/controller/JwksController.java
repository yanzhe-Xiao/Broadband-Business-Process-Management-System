// web/JwksController.java
package com.xyz.controller;

import com.xyz.security.JwkKeyProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class JwksController {
    private final JwkKeyProvider keys;
    public JwksController(JwkKeyProvider keys) { this.keys = keys; }

    @GetMapping(value = "/jwks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> jwks() {
        return ResponseEntity.ok(keys.jwksJson());
    }
}
