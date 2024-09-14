package tomocomd.md;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import tomocomd.StartpepException;
import tomocomd.md.aggregation.*;
import tomocomd.md.properties.AminoAcidPropertiesGetter;
import tomocomd.model.*;

public class ComputeMD {

  private static final Logger logger = Logger.getLogger(ComputeMD.class.getName());

  protected ComputeMD() {
    throw StartpepException.ExceptionType.COMPUTE_MD_EXCEPTION.get("Constructor not allowed");
  }

  public static double[] computeInBatch(Peptide peptide, Set<String> headings) {
    return headings.stream()
        .map(h -> compute(peptide, h))
        .mapToDouble(Double::doubleValue)
        .toArray();
  }

  public static double[][] computeInBatch(PeptideContainer peptides, List<String> headings)
      throws InterruptedException {
    int availableProcessors = Runtime.getRuntime().availableProcessors();
    ExecutorService executorService =
        java.util.concurrent.Executors.newFixedThreadPool(availableProcessors);

    List<Callable<Object>> tasks = new ArrayList<>();
    double[][] results = new double[peptides.size()][headings.size()];

    for (int i = 0; i < peptides.size(); i++) {
      for (int j = 0; j < headings.size(); j++) {
        int finalI = i;
        int finalJ = j;
        tasks.add(
            () -> results[finalI][finalJ] = compute(peptides.get(finalI), headings.get(finalJ)));
      }
    }

    executorService.invokeAll(tasks);
    executorService.shutdown();
    return results;
  }

  public static double compute(Peptide peptide, String heading) {

    Map<String, Object> operators = HeaderValidator.validateHeaderAndGetOperators(heading);

    try {
      AminoAcidPropertiesGetter aminoAcidPropertiesGetter = new AminoAcidPropertiesGetter();
      double[] lovis =
          aminoAcidPropertiesGetter.getAminoacidPropertyValues(
              peptide.getSeqPeptide(),
              (AMINOACID_PROPERTY) operators.get("PROPERTY"),
              (GROUPS) operators.get("GROUP"));

      return applyAggregationOperators(
          peptide,
          lovis,
          (AggregatorOperators) operators.get("CLASSICAL"),
          (AggregatorOperators) operators.get("NO_CLASSICAL"));

    } catch (IOException e) {
      throw StartpepException.ExceptionType.COMPUTE_MD_EXCEPTION.get(e.getMessage());
    }
  }

  private static double applyAggregationOperators(
      Peptide peptide, double[] lovis, AggregatorOperators classic, AggregatorOperators noClassic) {
    double[] lovisLocal =
        Objects.nonNull(classic)
            ? Classics.computeClassicalOperator(lovis, peptide, classic)
            : lovis;
    return applyNoClassicOperator(lovisLocal, noClassic);
  }

  private static double applyNoClassicOperator(double[] lovis, AggregatorOperators noClassic) {
    switch (noClassic.getType()) {
      case "INFORMATION":
        return Information.computeInformationOperator(lovis, noClassic);
      case "NORM":
        return Norms.computeNormOperator(lovis, noClassic);
      case "MEAN":
        return Means.computeMeansOperator(lovis, noClassic);
      case "STATISTIC":
        return Statistics.computeStatisticOperator(lovis, noClassic);
      case "CHOQUET":
        return Choquet.validateAndCompute(lovis, noClassic.getCode());
      case "GOWAWA":
        return Gowawa.validateAndCompute(lovis, noClassic.getCode());
      default:
        throw StartpepException.ExceptionType.INVALID_AGGREGATOR_OPERATOR.get(
            "Invalid No Classic operator code: " + noClassic.getCode());
    }
  }

  public static void main(String[] args) throws CompoundNotFoundException {
    Peptide p = new Peptide("id", "AGSKVAGSKV");

    String h = "MIC_S_T_ptt";
    ComputeMD.compute(p, h);
    int cont = 1;
    Set<String> headers = generate();
    for (String heading : headers) {
      try {
        double value = ComputeMD.compute(p, heading);
        logger.log(Level.INFO, "{0}:{1}={2}", new Object[]{cont++, heading, value});
      } catch (Exception e) {
        logger.log(Level.INFO, "heading {} not valid", heading);
        System.exit(-1);
      }
    }
  }

  public static Set<String> generate() {
    List<GROUPS> groups = List.of(GROUPS.values());
    List<AMINOACID_PROPERTY> properties = List.of(AMINOACID_PROPERTY.values());
    List<String> choquet = List.of(Choquet.defaultChoquet);
    List<String> gowawa = List.of(Gowawa.defaultOWAWAs);
    List<String> aNC =
        new LinkedList<>(
            List.of(
                "TIC", "SIC", "AM", "GM", "P2", "P3", "HM", "N1", "N2", "N3", "V", "SD", "VC", "RA",
                "Q1", "Q2", "Q3", "I50", "S", "K", "MX", "MN"));
    aNC.addAll(choquet);
    aNC.addAll(gowawa);
    String[] classics =
        new String[] {
          "ES", "MIC", "AC[1]", "AC[2]", "AC[3]", "AC[4]", "AC[5]", "AC[6]", "AC[7]", "GV[1]",
          "GV[2]", "GV[3]", "GV[4]", "GV[5]", "GV[6]", "GV[7]", "TS[1]", "TS[2]", "TS[3]", "TS[4]",
          "TS[5]", "TS[6]", "TS[7]"
        };

    Set<String> headers = new LinkedHashSet<>();
    for (GROUPS group : groups) {
      for (AMINOACID_PROPERTY property : properties) {
        for (String nClassic : aNC) {
          headers.add(nClassic + "_" + group.getCode() + "_" + property.toString());
          for (String classic : classics) {
            headers.add(classic + "_" + nClassic + "_" + group.getCode() + "_" + property);
          }
        }
      }
    }
    return headers;
  }
}
