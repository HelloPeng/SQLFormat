package com.pansoft.lvzp.entity;

import org.jdom.Namespace;

/**
 * 作者：吕振鹏
 * E-mail:lvzhenpeng@pansoft.com
 * 创建时间：2018年05月11日
 * 时间：16:36
 * 版本：v1.0.0
 * 类描述：
 * 修改时间：
 */
public enum ConfigNamespace {

    O("o", "object"), C("c", "collection"), A("a", "attribute");

    ConfigNamespace(String prefix, String uri) {
        this.prefix = prefix;
        this.uri = uri;
    }

    private String prefix;
    private String uri;

    public String getPrefix() {
        return prefix;
    }


    public String getUri() {
        return uri;
    }


    public static Namespace getNamespace(String prefix) {
        ConfigNamespace namespace = null;
        if (O.prefix.equals(prefix)) {
            namespace = O;
        } else if (C.prefix.equals(prefix)) {
            namespace = C;
        } else if (A.prefix.equals(prefix)) {
            namespace = A;
        }
        if (namespace == null)
            return null;
        return Namespace.getNamespace(namespace.prefix, namespace.uri);
    }
}
