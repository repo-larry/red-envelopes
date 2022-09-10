package com.jade.envelope.controller;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.jade.envelope.domain.*;
import com.jade.envelope.service.IEnvelopeInfoService;
import com.jade.envelope.util.EnvelopeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("envelope")
public class EnvelopeController {

    @Autowired
    @Qualifier("JacksonRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private IEnvelopeInfoService iEnvelopeInfoService;

    // 发红包
    @PostMapping("add")
    public R<?> addEnvelope(@RequestBody EnvelopeSaveDTO envelopeSave) {
        if (envelopeSave.getKeepTime() <= 600) {
            return R.fail("有效时间必须大于10分钟");
        }
        EnvelopeInfo envelopeInfo = new EnvelopeInfo();
        int uid = envelopeSave.getUid();
        envelopeInfo.setUid(uid);
        envelopeInfo.setCreateTime(LocalDateTimeUtil.now());
        envelopeInfo.setUpdateTime(LocalDateTimeUtil.now());
        envelopeInfo.setKeepTime(envelopeSave.getKeepTime());
        envelopeInfo.setStatus(1);

        envelopeInfo.setAmount(envelopeSave.getAccount());
        envelopeInfo.setNumber(envelopeSave.getNumber());
        envelopeInfo.setRemainingAmount(envelopeSave.getAccount());
        envelopeInfo.setRemainingNumber(envelopeSave.getNumber());
        long envelopeId = System.currentTimeMillis();
        envelopeId = envelopeId + (long) (Math.random() * 99999) + 1;
        envelopeInfo.setEnvelopeId(envelopeId);

        long[] envelopeArray = EnvelopeUtil.generate(envelopeSave.getAccount(), envelopeSave.getNumber());
        List<TinyEnvelope> list = new ArrayList<>();
        for (long l : envelopeArray) {
            TinyEnvelope tinyEnvelope = new TinyEnvelope();
            tinyEnvelope.setAmount(l);
            tinyEnvelope.setEnvelopeId(envelopeId);
            tinyEnvelope.setUid(uid);
            list.add(tinyEnvelope);
        }
        TinyEnvelope[] envelopeArr = new TinyEnvelope[list.size()];
        list.toArray(envelopeArr);
        try {
            redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(TinyEnvelope.class));

            redisTemplate.opsForList().rightPushAll("envelopeList_" + envelopeId, envelopeArr);
            redisTemplate.expire("envelopeList_" + envelopeId, envelopeSave.getKeepTime(), TimeUnit.SECONDS);
            iEnvelopeInfoService.insert(envelopeInfo);
        } catch (Exception e) {
            throw new RuntimeException("创建红包失败");
        }
        return R.ok(envelopeId, "创建红包成功");
    }

    // 抢红包
    @PostMapping("get")
    public R<?> getEnvelope(@RequestBody EnvelopeGetDTO envelopeGetDTO) {
        long envelopeId = envelopeGetDTO.getEnvelopeId();
        Integer uid = envelopeGetDTO.getUid();
        String envelopeListKey = "envelopeList_" + envelopeId;
        String envelopeConsumedListKey = "envelopeConsumedList_" + envelopeId;
        String envelopeConsumedMapKey = "envelopeConsumedMap_" + envelopeId;
        Boolean b = redisTemplate.hasKey(envelopeListKey);
        if (Boolean.FALSE.equals(b)) {
            return R.fail("红包抢完啦~");
        }
        Long size = redisTemplate.opsForList().size(envelopeListKey);
        assert size != null;
        if (size <= 0) {
            return R.fail("红包抢完啦~");
        }

        TinyEnvelope te = luaExpress(envelopeListKey, envelopeConsumedListKey, envelopeConsumedMapKey, uid);
        if (te != null) {
            long amount = te.getAmount();
            return R.ok("恭喜抢到红包" + amount + "元");
        }
        return R.fail("您已经抢过啦~");
    }

    public TinyEnvelope luaExpress(String envelopeListKey, String envelopeConsumedListKey, String envelopeConsumedMapKey, Integer uid) {
        DefaultRedisScript<TinyEnvelope> lockScript = new DefaultRedisScript<>();
        lockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("envelope.lua")));
        lockScript.setResultType(TinyEnvelope.class);
        List<String> keys = new ArrayList<>();
        keys.add(envelopeListKey);
        keys.add(envelopeConsumedListKey);
        keys.add(envelopeConsumedMapKey);
        keys.add(Integer.toString(uid));
        return redisTemplate.execute(lockScript, keys);
    }

}
