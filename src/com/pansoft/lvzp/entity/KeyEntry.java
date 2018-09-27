package com.pansoft.lvzp.entity;

import com.pansoft.lvzp.entity.XmlNamespace.ValueType;
import java.util.List;

@XmlNamespace(value = "o", name = "Key")
public class KeyEntry {

  @XmlNamespace(value = "", valueType = ValueType.ATTR)
  private String id;
  @XmlNamespace(value = "c", name = "Key.Columns", isGroup = true)
  private List<KeyColumnsEntry> keyColumnsEntryList;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<KeyColumnsEntry> getKeyColumnsEntryList() {
    return keyColumnsEntryList;
  }

  public void setKeyColumnsEntryList(
      List<KeyColumnsEntry> keyColumnsEntryList) {
    this.keyColumnsEntryList = keyColumnsEntryList;
  }

  @XmlNamespace(value = "o", name = "Column")
  public static class KeyColumnsEntry {

    @XmlNamespace(value = "", name = "Ref", valueType = ValueType.ATTR)
    private String ref;

    public String getRef() {
      return ref;
    }

    public void setRef(String ref) {
      this.ref = ref;
    }
  }
}
