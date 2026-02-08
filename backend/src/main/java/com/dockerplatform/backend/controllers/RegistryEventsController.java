package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.dto.RegistryNotification;
import com.dockerplatform.backend.service.RegistryTagSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/registry")
public class RegistryEventsController {

    @Autowired
    private RegistryTagSyncService syncService;

    public RegistryEventsController(RegistryTagSyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/events")
    public ResponseEntity<Void> onEvent(@RequestBody(required = false) RegistryNotification payload) {
        try {
            syncService.handleNotification(payload);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.ok().build();
    }

}
