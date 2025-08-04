local timeout = tonumber(ARGV[1])
local key = KEYS[1]

--对应的 key 加 ARGV
local res = redis.call('INCR', key)
if(res == 1) then
    redis.call('expire',key,timeout)
end

return res

