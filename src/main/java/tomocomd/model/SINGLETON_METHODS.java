package tomocomd.model;

import tomocomd.StartpepException;

public enum SINGLETON_METHODS {
  AGGREGATED_OBJECTS_1("AO1"),
  AGGREGATED_OBJECTS_2("AO2");

  private final String code;

  SINGLETON_METHODS(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public static SINGLETON_METHODS fromCode(String code) {
    for (SINGLETON_METHODS method : SINGLETON_METHODS.values()) {
      if (method.code.equals(code)) {
        return method;
      }
    }
    throw StartpepException.ExceptionType.INVALID_SINGLETON_METHOD.get(
        "Invalid singleton method code: " + code);
  }
}
