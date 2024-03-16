package com.zing.hsbc.ledgerservice.controller;
import com.zing.hsbc.ledgerservice.entity.Client;
import com.zing.hsbc.ledgerservice.repo.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    @Autowired
    private ClientRepository clientRepo;

    @PostMapping
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        Client savedClient = clientRepo.save(client);
        return ResponseEntity.ok(savedClient);
    }

    @GetMapping
    public ResponseEntity<List<Client>> getAllEntities() {
        List<Client> entities = clientRepo.findAll();
        return ResponseEntity.ok(entities);
    }
}
