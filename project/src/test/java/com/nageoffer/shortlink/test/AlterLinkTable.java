package com.nageoffer.shortlink.test;

public class AlterLinkTable {
    static final String SQL = "alter table t_link_%d add column `total_pv` int(11) DEFAULT '0' COMMENT '历史PV';\n" +
            "alter table t_link_%d add column `total_uv` int(11) DEFAULT '0' COMMENT '历史UV';\n" +
            "alter table t_link_%d add column `total_uip` int(11) DEFAULT '0' COMMENT '历史UIP';\n";

    static final String STATS_TODAY_SQL = "create table t_link_stats_today_%d\n" +
            "(\n" +
            "    id             bigint auto_increment comment 'ID'\n" +
            "        primary key,\n" +
            "    gid            varchar(32) default 'default' null comment '分组标识',\n" +
            "    full_short_url varchar(128)                  null comment '短链接',\n" +
            "    date           date                          null comment '日期',\n" +
            "    today_pv       int         default 0         null comment '今日PV',\n" +
            "    today_uv       int         default 0         null comment '今日UV',\n" +
            "    today_uip      int         default 0         null comment '今日IP数',\n" +
            "    create_time    datetime                      null comment '创建时间',\n" +
            "    update_time    datetime                      null comment '修改时间',\n" +
            "    del_flag       tinyint(1)                    null comment '删除标识 0：未删除 1：已删除',\n" +
            "    constraint idx_unique_today_stats\n" +
            "        unique (full_short_url, gid, date)\n" +
            ")\n" +
            "    row_format = DYNAMIC;\n" +
            "\n";

    static final String ALTER_T_LINK_GOTO = "ALTER TABLE t_link_goto_%d DROP INDEX gid;\n";

    public static void main(String[] args) {
        for (int i = 0; i < 16; i++) {
            System.out.printf(ALTER_T_LINK_GOTO,i);
        }
    }
}
