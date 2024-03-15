package com.zing.hsbc.ledgerservice.service;

import com.zing.hsbc.ledgerservice.entity.EventEntity;
import com.zing.hsbc.ledgerservice.eventSource.BaseEvent;
import com.zing.hsbc.ledgerservice.helper.Utils;
import com.zing.hsbc.ledgerservice.repo.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EventStoreService {
    @Autowired
    private EventRepository eventRepository;


    public void saveEvent(Long aggregateId, BaseEvent event) throws JsonProcessingException {
        EventEntity eventEntity = new EventEntity();
        eventEntity.setAggregateId(aggregateId.toString());
        eventEntity.setEventType(event.getClass().getSimpleName());
        eventEntity.setPayload(Utils.getMapper().writeValueAsString(event));
        eventEntity.setTimestamp(LocalDateTime.now());
        eventRepository.save(eventEntity);
    }
}
