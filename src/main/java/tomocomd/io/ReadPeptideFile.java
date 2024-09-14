package tomocomd.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;
import tomocomd.StartpepException;
import tomocomd.model.Peptide;
import tomocomd.model.PeptideContainer;

public class ReadPeptideFile {
  protected ReadPeptideFile() {}

  public static PeptideContainer readPeptideFile(String fileName) throws IOException {
    Map<String, ProteinSequence> peptides =
        FastaReaderHelper.readFastaProteinSequence(new File(fileName));
    return peptides.entrySet().stream()
        .map(
            pep -> {
              try {
                return new Peptide(pep.getKey(), pep.getValue().getSequenceAsString());
              } catch (CompoundNotFoundException e) {
                throw StartpepException.ExceptionType.READ_PEPTIDE_FILE_EXCEPTION.get(e);
              }
            })
        .collect(Collectors.toCollection(PeptideContainer::new));
  }

  public static PeptideContainer readPeptideFile(InputStream file) throws IOException {
    Map<String, ProteinSequence> peptides = FastaReaderHelper.readFastaProteinSequence(file);
    return peptides.entrySet().stream()
        .map(
            pep -> {
              try {
                return new Peptide(pep.getKey(), pep.getValue().getSequenceAsString());
              } catch (CompoundNotFoundException e) {
                throw StartpepException.ExceptionType.READ_PEPTIDE_FILE_EXCEPTION.get(e);
              }
            })
        .collect(Collectors.toCollection(PeptideContainer::new));
  }
}
