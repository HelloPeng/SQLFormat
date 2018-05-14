package com.pansoft.lvzp.entity;

import java.lang.annotation.*;

/**
 * 作者：吕振鹏
 * E-mail:lvzhenpeng@pansoft.com
 * 创建时间：2018年05月11日
 * 时间：16:45
 * 版本：v1.0.0
 * 类描述：
 * 修改时间：
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface XmlNamespace {

    String value();

    boolean isGroup() default false;

    String name() default "";
}
