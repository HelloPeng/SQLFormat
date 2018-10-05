package com.pansoft.lvzp;

public class AnnValue {

  private String value;
  private String name;

  public AnnValue(String name, String value) {
    this.value = value;
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public String getName() {
    return name;
  }
}
