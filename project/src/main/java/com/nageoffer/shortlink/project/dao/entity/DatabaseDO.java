package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

@Data
public class DatabaseDO {

    /**

     *  创建时间

     */
    @TableField(fill = FieldFill.INSERT)
    protected Date createTime;


    /**

     *  修改时间

     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    protected Date updateTime;


    /**

     *  删除标识 0：未删除 1：已删除

     */
    @TableField(fill = FieldFill.INSERT)
    protected Integer delFlag;
}
