package tomocomd.md.aggregation;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.StatUtils;
import tomocomd.StartpepException;
import tomocomd.model.AggregatorOperators;

public class Means {

  static Logger logger = Logger.getLogger(Means.class.getName());
  private static final int POT_HARMONIC = -1;
  private static final int POT_ARITHMETIC = 1;
  private static final int POT_CUADRATIC = 2;
  private static final int POT_POTENTIAL = 3;

  // AM: Arithmetic Mean (AM)
  private static double arithmeticMean(double[] lovis) {
    return generalizedMean(lovis, POT_ARITHMETIC);
  }

  // GM: Geometric Mean (GM)
  private static double geometricMean(double[] lovis) {
    return StatUtils.geometricMean(lovis);
  }

  // PM: Potential Mean (P3)
  private static double potentialMeans(double[] lovis) {
    return generalizedMean(lovis, POT_POTENTIAL);
  }

  // QM: Quadratic Mean (P2)
  private static double quadraticMeans(double[] lovis) {
    return generalizedMean(lovis, POT_CUADRATIC);
  }

  // HM: Harmonic Mean (HM)
  private static double harmonicMeans(double[] lovis) {
    return generalizedMean(lovis, POT_HARMONIC);
  }

  private static double generalizedMean(double[] lovis, int pot) {
    int n = lovis.length;
    int nZeros = 0;
    if (n == 0) {
      return 0;
    }

    double value = 0;
    for (double lovi : lovis) {
      if (pot == 1) {
        value += lovi;
      } else if (lovi < 0 && (pot % 2 != 0)) {
        return Double.NaN;
      } else if (lovi == 0) {
        nZeros++;
      } else {
        value = value + Math.pow(lovi, pot);
      }
    }

    if (Double.isNaN(value)) {
      return value;
    }

    if ((n - nZeros) == 0 || value == 0) // all lovis are zeros
    {
      return 0;
    }

    switch (pot) {
      case -1: // harmonic mean
        return (n - nZeros) / value;
      case 1: // arithmetic mean
        return value / (n - nZeros);
      case 2: // quadratic mean
        return Math.sqrt(value / (n - nZeros));
      case 3: // potential mean
        return Math.cbrt(value / (n - nZeros));
      default:
        return Double.NaN;
    }
  }

  public static double computeMeansOperator(double[] lovis, AggregatorOperators operator) {
    switch (operator.getCode()) {
      case "AM":
        return arithmeticMean(lovis);
      case "GM":
        return geometricMean(lovis);
      case "P3":
        return potentialMeans(lovis);
      case "P2":
        return quadraticMeans(lovis);
      case "HM":
        return harmonicMeans(lovis);
      default:
        throw StartpepException.ExceptionType.INVALID_AGGREGATOR_OPERATOR.get(
            "Invalid Mean operator code: " + operator.getCode());
    }
  }

  public static void main(String[] args) {
    double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};

    logger.log(Level.INFO, "Arithmetic means: {0}", Means.arithmeticMean(values));
    logger.log(Level.INFO, "Geometric means: {0}", Means.geometricMean(values));
    logger.log(Level.INFO, "Potential means (p=3): {0}", Means.potentialMeans(values));
    logger.log(Level.INFO, "Quadratic means (p=2): {0}", Means.quadraticMeans(values));
    logger.log(Level.INFO, "Harmonic means: {0}", Means.harmonicMeans(values));
  }
}
