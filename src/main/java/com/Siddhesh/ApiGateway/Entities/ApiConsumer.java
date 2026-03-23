package com.Siddhesh.ApiGateway.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Entity
@Data
@PrimaryKeyJoinColumn
@Table(name = "api_consumers")
@ToString(exclude = "subscriptions")
public class ApiConsumer extends BaseUser{
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private List<Subscription> subscriptions;
}
