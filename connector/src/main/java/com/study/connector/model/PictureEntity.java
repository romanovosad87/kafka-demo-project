package com.study.connector.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "pictures")
public class PictureEntity {
    @Id
    private Integer sol;

    private String url;

    private Long size;

    @ManyToMany
    @JoinTable(name = "picture_ip_address",
    joinColumns = @JoinColumn(name = "sol"),
    inverseJoinColumns = @JoinColumn(name = "ip_address"))
    private List<IpAddressEntity> ipAddress = new ArrayList<>();

    public Integer getSol() {
        return sol;
    }

    public String getUrl() {
        return url;
    }

    public Long getSize() {
        return size;
    }

    public List<IpAddressEntity> getIpAddress() {
        return ipAddress;
    }

    public void setSol(Integer sol) {
        this.sol = sol;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public void setIpAddress(List<IpAddressEntity> ipAddress) {
        this.ipAddress = ipAddress;
    }
}
