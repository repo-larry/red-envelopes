package com.jade.envelope.service;

import com.jade.envelope.domain.EnvelopeInfo;

import java.util.List;

public interface IEnvelopeInfoService {

    void insert(EnvelopeInfo envelopeInfo);

    List<EnvelopeInfo> getEmptyRecord();

    void updateStatus(EnvelopeInfo envelopeInfo);

    boolean updateEnvelopeInfo(EnvelopeInfo envelopeInfo);
}
