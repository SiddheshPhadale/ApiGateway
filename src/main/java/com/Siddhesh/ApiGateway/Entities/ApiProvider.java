package com.Siddhesh.ApiGateway.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Entity
@Data
@Table(name = "Api_Owner")
@PrimaryKeyJoinColumn
@ToString(exclude = "apiList")
public class ApiProvider extends BaseUser{
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private List<ApiBody> apiList;
}
