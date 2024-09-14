package tomocomd;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.logging.log4j.core.config.Configurator;
import tomocomd.io.SaveDescriptorValue;
import tomocomd.md.ComputeBatch;
import tomocomd.md.ComputeMD;
import tomocomd.md.properties.AminoAcidPropertiesGetter;
import tomocomd.model.Peptide;
import tomocomd.model.PeptideContainer;

/** Hello world! */
public class App {
  private static final Logger logger = Logger.getLogger(App.class.getName());

  public static void main(String[] args) throws Exception {

    Configurator.initialize(
        null, AminoAcidPropertiesGetter.class.getResourceAsStream("/log4j2.xml").toString());

    Peptide peptide =
        new Peptide(
            "id",
            "VQPNRRFGTELADRVRVWTSLSGLIHSDELPGYGITAEEVSRVSSRLGVDSFILLAGTSDRDLVDAVDVIIDRIREALHGVPEETRAANPDGTTRFMRPR");

    PeptideContainer peptides = new PeptideContainer();
    peptides.add(peptide);
    List<String> headings = new LinkedList<>(ComputeMD.generate()).subList(0, 1000);
    long start = System.currentTimeMillis();
    ComputeBatch computeBatch = new ComputeBatch(peptides.size(), headings.size());
    computeBatch.computeInBatch(peptides, headings);
    RealMatrix realMatrix = computeBatch.getResults();
    computeBatch.shutdown();
    logger.log(Level.INFO, "Time elapsed : {0} ms", System.currentTimeMillis() - start);
    SaveDescriptorValue.save("output.csv", peptides, headings, realMatrix);
  }
}
