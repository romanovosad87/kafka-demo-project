package com.study.ksqldb.controller;

import com.study.ksqldb.Statistic;
import com.study.ksqldb.dto.StatisticDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ksqldb")
public class Controller {

    private final Statistic statistic;

    @GetMapping("/statistic/address")
    public ResponseEntity<StatisticDto> getStatisticByAddressKsqldb(HttpServletRequest request) {
        var address = request.getRemoteAddr();
        return ResponseEntity.ok(statistic.getStatisticByAddress(address));
    }

    @GetMapping("/statistic")
    public ResponseEntity<List<StatisticDto>> getAllStatisticKsqldb() {
        return ResponseEntity.ok(statistic.getStatistic());
    }
}
