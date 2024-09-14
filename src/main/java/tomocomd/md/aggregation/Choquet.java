package tomocomd.md.aggregation;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import tomocomd.StartpepException;
import tomocomd.model.SINGLETON_METHODS;
import tomocomd.model.SORTING_METHOD;

public class Choquet {

  private static final Logger logger = Logger.getLogger(Choquet.class.getName());

  public static double validateAndCompute(double[] origLovis, String headChoquet) {
    String choquetConf =
        headChoquet.substring(headChoquet.indexOf('[') + 1, headChoquet.indexOf(']'));
    String[] conf = choquetConf.split(";");

    try {
      SORTING_METHOD sort = SORTING_METHOD.fromCode(conf[0]);
      double lValue = Double.parseDouble(conf[1]);
      SINGLETON_METHODS method = SINGLETON_METHODS.fromCode(conf[2]);
      double alfa = Double.parseDouble(conf[3]);
      return choquetIntegral(origLovis, method, sort, lValue, alfa);
    } catch (StartpepException e) {
      throw e;
    } catch (Exception e) {
      throw StartpepException.ExceptionType.COMPUTE_MD_EXCEPTION.get(
          "Error in choquet configuration: " + e);
    }
  }

  private static double choquetIntegral(
      double[] oriLovis,
      SINGLETON_METHODS method,
      SORTING_METHOD sortingMethod,
      double lValue,
      double alfa) {
    if (Objects.isNull(oriLovis)) return Double.NaN;
    if (oriLovis.length == 0) return Double.NaN;

    boolean hasNegativeLoves = false;
    Double[] lovis = new Double[oriLovis.length];
    for (int i = 0; i < oriLovis.length; i++) {
      lovis[i] = oriLovis[i];
      if (lovis[i] < 0) {
        hasNegativeLoves = true;
      }
    }

    if (hasNegativeLoves) {
      return Double.NaN;
    }

    return compute(lovis, method, sortingMethod, lValue, alfa);
  }

  private static double compute(
      Double[] lovis,
      SINGLETON_METHODS method,
      SORTING_METHOD sortingMethod,
      double lValue,
      double alfa) {

    if (sortingMethod == SORTING_METHOD.DESCENDING) Arrays.sort(lovis);
    else if (sortingMethod == SORTING_METHOD.ASCENDING)
      Arrays.sort(lovis, Collections.reverseOrder());

    int dim = lovis.length;
    double[] singleton = computeSingletonMeasures(method, dim, alfa, lovis);

    double[] summationLU = new double[dim]; // summation from minumum to maximum value
    double[] summationUL = new double[dim]; // summation from maximum to minumum value

    int i;
    int j;
    if (sortingMethod == SORTING_METHOD.DESCENDING) {
      i = dim;
      j = 1;

      while (i >= 1) {
        summationLU[j - 1] = j - 1 == 0 ? singleton[j - 1] : summationLU[j - 2] + singleton[j - 1];
        summationUL[i - 1] = j - 1 == 0 ? singleton[i - 1] : summationUL[i] + singleton[i - 1];
        --i;
        ++j;
      }
    } else if (sortingMethod == SORTING_METHOD.ASCENDING) {
      for (j = dim, i = 1; i <= dim; --j, i++) {
        summationLU[j - 1] = j == dim ? singleton[j - 1] : singleton[j - 1] + summationLU[j];
        summationUL[i - 1] = j == dim ? singleton[i - 1] : singleton[i - 1] + summationUL[i - 2];
      }
    }

    double pivot = 1.0;
    double value = 0.0;

    for (i = dim; i >= 1; --i) {
      double aMinus1;
      if (sortingMethod.equals(SORTING_METHOD.DESCENDING)) {
        aMinus1 = computeFuzzyValue(dim, i - 1, singleton, summationLU, summationUL, lValue, true);
      } else {
        aMinus1 = computeFuzzyValue(dim, i - 1, singleton, summationUL, summationLU, lValue, false);
      }

      value += lovis[i - 1] * (pivot - aMinus1);
      pivot = aMinus1;
    }
    return value;
  }

  private static double computeFuzzyValue(
      int dim,
      int dimA,
      double[] singleton,
      double[] summationLU,
      double[] summationUL,
      double lValue,
      boolean desc) {
    if (dimA == 0) {
      return 0.0;
    } else if (dim == dimA) {
      return 1.0;
    } else if (lValue == -1.0) {
      return desc ? singleton[dimA - 1] : singleton[0];
    }

    double num;
    double den;
    if (lValue > -1.0 && lValue <= 0.0) {
      num = (1.0 + lValue) * summationLU[dimA - 1] * (1.0 + lValue * singleton[dimA - 1]);
      den = 1.0 + lValue * summationLU[dimA - 1];
      return num / den - lValue * singleton[dimA - 1];
    } else if (lValue > 0.0) {
      num = lValue * (dimA - 1) * summationLU[dimA - 1] * (1.0 - summationLU[dimA - 1]);
      den = (dim - dimA) * summationUL[dimA] + lValue * (dimA - 1) * summationLU[dimA - 1];
      return num / den + summationLU[dimA - 1];
    } else {
      return Double.NaN;
    }
  }

  private static double[] computeSingletonMeasures(
      SINGLETON_METHODS method, int dim, double alfa, Double[] lovis) {

    switch (method) {
      case AGGREGATED_OBJECTS_1:
        return aggregatedMethod1(dim, alfa, lovis);

      case AGGREGATED_OBJECTS_2:
        return aggregatedMethod2(dim, alfa, lovis);
      default:
        return new double[dim];
    }
  }

  private static double[] aggregatedMethod1(int dim, double alfa, Double[] lovis) {

    double[] singleton = new double[dim];
    if (dim > 0) {
      // the lovis vector is already in ascending order

      double den = 0d;
      for (int i = 1; i <= dim; i++) {
        den += Math.pow(lovis[i - 1], alfa);
      }

      if (den != 0) {
        for (int i = lovis.length; i >= 1; i--) {
          singleton[i - 1] = Math.pow(lovis[i - 1], alfa) / den;
        }
      }
    }
    return singleton;
  }

  private static double[] aggregatedMethod2(int dim, double alfa, Double[] lovis) {

    double[] singleton = new double[dim];
    if (dim > 0) {
      // the lovis vector is already in ascending order

      double den = 0d;
      for (int i = 1; i <= dim; i++) {
        den += Math.pow(Math.abs(1d - lovis[i - 1]), alfa);
      }

      if (den != 0) {
        for (int i = lovis.length; i >= 1; i--) {
          singleton[i - 1] = Math.pow(Math.abs(1d - lovis[i - 1]), alfa) / den;
        }
      }
    }
    return singleton;
  }

  public static void main(String[] args) {
    double[] lovis = {1.0, 2.0, 3.0};

    for (String choquet : defaultChoquet) {

      String conf = choquet.substring(choquet.indexOf('[') + 1, choquet.indexOf(']'));
      String[] confs = conf.split(";");

      SORTING_METHOD sort = SORTING_METHOD.fromCode(confs[0]);
      double lValue = Double.parseDouble(confs[1]);
      SINGLETON_METHODS method = SINGLETON_METHODS.fromCode(confs[2]);
      double alfa = Double.parseDouble(confs[3]);
      logger.log(
          Level.INFO,
          "Choquet({0})={1}",
          new Object[] {conf, choquetIntegral(lovis, method, sort, lValue, alfa)});
    }
  }

  public static final String[] defaultChoquet = {
    "CHOQUET[A;-0.75;AO2;0.6]",
    "CHOQUET[A;-0.75;AO1;0.3]",
    "CHOQUET[A;-0.25;AO2;0.6]",
    "CHOQUET[A;0.75;AO2;0.6]",
    "CHOQUET[A;0.75;AO1;0.2]",
    "CHOQUET[A;0.25;AO1;0.9]",
    "CHOQUET[A;0.25;AO2;0.6]",
    "CHOQUET[A;0.5;AO1;0.2]",
    "CHOQUET[A;0.75;AO2;0.5]",
    "CHOQUET[A;0.75;AO1;0.8]",
    "CHOQUET[A;0.5;AO1;0.9]",
    "CHOQUET[A;0.25;AO1;0.8]",
    "CHOQUET[A;-0.25;AO2;1.0]",
    "CHOQUET[A;-0.25;AO2;0.8]",
    "CHOQUET[A;0.5;AO2;0.6]",
    "CHOQUET[A;-0.25;AO1;0.8]",
    "CHOQUET[A;-0.75;AO1;0.9]",
    "CHOQUET[A;0.5;AO2;0.9]",
    "CHOQUET[A;0.75;AO1;0.9]",
    "CHOQUET[A;-0.75;AO2;0.9]",
    "CHOQUET[A;-0.75;AO1;1.0]",
    "CHOQUET[A;0.5;AO2;0.8]",
    "CHOQUET[A;0.25;AO2;0.5]",
    "CHOQUET[A;-0.75;AO2;0.7]",
    "CHOQUET[A;-0.75;AO2;1.0]",
    "CHOQUET[A;-0.5;AO1;0.3]",
    "CHOQUET[A;-0.75;AO2;0.0]",
    "CHOQUET[A;-0.75;AO1;0.2]",
    "CHOQUET[A;-0.5;AO2;0.0]",
    "CHOQUET[A;0.5;AO1;0.8]",
    "CHOQUET[A;0.25;AO1;0.2]",
    "CHOQUET[A;-0.5;AO1;0.2]",
    "CHOQUET[A;0.5;AO2;0.5]",
    "CHOQUET[D;-0.75;AO2;0.6]",
    "CHOQUET[D;-0.75;AO1;0.3]",
    "CHOQUET[D;-0.25;AO2;0.6]",
    "CHOQUET[D;0.75;AO2;0.6]",
    "CHOQUET[D;0.75;AO1;0.2]",
    "CHOQUET[D;0.25;AO1;0.9]",
    "CHOQUET[D;0.25;AO2;0.6]",
    "CHOQUET[D;0.5;AO1;0.2]",
    "CHOQUET[D;0.75;AO2;0.5]",
    "CHOQUET[D;0.75;AO1;0.8]",
    "CHOQUET[D;0.5;AO1;0.9]",
    "CHOQUET[D;0.25;AO1;0.8]",
    "CHOQUET[D;-0.25;AO2;1.0]",
    "CHOQUET[D;-0.25;AO2;0.8]",
    "CHOQUET[D;0.5;AO2;0.6]",
    "CHOQUET[D;-0.25;AO1;0.8]",
    "CHOQUET[D;-0.75;AO1;0.9]",
    "CHOQUET[D;0.5;AO2;0.9]",
    "CHOQUET[D;0.75;AO1;0.9]",
    "CHOQUET[D;-0.75;AO2;0.9]",
    "CHOQUET[D;-0.75;AO1;1.0]",
    "CHOQUET[D;0.5;AO2;0.8]",
    "CHOQUET[D;0.25;AO2;0.5]",
    "CHOQUET[D;-0.75;AO2;0.7]",
    "CHOQUET[D;-0.75;AO2;1.0]",
    "CHOQUET[D;-0.5;AO1;0.3]",
    "CHOQUET[D;-0.75;AO2;0.0]",
    "CHOQUET[D;-0.75;AO1;0.2]",
    "CHOQUET[D;-0.5;AO2;0.0]",
    "CHOQUET[D;0.5;AO1;0.8]",
    "CHOQUET[D;0.25;AO1;0.2]",
    "CHOQUET[D;-0.5;AO1;0.2]",
    "CHOQUET[D;0.5;AO2;0.5]"
  };
}
