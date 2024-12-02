package tomocomd.md;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tomocomd.io.ReadPeptideFile;
import tomocomd.model.PeptideContainer;

class ComputeBatchTest {

  ComputeBatch computeBatch;
  PeptideContainer peptides;
  List<String> pdSet = List.of("MIC_S_T_ptt", "TIC_T_ptt", "ES_TIC_T_ptt");

  @BeforeEach
  public void setUp() throws Exception {
    String path =
        Paths.get(
                Objects.requireNonNull(
                    getClass().getClassLoader().getResource("peptides.fasta").toURI()))
            .toString();
    peptides = ReadPeptideFile.readPeptideFile(path);
    computeBatch = new ComputeBatch(peptides.size(), pdSet.size());
  }

  @Test
  void testComputeInBatch() throws InterruptedException {
    computeBatch.computeInBatch(peptides, pdSet);
    RealMatrix results = computeBatch.getResults();
    assertNotNull(results);
    assertEquals(9, results.getRowDimension());
    assertEquals(3, results.getColumnDimension());
    assertArrayEquals(
        new double[] {1.1298738344298023, 151.31881626063642, 226.47733175670794},
        results.getRow(0));
  }
}
