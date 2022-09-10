package com.jade.envelope.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jade.envelope.domain.EnvelopeInfo;
import com.jade.envelope.domain.EnvelopeRecord;
import com.jade.envelope.domain.TinyEnvelope;
import com.jade.envelope.mapper.EnvelopeRecordMapper;
import com.jade.envelope.service.IEnvelopeInfoService;
import com.jade.envelope.service.IRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecordServiceImpl extends ServiceImpl<EnvelopeRecordMapper, EnvelopeRecord> implements IRecordService {

    @Autowired
    private IEnvelopeInfoService iEnvelopeInfoService;

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate redisTemplate;

    @Override
    public void startPool() {
        List<EnvelopeInfo> envelopeInfoList = iEnvelopeInfoService.getEmptyRecord();
        if (envelopeInfoList.size() == 0) {
            System.out.println("没有未被持久化的红包信息");
            return;
        }

        for (EnvelopeInfo ei : envelopeInfoList) {
            // 抢到的
            List<TinyEnvelope> tinyEnvelopeList = getTapped(ei.getEnvelopeId());
            // 未抢到的
            List<TinyEnvelope> tinyEnvelopeList1 = getUnTapped(ei.getEnvelopeId());
            if (tinyEnvelopeList1.size() != 0) {
                System.out.println("redis中查找到未被抢完的红包,id为" + ei.getEnvelopeId());
                continue;
            }

            if (tinyEnvelopeList.size() == 0) {
                System.out.println("redis中未查询到红包号为" + ei.getEnvelopeId() + "的红包,出错");
                //将status置为0,表示红包已经过期
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
                envelopeRecord.setAmount(te.getAmount().intValue());
                envelopeRecord.setCreateTime(LocalDateTimeUtil.now());
                baseMapper.insert(envelopeRecord);
                totalAmount += te.getAmount();
                totalNum += 1;
            }
            System.out.println("持久化到数据库完成，共插入" + tinyEnvelopeList.size() + "条红包数据");
            //更新发布的红包信息
            ei.setRemainingAmount(ei.getRemainingAmount() - totalAmount.intValue());
            ei.setRemainingNumber(ei.getRemainingNumber() - totalNum);
            ei.setUpdateTime(LocalDateTimeUtil.now());
            if (ei.getRemainingAmount() == 0 && ei.getRemainingNumber() == 0) {
                //持久化到数据库后删除redis中的数据
                redisTemplate.delete("envelopeConsumedList_" + ei.getEnvelopeId());
                redisTemplate.delete("envelopeConsumedMap_" + ei.getEnvelopeId());
                //如果红包被抢完设置为不可用
                ei.setStatus(0);
            }
            if (iEnvelopeInfoService.updateEnvelopeInfo(ei)) {
                System.out.println("红包发布信息更新成功");
            } else {
                System.out.println("红包发布信息更新失败");
            }
        }
    }

    public List<TinyEnvelope> getTapped(Long envelopeId) {
        String envelopeConsumedListKey = "envelopeConsumedList_" + envelopeId;
        List<TinyEnvelope> results = new ArrayList<>();
        ListOperations<Serializable, Object> operations = redisTemplate.opsForList();
        List<Object> objects = operations.range(envelopeConsumedListKey, 0, -1);
        if (objects == null) {
            System.out.println("未查询到此红包信息");
            return null;
        }
        for (Object o : objects) {
            JSONObject jsonObject = new JSONObject(o);
            TinyEnvelope te = new TinyEnvelope();
            te.setUid(Integer.parseInt(jsonObject.get("uid").toString()));
            te.setEnvelopeId(Long.valueOf(jsonObject.get("envelopeId").toString()));
            te.setAmount(Long.valueOf(jsonObject.get("amount").toString()));
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
            st.setAmount(Long.valueOf(jsonObject.get("amount").toString()));
            st.setEnvelopeId(Long.valueOf(jsonObject.get("envelopeId").toString()));
            results.add(st);
        }
        return results;
    }
}
