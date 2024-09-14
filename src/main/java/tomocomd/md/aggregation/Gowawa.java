package tomocomd.md.aggregation;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import tomocomd.StartpepException;
import tomocomd.model.GOWAWA_WEIGHT_METHODS;

public class Gowawa {

  private static final Logger logger = Logger.getLogger(Gowawa.class.getName());

  public static double computeGOWAWA(
      double[] oriLovis,
      double betaOwawa,
      int lambdaOwa,
      GOWAWA_WEIGHT_METHODS methodOwa,
      double alfaOwa,
      double betaOwa,
      int deltaWa,
      GOWAWA_WEIGHT_METHODS methodWa,
      double alfaWa,
      double betaWa) {
    if (oriLovis == null || oriLovis.length == 0) {
      return Double.NaN;
    }

    int[] indeces = new int[oriLovis.length];
    double[] lovis = new double[oriLovis.length];

    for (int i = 0; i < oriLovis.length; i++) {
      indeces[i] = i;

      if ((lovis[i] = oriLovis[i]) < 0) return Double.NaN;
    }
    sort(lovis, indeces); // it sorts the lovis vector in ascending order

    double[] owaWeights =
        computeWeights(true, methodOwa, lovis.length, lovis, alfaOwa, betaOwa, alfaWa, betaWa);
    double[] waWeights =
        computeWeights(false, methodWa, lovis.length, lovis, alfaOwa, betaOwa, alfaWa, betaWa);

    double gowa = 0;
    double wgm = 0;
    if (lambdaOwa != 0 && deltaWa != 0) {
      for (int i = lovis.length, j = 1; i >= 1; i--, j++) {
        gowa += Math.pow(lovis[i - 1], lambdaOwa) * owaWeights[j - 1];
        wgm += Math.pow(lovis[i - 1], deltaWa) * waWeights[indeces[i - 1]];
      }
    } else {
      if (lambdaOwa == 0) // weigthed ordered geometric mean
      {
        gowa = 1;
        for (int i = lovis.length, j = 1; i >= 1; i--, j++) {
          gowa *= Math.pow(lovis[i - 1], owaWeights[j - 1]);
        }
      } else // generalized ordered weigthed averaging
      {
        for (int i = lovis.length, j = 1; i >= 1; i--, j++) {
          gowa += Math.pow(lovis[i - 1], lambdaOwa) * owaWeights[j - 1];
        }
      }

      if (deltaWa == 0) // weigthed geometric mean
      {
        wgm = 1;
        for (int i = lovis.length; i >= 1; i--) {
          wgm *= Math.pow(lovis[i - 1], waWeights[indeces[i - 1]]);
        }
      } else // weigthed generalized mean
      {
        for (int i = lovis.length; i >= 1; i--) {
          wgm += Math.pow(lovis[i - 1], deltaWa) * waWeights[indeces[i - 1]];
        }
      }
    }
    gowa = (lambdaOwa != 0 ? Math.pow(gowa, 1d / lambdaOwa) : gowa) * betaOwawa;
    wgm = (deltaWa != 0 ? Math.pow(wgm, 1d / deltaWa) : wgm) * (1 - betaOwawa);

    return gowa + wgm;
  }

  private static double[] computeWeights(
      boolean isOWAVector,
      GOWAWA_WEIGHT_METHODS method,
      int dim,
      double[] lovis,
      double alfaOwa,
      double betaOwa,
      double alfaWa,
      double betaWa) {
    double alfa = isOWAVector ? alfaOwa : alfaWa;
    double beta = isOWAVector ? betaOwa : betaWa;
    switch (method) {
      case S_OWA:
        return sOwaOperator(dim, alfa, beta);
      case WINDOW_OWA:
        return windowOwaOperator(dim, alfa, beta);
      case EXPONENTIAL_SMOOTHING_1:
        return exponentialSmoothing1OwaOperator(dim, alfa);
      case EXPONENTIAL_SMOOTHING_2:
        return exponentialSmoothing2OwaOperator(dim, alfa);
      case AGGREGATED_OBJECTS_1:
        return aggregatedObjects1OwaOperator(isOWAVector, dim, lovis, alfa);
      case AGGREGATED_OBJECTS_2:
        return aggregatedObjects2OwaOperator(isOWAVector, dim, lovis, alfa);
      default:
        return new double[dim];
    }
  }

  private static double[] sOwaOperator(int dim, double alfa, double beta) {
    double[] weights = new double[dim];
    if (dim > 0) {
      double v = (1f / dim) * (1 - (alfa + beta));
      weights[0] = v + alfa;
      weights[dim - 1] = v + beta;

      for (int i = 2; i <= dim - 1; i++) {
        weights[i - 1] = v;
      }
    }
    return weights;
  }

  private static double[] windowOwaOperator(int dim, double alfa, double beta) {
    double[] weights = new double[dim];
    if (dim > 0) {
      int k = (int) (alfa * dim);
      if (k == 0) {
        k = 1;
      }
      int m = (int) (beta * dim);

      for (int i = 1; i <= dim; i++) {
        if (i < k) {
          weights[i - 1] = 0;
        } else if (i <= m) {
          weights[i - 1] = 1f / ((m - k) + 1);
        } else {
          weights[i - 1] = 0;
        }
      }
    }

    return weights;
  }

  private static double[] exponentialSmoothing1OwaOperator(int dim, double alfa) {
    double[] weights = new double[dim];
    if (dim > 0) {
      weights[0] = alfa;
      for (int i = 2; i <= dim - 1; i++) {
        weights[i - 1] = weights[i - 2] * (1 - alfa);
      }
      weights[dim - 1] = Math.pow(1f - alfa, dim - 1.0);
    }

    return weights;
  }

  private static double[] exponentialSmoothing2OwaOperator(int dim, double alfa) {
    double[] weights = new double[dim];
    if (dim > 0) {
      weights[dim - 1] = 1f - alfa;
      for (int i = dim - 1; i >= 2; i--) {
        weights[i - 1] = weights[i] * (1 - weights[dim - 1]);
      }
      weights[0] = Math.pow(alfa, dim - 1.0);
    }
    return weights;
  }

  private static double[] aggregatedObjects1OwaOperator(
      boolean isOWAVector, int dim, double[] lovis, double alfa) {
    double[] weights = new double[dim];
    if (dim > 0) {
      if (!isOWAVector) {
        computeNaNVector(weights);
      } else {
        // the lovis vector is already in ascending order

        double den = 0d;
        for (int i = 1; i <= dim; i++) {
          den += Math.pow(lovis[i - 1], alfa);
        }

        if (den != 0) {
          for (int i = lovis.length, j = 1; i >= 1; i--, j++) {
            weights[j - 1] = Math.pow(lovis[i - 1], alfa) / den;
          }
        }
      }
    }
    return weights;
  }

  private static double[] aggregatedObjects2OwaOperator(
      boolean isOWAVector, int dim, double[] lovis, double alfa) {

    double[] weights = new double[dim];
    if (dim > 0) {
      if (!isOWAVector) {
        computeNaNVector(weights);
      } else {
        // the lovis vector is already in ascending order

        double den = 0d;
        for (int i = 1; i <= dim; i++) {
          den += Math.pow(Math.abs(1d - lovis[i - 1]), alfa);
        }

        if (den != 0) {
          for (int i = lovis.length, j = 1; i >= 1; i--, j++) {
            weights[j - 1] = Math.pow(Math.abs(1d - lovis[i - 1]), alfa) / den;
          }
        }
      }
    }
    return weights;
  }

  private static void computeNaNVector(double[] weights) {
    Arrays.fill(weights, Double.NaN);
  }

  private static void sort(double[] lovis, int[] indeces) {
    for (int i = 0; i < lovis.length; i++) {
      for (int j = 0; j < lovis.length - 1 - i; j++) {
        if (lovis[j] > lovis[j + 1]) {
          double dTemp = lovis[j];
          lovis[j] = lovis[j + 1];
          lovis[j + 1] = dTemp;

          int iTemp = indeces[j];
          indeces[j] = indeces[j + 1];
          indeces[j + 1] = iTemp;
        }
      }
    }
  }

  public static double validateAndCompute(double[] lovis, String headGowawa) {

    String conf = headGowawa.substring(headGowawa.indexOf('[') + 1, headGowawa.indexOf(']'));
    String[] owawaParts = conf.split(";");
    try {
      double betaOwawa = Double.parseDouble(owawaParts[0]);
      int lambdaOwa = Integer.parseInt(owawaParts[1]);
      GOWAWA_WEIGHT_METHODS methodOwa = GOWAWA_WEIGHT_METHODS.fromCode(owawaParts[2]);
      double alfaOwa = Double.parseDouble(owawaParts[3]);
      double betaOwa = Double.parseDouble(owawaParts[4]);
      int deltaWa = Integer.parseInt(owawaParts[5]);
      GOWAWA_WEIGHT_METHODS methodWa = GOWAWA_WEIGHT_METHODS.fromCode(owawaParts[6]);
      double alfaWa = Double.parseDouble(owawaParts[7]);
      double betaWa = Double.parseDouble(owawaParts[8]);
      return computeGOWAWA(
          lovis, betaOwawa, lambdaOwa, methodOwa, alfaOwa, betaOwa, deltaWa, methodWa, alfaWa,
          betaWa);
    } catch (Exception e) {
      throw StartpepException.ExceptionType.COMPUTE_MD_EXCEPTION.get(
          "Error in GOWAWA configuration: " + e);
    }
  }

  public static void main(String[] args) {
    double[] lovis = {1.0, 2.0, 3.0};

    for (String owawa : defaultOWAWAs) {
      String conf = owawa.substring(owawa.indexOf('[') + 1, owawa.indexOf(']'));
      String[] owawaParts = conf.split(";");
      double betaOwawa = Double.parseDouble(owawaParts[0]);
      int lambdaOwa = Integer.parseInt(owawaParts[1]);
      GOWAWA_WEIGHT_METHODS methodOwa = GOWAWA_WEIGHT_METHODS.fromCode(owawaParts[2]);
      double alfaOwa = Double.parseDouble(owawaParts[3]);
      double betaOwa = Double.parseDouble(owawaParts[4]);
      int deltaWa = Integer.parseInt(owawaParts[5]);
      GOWAWA_WEIGHT_METHODS methodWa = GOWAWA_WEIGHT_METHODS.fromCode(owawaParts[6]);
      double alfaWa = Double.parseDouble(owawaParts[7]);
      double betaWa = Double.parseDouble(owawaParts[8]);
      double value =
          computeGOWAWA(
              lovis, betaOwawa, lambdaOwa, methodOwa, alfaOwa, betaOwa, deltaWa, methodWa, alfaWa,
              betaWa);
      logger.log(Level.INFO, "Gowawa({0})={1}", new Object[] {owawa, value});
    }
  }

  public static final String[] defaultOWAWAs = {
    "GOWAWA[0.9;1;AO2-OWA;1.0;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[1.0;1;AO2-OWA;1.0;0.0;1;NONE;0.0;0.0]",
    "GOWAWA[0.1;1;S-OWA;1.0;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[0.2;1;S-OWA;1.0;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[0.7;1;S-OWA;0.8;0.2;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[0.8;1;S-OWA;1.0;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[0.5;1;ES1-OWA;0.7;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[0.2;1;AO2-OWA;1.0;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[1.0;1;ES1-OWA;0.7;0.0;1;NONE;0.0;0.0]",
    "GOWAWA[0.5;1;S-OWA;1.0;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[0.9;1;ES1-OWA;0.7;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[0.2;1;ES1-OWA;0.7;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[0.7;1;ES1-OWA;0.7;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[0.6;1;AO2-OWA;1.0;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[0.5;1;AO2-OWA;1.0;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[1.0;1;S-OWA;0.8;0.2;1;NONE;0.0;0.0]",
    "GOWAWA[0.7;1;AO2-OWA;1.0;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[0.0;1;NONE;0.0;0.0;1;S-OWA;1.0;0.0]",
    "GOWAWA[0.1;0;W-OWA;0.1;0.6;2;W-OWA;0.1;0.2]",
    "GOWAWA[0.0;1;NONE;0.0;0.0;2;W-OWA;0.4;0.5]",
    "GOWAWA[0.0;1;NONE;0.0;0.0;2;W-OWA;0.7;0.8]",
    "GOWAWA[0.1;2;ES2-OWA;0.9;0.0;2;W-OWA;0.3;0.4]",
    "GOWAWA[0.3;1;S-OWA;1.0;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[0.4;1;ES1-OWA;0.7;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[0.9;1;S-OWA;1.0;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[0.0;1;NONE;0.0;0.0;2;W-OWA;0.5;0.6]",
    "GOWAWA[0.1;0;AO1-OWA;1.0;0.0;2;S-OWA;0.8;0.1]",
    "GOWAWA[0.0;1;NONE;0.0;0.0;2;W-OWA;0.5;0.7]",
    "GOWAWA[0.6;1;S-OWA;1.0;0.0;2;W-OWA;0.9;1.0]",
    "GOWAWA[0.1;2;ES2-OWA;0.9;0.0;2;W-OWA;0.2;0.3]",
    "GOWAWA[1.0;1;ES1-OWA;0.7;0.0;1;NONE;0.0;0.0]",
    "GOWAWA[1.0;1;S-OWA;1.0;0.0;1;NONE;0.0;0.0]",
    "GOWAWA[1.0;2;ES2-OWA;0.9;0.0;1;NONE;0.0;0.0]",
    "GOWAWA[0.5;1;AO2-OWA;1.0;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[1.0;1;S-OWA;0.8;0.2;1;NONE;0.0;0.0]",
    "GOWAWA[0.0;1;NONE;0.0;0.0;0;S-OWA;0.0;1.0]",
    "GOWAWA[0.7;2;ES2-OWA;0.9;0.0;0;S-OWA;0.0;1.0]",
    "GOWAWA[0.1;2;ES2-OWA;0.9;0.0;2;W-OWA;0.7;0.8]",
    "GOWAWA[0.1;2;ES2-OWA;0.9;0.0;0;W-OWA;0.0;0.1]",
    "GOWAWA[0.9;1;AO2-OWA;1.0;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[0.2;2;ES2-OWA;0.9;0.0;2;W-OWA;0.4;0.6]",
    "GOWAWA[0.3;2;S-OWA;0.6;0.0;2;W-OWA;0.9;1.0]",
    "GOWAWA[0.4;1;AO2-OWA;1.0;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[1.0;1;AO2-OWA;1.0;0.0;1;NONE;0.0;0.0]",
    "GOWAWA[0.0;1;NONE;0.0;0.0;1;S-OWA;1.0;0.0]",
    "GOWAWA[0.8;1;AO2-OWA;1.0;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[0.9;1;ES1-OWA;0.7;0.0;1;ES2-OWA;0.9;0.0]",
    "GOWAWA[0.2;2;S-OWA;0.6;0.0;2;S-OWA;0.8;0.1]"
  };
}
