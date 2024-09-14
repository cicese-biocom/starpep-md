package tomocomd.model;

public enum GOWAWA_WEIGHT_METHODS {
  S_OWA("S-OWA"),
  WINDOW_OWA("W-OWA"),
  EXPONENTIAL_SMOOTHING_1("ES1-OWA"),
  EXPONENTIAL_SMOOTHING_2("ES2-OWA"),
  AGGREGATED_OBJECTS_1("AO1-OWA"),
  AGGREGATED_OBJECTS_2("AO2-OWA"),
  NONE("NONE");

  private final String code;

  GOWAWA_WEIGHT_METHODS(String code) {
    this.code = code;
  }

  public static GOWAWA_WEIGHT_METHODS fromCode(String code) {
    for (GOWAWA_WEIGHT_METHODS method : GOWAWA_WEIGHT_METHODS.values()) {
      if (method.code.equals(code)) {
        return method;
      }
    }
    return null;
  }
}
