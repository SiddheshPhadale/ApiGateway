package com.Siddhesh.ApiGateway.Auth.Entity;

import com.Siddhesh.ApiGateway.Api.Entity.ApiBody;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "Api_Owner")
@PrimaryKeyJoinColumn(name = "user_id")
public class ApiProvider extends BaseUser {

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApiBody> apiList = new ArrayList<>();
}
