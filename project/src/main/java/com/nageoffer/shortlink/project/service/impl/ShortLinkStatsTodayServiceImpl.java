package com.nageoffer.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkStatsTodayDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkStatsTodayMapper;
import com.nageoffer.shortlink.project.service.ShortLinkStatsTodayService;
import org.springframework.stereotype.Service;

@Service
public class ShortLinkStatsTodayServiceImpl extends ServiceImpl<ShortLinkStatsTodayMapper, ShortLinkStatsTodayDO> implements ShortLinkStatsTodayService {
}
