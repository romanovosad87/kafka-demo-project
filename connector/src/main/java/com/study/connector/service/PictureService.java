package com.study.connector.service;

import com.study.connector.mapper.PictureDto;
import com.study.connector.mapper.PictureMapper;
import com.study.connector.repository.PictureRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PictureService {

    private final PictureMapper pictureMapper;
    private final PictureRepository pictureRepository;

    public List<PictureDto> getAllPicturesByIpAddress(String ipAddress) {
        return pictureRepository.getPictureEntitiesByIpAddress(ipAddress).stream()
                .map(pictureMapper::pictureToDto)
                .toList();
    }
}
