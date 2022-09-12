package com.jade.envelope.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jade.envelope.domain.EnvelopeInfo;
import com.jade.envelope.domain.EnvelopeRecord;
import com.jade.envelope.domain.TinyEnvelope;
import com.jade.envelope.mapper.EnvelopeRecordMapper;
import com.jade.envelope.service.IEnvelopeInfoService;
import com.jade.envelope.service.IRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecordServiceImpl extends ServiceImpl<EnvelopeRecordMapper, EnvelopeRecord> implements IRecordService {

    @Autowired
    private IEnvelopeInfoService iEnvelopeInfoService;

    @Resource(name = "JacksonRedisTemplate")
    private RedisTemplate redisTemplate;

    public void envelopeDataSync() {
        List<EnvelopeInfo> envelopeInfoList = iEnvelopeInfoService.getEmptyRecord();
        if (envelopeInfoList.size() == 0) {
            return;
        }

        for (EnvelopeInfo ei : envelopeInfoList) {
            // 抢到的
            List<TinyEnvelope> tinyEnvelopeList = getTapped(ei.getEnvelopeId());
            // 未抢到的
            List<TinyEnvelope> tinyEnvelopeList1 = getUnTapped(ei.getEnvelopeId());
            if (tinyEnvelopeList1.size() != 0) {
                System.out.println("redis中查找到未被抢完的红包,id为" + ei.getEnvelopeId());
            }

            if (tinyEnvelopeList.size() == 0) {
                // 红包已经过期，status = 0
                iEnvelopeInfoService.updateStatus(ei);
                continue;
            }

            Long totalAmount = 0L;
            int totalNum = 0;
            for (TinyEnvelope te : tinyEnvelopeList) {
                EnvelopeRecord envelopeRecord = new EnvelopeRecord();
                envelopeRecord.setEnvelopeId(ei.getEnvelopeId());
                envelopeRecord.setNickName("testUser_" + te.getUid());
                envelopeRecord.setUid(te.getUid());
                envelopeRecord.setAccount(te.getAccount().intValue());
                envelopeRecord.setCreateTime(LocalDateTimeUtil.now());
                boolean exists = baseMapper.exists(Wrappers.<EnvelopeRecord>query().lambda().eq(EnvelopeRecord::getUid, te.getUid()).eq(EnvelopeRecord::getEnvelopeId, te.getEnvelopeId()));
                if (!exists) {
                    baseMapper.insert(envelopeRecord);
                    totalAmount += te.getAccount();
                    totalNum += 1;
                }
            }
            System.out.println("红包" + tinyEnvelopeList.get(0).getEnvelopeId() + ",共完成" + tinyEnvelopeList.size() + "条红包数据更新");
            //更新发布的红包信息
            ei.setRemainingAmount(ei.getRemainingAmount() - totalAmount.intValue());
            ei.setRemainingNumber(ei.getRemainingNumber() - totalNum);
            ei.setUpdateTime(LocalDateTimeUtil.now());
            if (ei.getRemainingAmount() == 0 && ei.getRemainingNumber() == 0) {
                // 持久化到数据库后删除redis中的数据
                redisTemplate.delete("envelopeConsumedList_" + ei.getEnvelopeId());
                redisTemplate.delete("envelopeConsumedMap_" + ei.getEnvelopeId());
                // 如果红包被抢完设置为不可用
                ei.setStatus(0);
            }
            iEnvelopeInfoService.updateEnvelopeInfo(ei);

        }
    }

    public List<TinyEnvelope> getTapped(Long envelopeId) {
        String envelopeConsumedListKey = "envelopeConsumedList_" + envelopeId;
        List<TinyEnvelope> results = new ArrayList<>();
        ListOperations<Serializable, Object> operations = redisTemplate.opsForList();
        List<Object> objects = operations.range(envelopeConsumedListKey, 0, -1);
        if (objects == null) {
            return null;
        }
        for (Object o : objects) {
            JSONObject jsonObject = new JSONObject(o);
            TinyEnvelope te = new TinyEnvelope();
            te.setUid(Integer.parseInt(jsonObject.get("uid").toString()));
            te.setEnvelopeId(Long.valueOf(jsonObject.get("envelopeId").toString()));
            te.setAccount(Long.valueOf(jsonObject.get("account").toString()));
            results.add(te);
        }
        return results;
    }

    public List<TinyEnvelope> getUnTapped(Long envelopeId) {
        String envelopeListKey = "envelopeList_" + envelopeId;
        List<TinyEnvelope> results = new ArrayList<>();
        ListOperations<Serializable, Object> operations = redisTemplate.opsForList();
        List<Object> objects = operations.range(envelopeListKey, 0, -1);
        if (objects == null) {
            return null;
        }
        for (Object o : objects) {
            TinyEnvelope st = new TinyEnvelope();
            JSONObject jsonObject = new JSONObject(o);
            st.setUid(Integer.parseInt(jsonObject.get("uid").toString()));
            st.setAccount(Long.valueOf(jsonObject.get("account").toString()));
            st.setEnvelopeId(Long.valueOf(jsonObject.get("envelopeId").toString()));
            results.add(st);
        }
        return results;
    }
}
