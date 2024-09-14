package tomocomd.md;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Set;
import org.apache.commons.math3.linear.RealMatrix;
import tomocomd.StartpepException;
import tomocomd.io.ReadPeptideFile;
import tomocomd.model.PeptideContainer;

public class StartpepComputer {

  protected StartpepComputer() {
    throw new IllegalStateException("Utility class");
  }

  public static RealMatrix compute(Set<String> pDSet, String seqFilePath) throws StartpepException {
    try {
      PeptideContainer peptides = ReadPeptideFile.readPeptideFile(seqFilePath);
      return computeForSeqContainer(pDSet, peptides);
    } catch (IOException e) {
      throw StartpepException.ExceptionType.READ_PEPTIDE_FILE_EXCEPTION.get(e);
    } catch (StartpepException e) {
      throw e;
    } catch (Exception e) {
      Thread.currentThread().interrupt();
      throw StartpepException.ExceptionType.COMPUTE_MD_EXCEPTION.get(e);
    }
  }

  public static RealMatrix computeForSeqContainer(Set<String> pDSet, PeptideContainer peptides)
      throws StartpepException, InterruptedException {
    ComputeBatch computeBatch = new ComputeBatch(peptides.size(), pDSet.size());
    computeBatch.computeInBatch(peptides, new LinkedList<>(pDSet));
    return computeBatch.getResults();
  }
}
