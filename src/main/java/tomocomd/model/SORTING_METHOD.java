package tomocomd.model;

import tomocomd.StartpepException;

public enum SORTING_METHOD {
  ASCENDING("A"),
  DESCENDING("D");

  private final String code;

  SORTING_METHOD(String code) {
    this.code = code;
  }

  public static SORTING_METHOD fromCode(String code) {
    for (SORTING_METHOD method : SORTING_METHOD.values()) {
      if (method.code.equals(code)) {
        return method;
      }
    }
    throw StartpepException.ExceptionType.INVALID_SORT_CHOQUET_OPERATOR.get(
        "Invalid sort operator for choquet: " + code);
  }
}
