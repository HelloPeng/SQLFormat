package com.pansoft.lvzp.entity;

import java.lang.annotation.*;

/**
 * 作者：吕振鹏 E-mail:lvzhenpeng@pansoft.com 创建时间：2018年05月11日 时间：16:45 版本：v1.0.0 类描述： 修改时间：
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface XmlNamespace {

  /**
   * xml属性所处的命名空间
   */
  String value();


  /**
   * xml属性命名空间的名称 默认情况下取的是成员变量的名称
   *
   * @return 命名空间的名称
   */
  String name() default "";

  /**
   * 标识该属性是否为集合类型
   *
   * @return 默认为非集合类型
   */
  boolean isGroup() default false;

  /**
   * 需要获取属性的类型
   *
   * @return 返回具体的属性类型
   */
  ValueType valueType() default ValueType.CONTENT;

  enum ValueType {
    /**
     * CONTENT:xml标签的的内容
     */
    CONTENT,
    /**
     * xml标签的属性值
     */
    ATTR
  }
}
