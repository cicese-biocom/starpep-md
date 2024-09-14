package tomocomd.model;

import tomocomd.StartpepException;

public class AggregatorOperators {
  private final String code;
  private String type;
  private int k;

  private static final String CLASSIC = "CLASSIC";

  public AggregatorOperators(String code) {
    if (code == null || code.isEmpty())
      throw StartpepException.ExceptionType.INVALID_AGGREGATOR_OPERATOR.get(
          "Invalid aggregator operator code, has to be different to null");
    this.code = code;
    this.k = 0;
    switch (code) {
      case "N1":
      case "N2":
      case "N3":
        type = "NORM";
        break;
      case "AM":
      case "GM":
      case "HM":
      case "P2":
      case "P3":
        type = "MEAN";
        break;
      case "V":
      case "SD":
      case "VC":
      case "RA":
      case "Q1":
      case "Q2":
      case "Q3":
      case "I50":
      case "S":
      case "K":
      case "MX":
      case "MN":
        type = "STATISTIC";
        break;
      case "TIC":
      case "SIC":
        type = "INFORMATION";
        break;
      case "MIC":
      case "ES":
        type = CLASSIC;
        break;
      case "AC[1]":
        type = CLASSIC;
        k = 1;
        code = "AC";
        break;
      case "GV[1]":
        type = CLASSIC;
        k = 1;
        code = "GV";
        break;
      case "TS[1]":
        type = CLASSIC;
        k = 1;
        code = "TS";
        break;
      case "AC[2]":
        type = CLASSIC;
        k = 2;
        code = "AC";
        break;
      case "GV[2]":
        type = CLASSIC;
        k = 2;
        code = "GV";
        break;
      case "TS[2]":
        type = CLASSIC;
        k = 2;
        code = "TS";
        break;
      case "AC[3]":
        type = CLASSIC;
        k = 3;
        code = "AC";
        break;
      case "GV[3]":
        type = CLASSIC;
        k = 3;
        code = "GV";
        break;
      case "TS[3]":
        type = CLASSIC;
        k = 3;
        code = "TS";
        break;
      case "AC[4]":
        type = CLASSIC;
        k = 4;
        code = "AC";
        break;
      case "GV[4]":
        type = CLASSIC;
        k = 4;
        code = "GV";
        break;
      case "TS[4]":
        type = CLASSIC;
        k = 4;
        code = "TS";
        break;
      case "AC[5]":
        type = CLASSIC;
        k = 5;
        code = "AC";
        break;
      case "GV[5]":
        type = CLASSIC;
        k = 5;
        code = "GV";
        break;
      case "TS[5]":
        type = CLASSIC;
        k = 5;
        code = "TS";
        break;
      case "AC[6]":
        type = CLASSIC;
        k = 6;
        code = "AC";
        break;
      case "GV[6]":
        type = CLASSIC;
        k = 6;
        code = "GV";
        break;
      case "TS[6]":
        type = CLASSIC;
        k = 6;
        code = "TS";
        break;
      case "AC[7]":
        type = CLASSIC;
        k = 7;
        code = "AC";
        break;
      case "GV[7]":
        type = CLASSIC;
        k = 7;
        code = "GV";
        break;
      case "TS[7]":
        type = CLASSIC;
        k = 7;
        code = "TS";
        break;
      default:
        break;
    }

    if (code.contains("GOWAWA")) {
      type = "GOWAWA";
    } else if (code.contains("CHOQUET")) {
      type = "CHOQUET";
    }
    if (type == null)
        throw StartpepException.ExceptionType.INVALID_AGGREGATOR_OPERATOR.get(
            "Invalid aggregator operator code: " + code);
  }

  public String getCode() {
    return code;
  }

  public String getType() {
    return type;
  }

  public int getK() {
    return k;
  }
}
