package com.kakapo.unity.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Objects
{
  public static String toString(Object object)
  {
    StringBuilder builder = new StringBuilder();
    builder.append("[");

    String name = object.getClass().getName();
    builder.append(name.substring(name.lastIndexOf(".") + 1));
    builder.append(" ");
    Iterator<Field> fields = getAccessibleMemberFields(object).iterator();
    while (fields.hasNext())
    {
      Field field = (Field)fields.next();
      String fieldName = field.getName();
      if (fieldName.startsWith("_"))
      {
        fieldName = fieldName.substring(1);
      }builder.append(fieldName);
      builder.append("=");
      Object value;
      try {
        value = field.get(object);
      }
      catch (IllegalAccessException e)
      {
        throw new RuntimeException(e);
      }

      if (value == null)
      {
        builder.append("null");
      }
      else
      {
        builder.append(value.toString());
      }
      if (fields.hasNext())
      {
        builder.append(" ");
      }
    }
    builder.append("]");

    return builder.toString();
  }

  public static List<Field> getAccessibleMemberFields(Object object)
  {
    List<Field> fields = new ArrayList<Field>();
    Class<?> type = object.getClass();
    while (!Object.class.equals(type))
    {
      Field[] declaredFields = type.getDeclaredFields();
      for (Field field : declaredFields)
      {
        if (Modifier.isStatic(field.getModifiers())) {
          continue;
        }
        if (field.getName().startsWith("$"))
          continue;
        fields.add(field);
        field.setAccessible(true);
      }

      type = type.getSuperclass();
    }
    return fields;
  }

  @SuppressWarnings("unchecked")
public static <T> T constructCopyWith(T original, Object[] arguments)
    throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
  {
    Class<?>[] types = new Class[arguments.length];
    for (int i = 0; i < arguments.length; i++)
    {
      types[i] = arguments[i].getClass();
    }

    Constructor<?> constructor = original.getClass().getConstructor(types);

    return (T) constructor.newInstance(arguments);
  }
}