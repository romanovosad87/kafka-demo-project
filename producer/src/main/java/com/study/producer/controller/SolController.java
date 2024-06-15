package com.study.producer.controller;

import com.study.producer.producer.SolProducer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SolController {

    private final SolProducer solProducer;

    @PostMapping("/sol")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void getLargestPicture(@RequestBody InputData data, HttpServletRequest request) {
        var address = request.getRemoteAddr();
        solProducer.send(data, address);
    }
}

