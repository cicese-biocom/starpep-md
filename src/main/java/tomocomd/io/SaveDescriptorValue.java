package tomocomd.io;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import org.apache.commons.math3.linear.RealMatrix;
import tomocomd.model.PeptideContainer;

public class SaveDescriptorValue {
  protected SaveDescriptorValue() {}

  public static void save(
      String path, PeptideContainer peptides, List<String> headings, double[][] results)
      throws FileNotFoundException {
    PrintWriter pw = new PrintWriter(path);
    pw.print("Id");
    for (String head : headings) {
      pw.print("," + head);
    }
    pw.println();
    for (int i = 0; i < peptides.size(); i++) {

      pw.print(peptides.get(i).getIdPeptide());
      for (int j = 0; j < headings.size(); j++) {
        pw.print("," + results[i][j]);
      }
    }
    pw.close();
  }

  public static void save(
      String path, PeptideContainer peptides, List<String> headings, RealMatrix results)
      throws FileNotFoundException {
    PrintWriter pw = new PrintWriter(path);
    pw.print("Id");
    for (String head : headings) {
      pw.print("," + head);
    }
    pw.println();
    for (int i = 0; i < peptides.size(); i++) {

      pw.print(peptides.get(i).getIdPeptide());
      for (int j = 0; j < headings.size(); j++) {
        pw.print("," + results.getEntry(i, j));
      }
    }
    pw.close();
  }
}
