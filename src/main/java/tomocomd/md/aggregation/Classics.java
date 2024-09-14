package tomocomd.md.aggregation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import tomocomd.StartpepException;
import tomocomd.math.MathTomocomd;
import tomocomd.model.AggregatorOperators;
import tomocomd.model.Peptide;

public class Classics {

  static Logger logger = Logger.getLogger(Classics.class.getName());

  protected Classics() {}

  private static double[] meanInformation(double[] a) {

    boolean[] selections = new boolean[a.length];
    int n = a.length;
    ArrayList<ArrayList<String>> equivalenceClass = new ArrayList<>();

    int i;
    for (i = 0; i < n; ++i) {
      if (!selections[i]) {
        ArrayList<String> subClass = new ArrayList<>();
        subClass.add(String.valueOf(a[i]));
        selections[i] = true;

        for (int j = i + 1; j < n; ++j) {
          if (a[i] == a[j] && !selections[j]) {
            subClass.add(String.valueOf(a[j]));
            selections[j] = true;
          }
        }

        equivalenceClass.add(subClass);
      }
    }

    double[] result = new double[equivalenceClass.size()];

    for (i = 0; i < result.length; ++i) {
      double size = equivalenceClass.get(i).size();
      double pi = size / n;
      result[i] = -1.0 * pi * MathTomocomd.log2(pi);
    }

    return result;
  }

  private static double[] autocorelation(double[] a, Peptide peptide, int k) {
    ArrayList<Double> elements = new ArrayList<>();
    int[][] distanceMatrix = computeFloydAPSP(peptide.getMatrix());
    int n = peptide.getLength();

    int j;
    for (int i = 0; i < n; ++i) {
      for (j = i; j < n; ++j) {
        if (distanceMatrix[i][j] == k) {
          elements.add(a[i] * a[j]);
        }
      }
    }

    return elements.stream().mapToDouble(Double::doubleValue).toArray();
  }

  private static double[] gravitational(double[] a, Peptide peptide, int k) {

    ArrayList<String> elements = new ArrayList<>();
    int n = peptide.getLength();
    int[][] distanceMatrix = computeFloydAPSP(peptide.getMatrix());

    int i;
    for (i = 0; i < n; ++i) {
      for (int j = i + 1; j < n; ++j) {
        if (distanceMatrix[i][j] == k) {
          double r = a[i] * a[j] / k;
          String e = String.valueOf(r);
          elements.add(e);
        }
      }
    }

    double[] result = new double[elements.size()];

    for (i = 0; i < elements.size(); ++i) {
      result[i] = Double.parseDouble(elements.get(i));
    }

    return result;
  }

  private static double[] totalSumLagK(double[] a, Peptide peptide, int k) {
    ArrayList<String> elements = new ArrayList<>();
    int n = peptide.getLength();
    int[][] distanceMatrix = computeFloydAPSP(peptide.getMatrix());

    int i;
    for (i = 0; i < n; ++i) {
      for (int j = i + 1; j < n; ++j) {
        if (distanceMatrix[i][j] == k) {
          double r = a[i] + a[j];
          String e = String.valueOf(r);
          elements.add(e);
        }
      }
    }

    double[] result = new double[elements.size()];

    for (i = 0; i < elements.size(); ++i) {
      result[i] = Double.parseDouble(elements.get(i));
    }

    return result;
  }

  private static double[] electroTopologicalState(double[] lovis, Peptide peptide) {
    int[][] distanceMatrix = computeFloydAPSP(peptide.getMatrix());
    int longitud = peptide.getLength();

    double[] sI = new double[longitud];
    double lI;

    for (int i = 0; i < longitud; i++) {
      lI = lovis[i];
      double sum = 0;

      for (int j = 0; j < longitud; j++) {
        int dij = distanceMatrix[i][j] + 1;
        sum = sum + (lI - lovis[j]) / Math.pow(dij, 2);
      }
      sI[i] = lI + sum;
    }

    return sI;
  }

  private static int[][] computeFloydAPSP(int[][] costMatrix) {
    int i;
    int j;
    int k;
    int nRow = costMatrix.length;
    int[][] distMatrix = new int[nRow][nRow];
    for (i = 0; i < nRow; i++) {
      for (j = 0; j < nRow; j++) {
        if (costMatrix[i][j] == 0) {
          distMatrix[i][j] = 999999999;
        } else {
          distMatrix[i][j] = 1;
        }
      }
    }
    for (i = 0; i < nRow; i++) {
      distMatrix[i][i] = 0;
    }
    for (k = 0; k < nRow; k++) {
      for (i = 0; i < nRow; i++) {
        for (j = 0; j < nRow; j++) {
          if (distMatrix[i][k] + distMatrix[k][j] < distMatrix[i][j]) {
            distMatrix[i][j] = distMatrix[i][k] + distMatrix[k][j];
          }
        }
      }
    }
    return distMatrix;
  }

  public static double[] computeClassicalOperator(
      double[] lovis, Peptide peptide, AggregatorOperators operator) {
    switch (operator.getCode()) {
      case "ES":
        return electroTopologicalState(lovis, peptide);

      case "MIC":
        return meanInformation(lovis);
      case "AC[1]":
      case "AC[2]":
      case "AC[3]":
      case "AC[4]":
      case "AC[5]":
      case "AC[6]":
      case "AC[7]":
        return autocorelation(lovis, peptide, operator.getK());
      case "GV[1]":
      case "GV[2]":
      case "GV[3]":
      case "GV[4]":
      case "GV[5]":
      case "GV[6]":
      case "GV[7]":
        return gravitational(lovis, peptide, operator.getK());
      case "TS[1]":
      case "TS[2]":
      case "TS[3]":
      case "TS[4]":
      case "TS[5]":
      case "TS[6]":
      case "TS[7]":
        return totalSumLagK(lovis, peptide, operator.getK());
      default:
        throw StartpepException.ExceptionType.INVALID_AGGREGATOR_OPERATOR.get(
            "Invalid Classic operator code: " + operator.getCode());
    }
  }

  public static void main(String[] args) throws CompoundNotFoundException {
    double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
    Peptide p = new Peptide("id", "AGSKV");

    logger.log(Level.INFO, "MIC: {0}", Arrays.toString(meanInformation(values)));
    logger.log(Level.INFO, "ES: {0}", Arrays.toString(electroTopologicalState(values, p)));
    for (int i = 1; i <= 7; i++) {
      logger.log(
          Level.INFO,
          "AC[{1}]: {0}",
          new Object[] {Arrays.toString(autocorelation(values, p, i)), i});
      logger.log(
          Level.INFO,
          "GV[{1}]: {0}",
          new Object[] {Arrays.toString(gravitational(values, p, i)), i});
      logger.log(
          Level.INFO,
          "TS[{1}]: {0}",
          new Object[] {Arrays.toString(totalSumLagK(values, p, i)), i});
    }
  }
}
