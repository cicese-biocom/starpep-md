package tomocomd.model;

import java.util.Objects;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.ProteinSequence;

/**
 * @author loge and Luis
 */
public class Peptide {

  protected final String idPeptide;
  protected final String seqPeptide;
  protected final ProteinSequence bioJavaSeq;

  protected int[][] matrix;

  public Peptide(String idPeptide, String seqPeptide) throws CompoundNotFoundException {
    this.idPeptide = idPeptide;
    this.seqPeptide = seqPeptide;
    this.bioJavaSeq = new ProteinSequence(seqPeptide);
    calcAdjancencyMatrix();
  }

  public Peptide(ProteinSequence proteinSequence) throws CompoundNotFoundException {
    this.idPeptide = proteinSequence.getOriginalHeader();
    this.seqPeptide = proteinSequence.getSequenceAsString();
    this.bioJavaSeq = new ProteinSequence(String.valueOf(proteinSequence));
    calcAdjancencyMatrix();
  }

  public ProteinSequence getBioJavaSeq() throws CompoundNotFoundException {
    return Objects.nonNull(bioJavaSeq) ? bioJavaSeq : new ProteinSequence(seqPeptide);
  }

  public int getLength() {
    return seqPeptide.length();
  }

  public int[][] getMatrix() {
    return matrix;
  }

  public String getIdPeptide() {
    return idPeptide;
  }

  public String getSeqPeptide() {
    return seqPeptide;
  }

  @Override
  public String toString() {
    return seqPeptide;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 37 * hash + Objects.hashCode(getIdPeptide());
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Peptide other = (Peptide) obj;
    return Objects.equals(this.getIdPeptide(), other.getIdPeptide());
  }

  private void calcAdjancencyMatrix() {
    this.matrix = new int[getLength()][getLength()];
    int n = getLength();
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        this.matrix[i][j] = (j == i - 1 || j == i + 1) ? 1 : 0;
      }
    }
  }
}
