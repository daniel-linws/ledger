package com.zing.hsbc.ledgerservice.controller;

import com.zing.hsbc.ledgerservice.entity.EventEntity;
import com.zing.hsbc.ledgerservice.service.EventStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/event")
public class EventSourceController {

    @Autowired
    private EventStoreService eventStoreService;

    //for testing purpose only
    @GetMapping("/findAll")
    public List<EventEntity> findAll(){
        List<EventEntity> eventEntitys = eventStoreService.findAll();
        return eventEntitys;
    }
}

