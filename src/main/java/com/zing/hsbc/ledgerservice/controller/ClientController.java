package com.zing.hsbc.ledgerservice.controller;
import com.zing.hsbc.ledgerservice.entity.Client;
import com.zing.hsbc.ledgerservice.entity.WalletSubscription;
import com.zing.hsbc.ledgerservice.repo.ClientRepository;
import com.zing.hsbc.ledgerservice.service.WalletSubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private WalletSubscriptionService subscriptionService;
    @PostMapping("/update")
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        Client savedClient = clientRepository.save(client);
        return ResponseEntity.ok(savedClient);
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<Client>> getAllEntities() {
        List<Client> entities = clientRepository.findAll();
        return ResponseEntity.ok(entities);
    }
    @PostMapping("/wallet/subscribe")
    public ResponseEntity<WalletSubscription> subscribe(@RequestBody WalletSubscription subscription) {
        WalletSubscription savedSubscription = subscriptionService.subscribe(subscription.getClientId(), subscription.getWalletId());
        return ResponseEntity.ok(savedSubscription);
    }

    @PostMapping("/wallet/unsubscribe")
    public ResponseEntity<Void> unsubscribe(@RequestBody WalletSubscription subscription) {
        subscriptionService.unsubscribe(subscription.getClientId(), subscription.getWalletId());
        return ResponseEntity.ok().build();
    }

    //for testing purpose
    @GetMapping("/subscription/findAll")
    public List<WalletSubscription> getAllSubscription() {
        List<WalletSubscription> walletSubscriptions = subscriptionService.findAll();
        return walletSubscriptions;
    }
}
