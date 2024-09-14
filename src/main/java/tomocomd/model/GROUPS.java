package tomocomd.model;

import tomocomd.StartpepException;

/**
 * @author Cesar and Luis
 */
public enum GROUPS {
  Total("T"),
  Aliphatic("A"),
  AlphaHelixFavoring("H"),
  Apolar("P"),
  Aromatic("R"),
  BetaSheetFavoring("B"),
  BetaTurnFavoring("F"),
  NegativelyChargedPolar("N"),
  PositivelyChargedPolar("C"),
  UnchargedPolar("U"),
  Unfolding("D");

  private final String code;

  GROUPS(String code) {
    this.code = code;
  }

  public static GROUPS fromCode(String code) {
    for (GROUPS local : GROUPS.values()) {
      if (local.code.equals(code)) {
        return local;
      }
    }
    throw StartpepException.ExceptionType.INVALID_GROUP_OPERATOR.get("Invalid code: " + code);
  }

  public String getCode() {
    return code;
  }
}
