package com.study.connector.controller;

import com.study.connector.mapper.PictureDto;
import com.study.connector.service.PictureService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


@RestController
@RequiredArgsConstructor
public class Controller {

    private final PictureService pictureService;


    @GetMapping(value = "/db/picture")
    public ResponseEntity<List<PictureDto>> getAllFromDb(HttpServletRequest request) {
        var address = request.getRemoteAddr();
        var allPictures = pictureService.getAllPicturesByIpAddress(address);
        return ResponseEntity.ok(allPictures);
    }
}

