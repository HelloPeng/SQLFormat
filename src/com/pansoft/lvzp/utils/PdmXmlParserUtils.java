package com.pansoft.lvzp.utils;

import com.pansoft.lvzp.entity.ConfigNamespace;
import com.pansoft.lvzp.entity.XmlNamespace;
import com.pansoft.lvzp.entity.XmlNamespace.ValueType;
import org.jdom.Attribute;
import org.jdom.Element;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者：吕振鹏 E-mail:lvzhenpeng@pansoft.com 创建时间：2018年05月11日 时间：17:02 版本：v1.0.0 类描述： 修改时间：
 */
public class PdmXmlParserUtils {


  public static <T> List<T> buildTables(Element element, Class<T> entityClass)
      throws IllegalAccessException, InstantiationException, ClassNotFoundException {
    List<T> entityList = new ArrayList<>();
    //获取所有的成员变量
    Field[] fields = entityClass.getDeclaredFields();
    //获取类的XmlNamespace泛型
    XmlNamespace tableClassAnnotation = entityClass.getAnnotation(XmlNamespace.class);
    //通过泛型获取到该类再xml中的名字以及命名空间
    List<Element> childrenElement =
        element.getChildren(tableClassAnnotation.name(),
            ConfigNamespace.getNamespace(tableClassAnnotation.value()));
    //循环该类型下
    for (Element childElement : childrenElement) {
      //根据类型创建该类的实体类
      T entity = entityClass.newInstance();
      for (Field field : fields) {
        field.setAccessible(true);
        XmlNamespace fieldAnnotation = field.getAnnotation(XmlNamespace.class);
        String name = fieldAnnotation.name();
        String namespaceValue = fieldAnnotation.value();
        if ("".equals(name)) {
          //获取变量名字，并设置首字母大写
          name = fristUpperCase(field.getName());
        }
        if (fieldAnnotation.valueType() == ValueType.ATTR) {
          Attribute attr = childElement.getAttribute(name);
          if (attr != null) {
            field.set(entity, attr.getValue());
          }
          continue;
        }
        //根据name和命名空间查找具体的Element对象
        Element attributeElement = childElement
            .getChild(name, ConfigNamespace.getNamespace(namespaceValue));
        Object value = null;
        //判断是否根据名称和命名空间查询到了Element
        if (attributeElement != null) {
          //判断该对象是否为数组
          if (!fieldAnnotation.isGroup()) {
            value = attributeElement.getContent(0).getValue();
          } else {
            //因为Group是使用的List集合存储的数据，所以需要先通过getGenericType()获取类所属参数，然后转换为ParameterizedTypeImpl类型
            ParameterizedTypeImpl fieldType = (ParameterizedTypeImpl) field.getGenericType();
            //通过getActualTypeArguments()获取泛型中实际的参数
            Class<?> groupClass = Class
                .forName(fieldType.getActualTypeArguments()[0].getTypeName());
            //通过递归解析它子集合中的参数
            value = buildTables(attributeElement, groupClass);
          }
        }
        field.set(entity, value);
      }
      entityList.add(entity);
    }
    return entityList;
  }


  private static String fristUpperCase(String str) {
    char[] ch = str.toCharArray();
    if (ch[0] >= 'a' && ch[0] <= 'z') {
      ch[0] = (char) (ch[0] - 32);
    }
    return new String(ch);
  }
}
