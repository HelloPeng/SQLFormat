package com.pansoft.lvzp.entity;

import java.util.List;

/**
 * 作者：吕振鹏
 * E-mail:lvzhenpeng@pansoft.com
 * 创建时间：2018年05月11日
 * 时间：11:53
 * 版本：v1.0.0
 * 类描述：
 * 修改时间：
 */
public class ConfigEntity {

    private String name;
    private String namespace;
    private ConfigEntity child;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public ConfigEntity getChild() {
        return child;
    }

    public void setChild(ConfigEntity child) {
        this.child = child;
    }
}
