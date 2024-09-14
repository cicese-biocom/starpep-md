package tomocomd.md.aggregation;

import java.util.ArrayList;
import java.util.logging.Logger;
import tomocomd.StartpepException;
import tomocomd.math.MathTomocomd;
import tomocomd.model.AggregatorOperators;

public class Information {
  static Logger logger = Logger.getLogger(Information.class.getName());

  protected Information() {}

  public static double totalInformation(double[] a) {
    boolean[] selections = new boolean[a.length];
    int n = a.length;
    ArrayList<ArrayList<String>> equivalenceClass = new ArrayList<>();

    int j;
    for (int i = 0; i < n; ++i) {
      if (!selections[i]) {
        ArrayList<String> subClass = new ArrayList<>();
        subClass.add(String.valueOf(a[i]));
        selections[i] = true;

        for (j = i + 1; j < n; ++j) {
          if (a[i] == a[j] && !selections[j]) {
            subClass.add(String.valueOf(a[j]));
            selections[j] = true;
          }
        }

        equivalenceClass.add(subClass);
      }
    }

    double[] result = new double[equivalenceClass.size()];
    double sum = 0.0;

    for (j = 0; j < result.length; ++j) {
      double size = equivalenceClass.get(j).size();
      sum += size * MathTomocomd.log2(size);
    }

    return n * MathTomocomd.log2(n) - sum;
  }

  public static double standardizedInformation(double[] a) {
    double ti = totalInformation(a);
    int n = a.length;
    return ti / (n * MathTomocomd.log2(n));
  }

  public static double computeInformationOperator(double[] lovis, AggregatorOperators operator) {
    switch (operator.getCode()) {
      case "TIC":
        return totalInformation(lovis);
      case "SIC":
        return standardizedInformation(lovis);
      default:
        throw StartpepException.ExceptionType.INVALID_AGGREGATOR_OPERATOR.get(
            "Invalid information operator code: " + operator.getCode());
    }
  }
}
