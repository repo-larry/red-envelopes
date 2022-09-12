package com.jade.envelope.controller;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONObject;
import com.jade.envelope.domain.EnvelopeInfo;
import com.jade.envelope.domain.EnvelopeSaveDTO;
import com.jade.envelope.domain.R;
import com.jade.envelope.domain.TinyEnvelope;
import com.jade.envelope.service.IEnvelopeInfoService;
import com.jade.envelope.util.EnvelopeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("envelope")
public class EnvelopeController {

    @Resource(name = "JacksonRedisTemplate")
    private RedisTemplate redisTemplate;

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

        envelopeInfo.setAccount(envelopeSave.getAccount());
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
            tinyEnvelope.setAccount(l);
            tinyEnvelope.setEnvelopeId(envelopeId);
            tinyEnvelope.setUid(uid);
            list.add(tinyEnvelope);
        }
        TinyEnvelope[] envelopeArr = new TinyEnvelope[list.size()];
        list.toArray(envelopeArr);
        try {
            redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(TinyEnvelope.class));
            ListOperations<Serializable, Object> operations = redisTemplate.opsForList();
            final String key = "envelopeList_" + envelopeId;
            operations.rightPushAll(key, envelopeArr);
            redisTemplate.expire(key, envelopeSave.getKeepTime(), TimeUnit.SECONDS);
            iEnvelopeInfoService.insert(envelopeInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("创建红包失败");
        }
        return R.ok(envelopeId, "创建红包成功");
    }

    // 抢红包 @RequestBody EnvelopeGetDTO envelopeGetDTO
    @PostMapping("get")
    public R<?> getEnvelope(Long envelopeId) {
        Random random = new Random();
        int uid = random.nextInt(10000);
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
            long amount = te.getAccount();
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

        Object o = redisTemplate.execute(lockScript, keys);
        JSONObject js = new JSONObject(o);
        TinyEnvelope tinyEnvelope = new TinyEnvelope();
        tinyEnvelope.setUid(Integer.parseInt(js.get("uid").toString()));
        tinyEnvelope.setAccount(Long.valueOf(js.get("account").toString()));
        tinyEnvelope.setEnvelopeId(Long.valueOf(js.get("envelopeId").toString()));

        return tinyEnvelope;
    }

}
