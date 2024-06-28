package com.study.consumer.controller;

import com.study.consumer.consumer.PictureConsumer;
import com.study.consumer.mapper.PictureDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SolController {

    private final PictureConsumer pictureConsumer;

    @GetMapping("/internal/pictures")
    public ResponseEntity<List<PictureDto>> getPicturesInternal(HttpServletRequest request) {
        var address = request.getRemoteAddr();
        var allPicturesByAddress = pictureConsumer.getAllByAddress(address);
        return ResponseEntity.ok(allPicturesByAddress);
    }
}

