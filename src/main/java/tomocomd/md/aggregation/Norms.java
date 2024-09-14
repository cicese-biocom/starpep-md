package tomocomd.md.aggregation;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import tomocomd.StartpepException;
import tomocomd.model.AggregatorOperators;

public class Norms {

  private static final Logger logger = Logger.getLogger(Norms.class.getName());

  // N2: Euclidean Norm (N2)
  private static double euclideanNorm(double[] lovis) {
    RealVector realVector = new ArrayRealVector(lovis);
    return realVector.getNorm();
  }

  // N1: Manhattan Norm (N1)
  private static double manhattanNorm(double[] lovis) {
    RealVector realVector = new ArrayRealVector(lovis);
    return realVector.getL1Norm();
  }

  // N3: Minkowski Norm (N3)

  private static double minkowskiNorm(double[] lovis) {
    double sum = 0.0;

    for (int i = 0; i < lovis.length; ++i) {
      sum += Math.pow(lovis[i], 3);
    }

    return Math.cbrt(sum);
  }

  public static double computeNormOperator(double[] lovis, AggregatorOperators operator) {
    switch (operator.getCode()) {
      case "N1":
        return manhattanNorm(lovis);
      case "N2":
        return euclideanNorm(lovis);
      case "N3":
        return minkowskiNorm(lovis);
      default:
        throw StartpepException.ExceptionType.INVALID_AGGREGATOR_OPERATOR.get(
            "Invalid Norm operator code: " + operator.getCode());
    }
  }

  public static void main(String[] args) {
    double[] vector = {1.0, 2.0, 3.0};

    logger.log(Level.INFO, "Euclidean Norm: {0}", Norms.euclideanNorm(vector));
    logger.log(Level.INFO, "Manhattan Norm: {0}", Norms.manhattanNorm(vector));
    logger.log(Level.INFO, "Minkowski Norm: {0}", Norms.minkowskiNorm(vector));
  }
}
