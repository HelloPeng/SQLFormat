package com.pansoft.lvzp.utils;

import com.pansoft.lvzp.entity.ConfigNamespace;
import com.pansoft.lvzp.entity.PDMConfigEntity;
import com.pansoft.lvzp.entity.PansoftFieldEntity;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

public class PDMUtils {

  public static Element getTablesElement(Document document, PDMConfigEntity PDMConfigEntity) {
    Element rootElement = document.getRootElement();
    boolean hasNext = true;
    while (hasNext) {
      String namespace = PDMConfigEntity.getNamespace();
      rootElement = rootElement
          .getChild(PDMConfigEntity.getName(), ConfigNamespace.getNamespace(namespace));
      PDMConfigEntity = PDMConfigEntity.getChild();
      hasNext = PDMConfigEntity != null;
    }
    return rootElement;
  }

  /**
   * 通过递归获取pdm数据的配置信息
   *
   * @param rootElement 需要解析的Object目录
   * @param PDMConfigEntity 当前用来保存信息的实体类
   */
  public static void buildConfig(Element rootElement, PDMConfigEntity PDMConfigEntity) {
    Element object = rootElement.getChild("Object");
    if (object != null) {
      Element name = object.getChild("Name");
      Element namespace = object.getChild("Namespace");
      if (name != null) {
        PDMConfigEntity.setName(name.getContent(0).getValue());
      }
      if (namespace != null) {
        PDMConfigEntity.setNamespace(namespace.getContent(0).getValue());
      }
      Element child = object.getChild("Child");
      if (child != null) {
        PDMConfigEntity childConfig = new PDMConfigEntity();
        PDMConfigEntity.setChild(childConfig);
        buildConfig(child, childConfig);
      }
    }
  }

  public static List<PansoftFieldEntity> buildFileTypes(Element rootElement) {
    List<PansoftFieldEntity> retData = new ArrayList<>();
    List<Element> children = rootElement.getChildren();
    children.forEach(it -> {
      Attribute name = it.getAttribute("name");
      Attribute value = it.getAttribute("value");
      Attribute packages = it.getAttribute("packages");
      PansoftFieldEntity pansoftFieldEntity = new PansoftFieldEntity();
      pansoftFieldEntity.setName(name.getValue());
      pansoftFieldEntity.setValue(value.getValue());
      pansoftFieldEntity.setPackages(packages.getValue());
      retData.add(pansoftFieldEntity);
    });
    return retData;
  }
}
