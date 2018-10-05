package com.pansoft.lvzp;

public enum AnnType {

  DATA("Data", "lombok.Data"),
  ENTITY("Entity", "javax.persistence.Entity"),
  GENERATEDVALUE("GeneratedValue", "javax.persistence.GeneratedValue"),
  GENERICGENERATOR("GenericGenerator", "org.hibernate.annotations.GenericGenerator"),
  ID("Id", "javax.persistence.Id"),
  COLUMN("Column", "javax.persistence.Column"),
  TABLE("Table", "javax.persistence.Table");

  AnnType(String simpleName, String className) {
    this.simpleName = simpleName;
    this.className = className;
  }

  private String simpleName;
  private String className;

  public String getSimpleName() {
    return simpleName;
  }

  public void setSimpleName(String simpleName) {
    this.simpleName = simpleName;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }
}

