package com.jade.envelope.controller;

import com.jade.envelope.domain.EnvelopeInfo;
import com.jade.envelope.service.IEnvelopeInfoService;
import com.jade.envelope.service.IRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("record")
@RequiredArgsConstructor
public class RecordController {

    private final IRecordService iRecordService;

    private final IEnvelopeInfoService iEnvelopeInfoService;

    @GetMapping("query")
    public List<EnvelopeInfo> getEmptyRecord() {
        return iEnvelopeInfoService.getEmptyRecord();
    }

    @GetMapping("update")
    @Async("asyncExecutor")
    public void saveRecord() {
        iRecordService.startPool();
    }
}
