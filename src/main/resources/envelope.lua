-- 如果用户已抢过红包，则返回nil
if redis.call('hexists', KEYS[3], KEYS[4]) ~= 0 then
    return nil
else
    -- 先取出一个小红包
    local envelope = redis.call('rpop', KEYS[1]);
    if envelope then
        local x = cjson.decode(envelope);
        -- 加入用户ID信息
        x['uid'] = KEYS[4];
        local res = cjson.encode(x);
        -- 把用户ID放到去重的set里
        redis.call('hset', KEYS[3], KEYS[4], KEYS[4]);
        -- 把红包放到已消费队列里
        redis.call('lpush', KEYS[2], res);
        return res;
    end
end
return nil
