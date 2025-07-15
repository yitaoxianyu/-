package com.nageoffer.shortlink.test;


public class TestGotoTable {
    public static final String SQL = "CREATE TABLE shortlink.t_link_goto_%s (\n" +
            "    full_short_url varchar(128) DEFAULT NULL COMMENT '完整短链接',\n" +
            "    gid varchar(32) DEFAULT NULL COMMENT '分组 id',\n" +
            "    id BIGINT(20) AUTO_INCREMENT NOT NULL COMMENT '主键 id',\n" +
            "    UNIQUE KEY (full_short_url),\n" +
            "    UNIQUE KEY (gid),\n" +
            "    PRIMARY KEY (id)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短链接跳转表';\n";


    public static void main(String[] args) {
        for (int i = 0; i < 16; i++) {
            System.out.printf(SQL,i);
        }
    }
}
