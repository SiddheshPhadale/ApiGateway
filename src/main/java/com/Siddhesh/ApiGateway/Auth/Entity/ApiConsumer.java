package com.Siddhesh.ApiGateway.Auth.Entity;

import com.Siddhesh.ApiGateway.Subscriptions.Entity.Subscription;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@PrimaryKeyJoinColumn(name = "user_id")
@Table(name = "api_consumers")
public class ApiConsumer extends BaseUser {

    @OneToMany(mappedBy = "consumer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscription> subscriptions = new ArrayList<>();
}
