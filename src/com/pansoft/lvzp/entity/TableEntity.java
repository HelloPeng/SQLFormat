package com.pansoft.lvzp.entity;

import com.pansoft.lvzp.entity.XmlNamespace.ValueType;
import java.util.List;

/**
 * 作者：吕振鹏 E-mail:lvzhenpeng@pansoft.com 创建时间：2018年05月11日 时间：10:54 版本：v1.0.0 类描述： 修改时间：
 */
@XmlNamespace(value = "o", name = "Table")
public class TableEntity {

  @XmlNamespace(value = "", valueType = ValueType.ATTR)
  private String id;
  @XmlNamespace(value = "a")
  private String name;
  @XmlNamespace(value = "a")
  private String code;
  @XmlNamespace(value = "a")
  private String comment;//解释
  @XmlNamespace(value = "c", isGroup = true)
  private List<KeyEntry> keys;//主键列
  @XmlNamespace(value = "c", isGroup = true)
  private List<ColumnEntity> columns;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<KeyEntry> getKeys() {
    return keys;
  }

  public void setKeys(List<KeyEntry> keys) {
    this.keys = keys;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public List<ColumnEntity> getColumns() {
    return columns;
  }

  public void setColumns(List<ColumnEntity> columns) {
    this.columns = columns;
  }
}
