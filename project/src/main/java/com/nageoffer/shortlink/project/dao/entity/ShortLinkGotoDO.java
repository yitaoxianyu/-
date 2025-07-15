package com.nageoffer.shortlink.project.dao.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;



/**

 * 短链接跳转表

 *

 * @author xianyu

 * @since 2025-07-15 16:23:48

 */

@Data
@Builder
@TableName("t_link_goto")
public class ShortLinkGotoDO implements Serializable{



    private static final long serialVersionUID=1L;


    /**

     *  主键 id

     */

    private Long id;



    /**

     *  完整短链接

     */

    private String fullShortUrl;



    /**

     *  分组 id

     */

    private String gid;




}