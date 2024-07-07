package com.study.connector.repository;

import com.study.connector.model.PictureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PictureRepository extends JpaRepository<PictureEntity, Integer> {

    @Query("select pe from PictureEntity pe join fetch pe.ipAddress ipe WHERE ipe.ipAddress =?1")
    List<PictureEntity> getPictureEntitiesByIpAddress(String address);
}

