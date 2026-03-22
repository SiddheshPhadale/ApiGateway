package com.Siddhesh.ApiGateway.Entities;

import jakarta.persistence.*;
import lombok.Data;

    @Entity
    @Data
    @Table(name = "subscriptions")
    public class Subscription {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long subId;
        @ManyToOne
        @JoinColumn(name = "api_id")
        private ApiBody api;
        @ManyToOne
        @JoinColumn(name = "user_id")
        private ApiConsumer consumer;
        @Column(unique = true, nullable = false)
        private String apiKey;
        @Enumerated(EnumType.STRING)
        private Status status;
    }




