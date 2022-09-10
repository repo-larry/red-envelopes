package com.jade.envelope.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jade.envelope.domain.EnvelopeInfo;
import com.jade.envelope.mapper.EnvelopeInfoMapper;
import com.jade.envelope.service.IEnvelopeInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnvelopeInfoServiceImpl extends ServiceImpl<EnvelopeInfoMapper, EnvelopeInfo> implements IEnvelopeInfoService {

    @Override
    public void insert(EnvelopeInfo envelopeInfo) {
        baseMapper.insert(envelopeInfo);
    }

    @Override
    public List<EnvelopeInfo> getEmptyRecord() {
        return baseMapper.selectList(Wrappers.<EnvelopeInfo>query().lambda().eq(EnvelopeInfo::getStatus, 1));
    }

    @Override
    public void updateStatus(EnvelopeInfo envelopeInfo) {
        UpdateWrapper<EnvelopeInfo> updateWrapper = new UpdateWrapper<>();
        envelopeInfo.setStatus(0);
//        updateWrapper.set("status", 0);
        baseMapper.update(envelopeInfo, updateWrapper);
    }

    @Override
    public boolean updateEnvelopeInfo(EnvelopeInfo envelopeInfo) {
        UpdateWrapper<EnvelopeInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("remaining_amount", envelopeInfo.getRemainingAmount());
        updateWrapper.set("remaining_number", envelopeInfo.getRemainingNumber());
        updateWrapper.set("update_time", envelopeInfo.getUpdateTime());
        updateWrapper.set("status", envelopeInfo.getStatus());
        baseMapper.update(envelopeInfo, updateWrapper);
        return Boolean.TRUE;
    }
}
