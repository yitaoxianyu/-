package com.nageoffer.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;

/**
 *
 *
 * @author xianyu
 * @since 2025-07-07 20:49:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_group")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupDO extends DatabaseDO implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  ID
     */
    private Long id;

    /**
     *  分组标识
     */
    private String gid;

    /**
     *  分组名称
     */
    private String name;

    /**
     *  创建分组用户名
     */
    private String username;

    /**
     *  分组排序
     */
    private Integer sortOrder;


}