package com.nageoffer.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.biz.user.UserContext;
import com.nageoffer.shortlink.admin.common.convention.exception.ClientException;
import com.nageoffer.shortlink.admin.dao.entity.UserDO;
import com.nageoffer.shortlink.admin.dao.mapper.UserMapper;
import com.nageoffer.shortlink.admin.dto.req.UserLoginReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.ActualUserRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.GroupService;
import com.nageoffer.shortlink.admin.service.UserService;
import com.nageoffer.shortlink.admin.util.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.nageoffer.shortlink.admin.common.constants.UserCacheConstants.LOCK_USER_REGISTER;
import static com.nageoffer.shortlink.admin.common.constants.UserCacheConstants.USER_LOGIN;
import static com.nageoffer.shortlink.admin.common.enums.UserErrorCodeEnum.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    private final RedissonClient redissonClient;

    private final StringRedisTemplate stringRedisTemplate;

    private final GroupService groupService;

    @Override
    @SneakyThrows
    public UserRespDTO getUserByUsername(String username) {
        UserDO userDO = lambdaQuery().eq(UserDO::getUsername, username).one();
        return BeanUtil.convert(userDO, UserRespDTO.class);
    }

    @Override
    public ActualUserRespDTO getActualUserByUsername(String username) {
        UserDO userDO = lambdaQuery().eq(UserDO::getUsername, username).one();
        return BeanUtil.convert(userDO, ActualUserRespDTO.class);
    }

    /**
     *
     * @return true 代表存在
     */
    @Override
    public Boolean hasUser(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterReqDTO requestParams) {
        //查询用户名是否可用
        String username = requestParams.getUsername();
        if(hasUser(username)){
            throw new ClientException(USER_NAME_EXIST);
        }
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER + username);
        if(!lock.tryLock()){
            throw new ClientException(USER_NAME_EXIST);
        }
        try{
            int insert = baseMapper.insert(BeanUtil.convert(requestParams, UserDO.class));
            if(insert < 1) throw new ClientException(USER_SAVE_ERROR);
            //给用户添加一个默认分组
            groupService.saveGroup("默认分组",username);
            userRegisterCachePenetrationBloomFilter.add(username);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void updateUser(UserUpdateReqDTO requestParams) {
        //这里认为用户注册之后不能修改 username 了,username 作为分片键不能随意修改
        if (!Objects.equals(requestParams.getUsername(), UserContext.getUsername())) {
            throw new ClientException("当前登录用户修改请求异常");
        }
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                    .eq(UserDO::getUsername, requestParams.getUsername());
        baseMapper.update(BeanUtil.convert(requestParams,UserDO.class),updateWrapper);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParams) {
        if(!userRegisterCachePenetrationBloomFilter.contains(requestParams.getUsername())){
            throw new ClientException(USER_NULL);
        }

        UserDO userDO = lambdaQuery().eq(UserDO::getUsername, requestParams.getUsername())
                .eq(UserDO::getPassword, requestParams.getPassword()).one();
        if(userDO == null){
            throw new ClientException(USER_NULL);
        }
        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(USER_LOGIN + requestParams.getUsername());
        if (CollUtil.isNotEmpty(map)) {
            stringRedisTemplate.expire(USER_LOGIN + requestParams.getUsername(),30,TimeUnit.DAYS);
            String token = map.keySet().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElseThrow(() -> new ClientException("用户登录异常"));
            return new UserLoginRespDTO(token);
        }


        String token = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put(USER_LOGIN + requestParams.getUsername(),
                 token,JSONObject.toJSONString(userDO));
        stringRedisTemplate.expire(USER_LOGIN + requestParams.getUsername(),30, TimeUnit.DAYS);

        return new UserLoginRespDTO(token);
    }

    @Override
    public Boolean checkLogin(String username, String token) {
        if (!userRegisterCachePenetrationBloomFilter.contains(username)) {
            throw new ClientException(USER_NULL);
        }

        if (!stringRedisTemplate.hasKey(USER_LOGIN + username)) {
            throw new ClientException(USER_NULL);
        }

        Object result = stringRedisTemplate.opsForHash().get(USER_LOGIN + username, token);
        return result != null;
    }

    @Override
    public void logout(String username, String token) {
        if (!checkLogin(username,token)) {
            throw new ClientException("用户不存在或 token 错误");
        }

        stringRedisTemplate.opsForHash().delete(USER_LOGIN + username,token);
    }
}
