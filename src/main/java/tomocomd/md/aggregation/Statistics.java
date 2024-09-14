package tomocomd.md.aggregation;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import tomocomd.model.AggregatorOperators;

public class Statistics {

  private static final int Q1 = 25;
  private static final int Q2 = 50;
  private static final int Q3 = 75;
  private static final Logger logger = Logger.getLogger(Statistics.class.getName());

  // V: Variance (V)
  private static double calculateVariance(double[] lovis) {
    DescriptiveStatistics stats = new DescriptiveStatistics(lovis);
    return stats.getVariance();
  }

  // SD: Standard Deviation (SD)
  private static double calculateStandardDeviation(double[] lovis) {
    return Math.sqrt(calculateVariance(lovis));
  }

  // VC: Coefficient of Variation (VC)
  private static double calculateCoefficientOfVariation(double[] lovis) {
    double mean = Arrays.stream(lovis).average().orElse(Double.NaN);
    return calculateStandardDeviation(lovis) / mean;
  }

  // RA: Range (RA)
  private static double calculateRange(double[] lovis) {
    double max = Arrays.stream(lovis).max().orElse(Double.NaN);
    double min = Arrays.stream(lovis).min().orElse(Double.NaN);
    return max - min;
  }

  // MIN: Minimum (MN)
  private static double min(double[] lovis) {
    return Arrays.stream(lovis).min().orElse(Double.NaN);
  }

  // MAX: Maximum (MX)
  private static double max(double[] lovis) {
    return Arrays.stream(lovis).max().orElse(Double.NaN);
  }

  // Q1: Quartile 1 (Q1)
  private static double calculateQ1(double[] lovis) {
    DescriptiveStatistics stats = new DescriptiveStatistics(lovis);
    return stats.getPercentile(Q1);
  }

  // Q2: Quartile 2 (Q2)
  private static double calculateQ2(double[] lovis) {
    DescriptiveStatistics stats = new DescriptiveStatistics(lovis);
    return stats.getPercentile(Q2);
  }

  // Q3: Quartile 3 (Q3)
  private static double calculateQ3(double[] lovis) {
    DescriptiveStatistics stats = new DescriptiveStatistics(lovis);
    return stats.getPercentile(Q3);
  }

  // IQR: Interquartile Range (I50)
  private static double calculateInterquartileRange(double[] lovis) {
    DescriptiveStatistics stats = new DescriptiveStatistics(lovis);
    return stats.getPercentile(Q3) - stats.getPercentile(Q1);
  }

  // S: Skewness (S)
  private static double calculateSkewness(double[] lovis) {
    Skewness skewness = new Skewness();
    return skewness.evaluate(lovis);
  }

  // K: Kurtosis (K)
  private static double calculateKurtosis(double[] lovis) {
    Kurtosis kurtosis = new Kurtosis();
    return kurtosis.evaluate(lovis); // Excess kurtosis
  }

  public static double computeStatisticOperator(double[] lovis, AggregatorOperators operator) {
    switch (operator.getCode()) {
      case "V":
        return calculateVariance(lovis);
      case "SD":
        return calculateStandardDeviation(lovis);
      case "VC":
        return calculateCoefficientOfVariation(lovis);
      case "RA":
        return calculateRange(lovis);
      case "Q1":
        return calculateQ1(lovis);
      case "Q2":
        return calculateQ2(lovis);
      case "Q3":
        return calculateQ3(lovis);
      case "I50":
        return calculateInterquartileRange(lovis);
      case "S":
        return calculateSkewness(lovis);
      case "K":
        return calculateKurtosis(lovis);
      case "MX":
        return max(lovis);
      case "MN":
        return min(lovis);
      default:
        throw new IllegalArgumentException(
            "Invalid statistic operator code: " + operator.getCode());
    }
  }

  public static void main(String[] args) {
    double[] lovis = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};

    logger.log(Level.INFO, "Variance: {0}", calculateVariance(lovis));
    logger.log(Level.INFO, "Standard Deviation: {0}", calculateStandardDeviation(lovis));
    logger.log(Level.INFO, "Coefficient of Variation: {0}", calculateCoefficientOfVariation(lovis));
    logger.log(Level.INFO, "Range: {0}", calculateRange(lovis));
    logger.log(Level.INFO, "Q1: {0}", calculateQ1(lovis));
    logger.log(Level.INFO, "Q2: {0}", calculateQ2(lovis));
    logger.log(Level.INFO, "Q3: {0}", calculateQ3(lovis));
    logger.log(Level.INFO, "Interquartile Range: {0}", calculateInterquartileRange(lovis));
    logger.log(Level.INFO, "Skewness: {0}", calculateSkewness(lovis));
    logger.log(Level.INFO, "Kurtosis: {0}", calculateKurtosis(lovis));
    logger.log(Level.INFO, "MIN: {0}", min(lovis));
    logger.log(Level.INFO, "MAX: {0}", max(lovis));
  }
}
