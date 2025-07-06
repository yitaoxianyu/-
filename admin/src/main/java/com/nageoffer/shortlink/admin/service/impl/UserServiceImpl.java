package com.nageoffer.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.constants.UserCacheConstants;
import com.nageoffer.shortlink.admin.common.convention.exception.ClientException;
import com.nageoffer.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.nageoffer.shortlink.admin.dao.entity.UserDO;
import com.nageoffer.shortlink.admin.dao.mapper.UserMapper;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.ActualUserRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import com.nageoffer.shortlink.admin.util.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    private final RedissonClient redissonClient;

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
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }
        RLock lock = redissonClient.getLock(UserCacheConstants.LOCK_USER_REGISTER + username);
        if(!lock.tryLock()){
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }
        try{
            int insert = baseMapper.insert(BeanUtil.convert(requestParams, UserDO.class));
            if(insert < 1) throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);

            userRegisterCachePenetrationBloomFilter.add(username);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void updateUser(UserUpdateReqDTO requestParams) {
        // todo 根据token判断用户登录,判断是否更改的是自己的信息

        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                    .eq(UserDO::getUsername, requestParams.getUsername());
        baseMapper.update(BeanUtil.convert(requestParams,UserDO.class),updateWrapper);
    }
}
