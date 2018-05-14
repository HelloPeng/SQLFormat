package com.pansoft.lvzp.entity;

/**
 * 作者：吕振鹏
 * E-mail:lvzhenpeng@pansoft.com
 * 创建时间：2018年05月11日
 * 时间：10:55
 * 版本：v1.0.0
 * 类描述：
 * 修改时间：
 */
@XmlNamespace(value = "o", name = "Column")
public class ColumnEntity {

    @XmlNamespace(value = "a")
    private String name;//数据库列名（中文）
    @XmlNamespace(value = "a")
    private String code;//数据库列名（Code）
    @XmlNamespace(value = "a")
    private String comment;//解释
    @XmlNamespace(value = "a")
    private String defaultValue;//默认值
    @XmlNamespace(value = "a")
    private String dataType;//数据类型
    @XmlNamespace(value = "a")
    private String length;//数据的长度 有些列的长度，如果没有长度则按照类型判断长度，如果再没有就默认255

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

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }
}
