package com.study.connector.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ip_addresses")
public class IpAddressEntity {
    @Id
    @Column(name = "ip_address")
    private String ipAddress;
}
