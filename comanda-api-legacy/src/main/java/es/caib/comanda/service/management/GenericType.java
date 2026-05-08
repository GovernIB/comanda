package es.caib.comanda.service.management;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

public abstract class GenericType<T> {
  private final Type type;
  private final Class<?> rawType;

  protected GenericType() {
    this.type = resolveTypeParameter(getClass());
    this.rawType = resolveRawType(type);
  }

  public Type getType() {
    return type;
  }

  public Class<?> getRawType() {
    return rawType;
  }

  private static Type resolveTypeParameter(Class<?> type) {
    Type genericSuperclass = type.getGenericSuperclass();
    if (!(genericSuperclass instanceof ParameterizedType)) {
      throw new IllegalArgumentException("GenericType requires a type parameter.");
    }
    return ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
  }

  private static Class<?> resolveRawType(Type type) {
    if (type instanceof Class<?>) {
      return (Class<?>) type;
    }
    if (type instanceof ParameterizedType) {
      return resolveRawType(((ParameterizedType) type).getRawType());
    }
    if (type instanceof GenericArrayType) {
      Class<?> componentType = resolveRawType(((GenericArrayType) type).getGenericComponentType());
      return Array.newInstance(componentType, 0).getClass();
    }
    if (type instanceof TypeVariable<?> || type instanceof WildcardType) {
      return Object.class;
    }
    throw new IllegalArgumentException("Unsupported type " + type);
  }
}
