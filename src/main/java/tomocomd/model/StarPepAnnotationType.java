package tomocomd.model;

import org.openide.util.NbBundle;

public enum StarPepAnnotationType {
  DATABASE("compiled_in") {

    @Override
    public String getLabelName() {
      return NbBundle.getMessage(StarPepAnnotationType.class, "AnnotationType.database");
    }

    @Override
    public String getDescription() {
      return NbBundle.getMessage(StarPepAnnotationType.class, "AnnotationType.database.desc");
    }
  },
  FUNCTION("related_to") {

    @Override
    public String getLabelName() {
      return NbBundle.getMessage(StarPepAnnotationType.class, "AnnotationType.function");
    }

    @Override
    public String getDescription() {
      return NbBundle.getMessage(StarPepAnnotationType.class, "AnnotationType.function.desc");
    }
  },
  ORIGIN("produced_by") {

    @Override
    public String getLabelName() {
      return NbBundle.getMessage(StarPepAnnotationType.class, "AnnotationType.origin");
    }

    @Override
    public String getDescription() {
      return NbBundle.getMessage(StarPepAnnotationType.class, "AnnotationType.origin.desc");
    }
  },
  TARGET("assessed_against") {

    @Override
    public String getLabelName() {
      return NbBundle.getMessage(StarPepAnnotationType.class, "AnnotationType.target");
    }

    @Override
    public String getDescription() {
      return NbBundle.getMessage(StarPepAnnotationType.class, "AnnotationType.target.desc");
    }
  },
  CROSSREF("linked_to") {

    @Override
    public String getLabelName() {
      return NbBundle.getMessage(StarPepAnnotationType.class, "AnnotationType.crossref");
    }

    @Override
    public String getDescription() {
      return NbBundle.getMessage(StarPepAnnotationType.class, "AnnotationType.crossref.desc");
    }
  },
  Nterminus("modified_by") {

    @Override
    public String getLabelName() {
      return NbBundle.getMessage(StarPepAnnotationType.class, "AnnotationType.nterm");
    }

    @Override
    public String getDescription() {
      return NbBundle.getMessage(StarPepAnnotationType.class, "AnnotationType.nterm.desc");
    }
  },
  Cterminus("modified_by") {

    @Override
    public String getLabelName() {
      return NbBundle.getMessage(StarPepAnnotationType.class, "AnnotationType.cterm");
    }

    @Override
    public String getDescription() {
      return NbBundle.getMessage(StarPepAnnotationType.class, "AnnotationType.cterm.desc");
    }
  },
  UnusualAA("constituted_by") {

    @Override
    public String getLabelName() {
      return NbBundle.getMessage(StarPepAnnotationType.class, "AnnotationType.unusualAA");
    }

    @Override
    public String getDescription() {
      return NbBundle.getMessage(StarPepAnnotationType.class, "AnnotationType.unusualAA.desc");
    }
  };

  private final String relType;

  StarPepAnnotationType(String relType) {
    this.relType = relType;
  }

  public abstract String getLabelName();

  public abstract String getDescription();

  public String getRelationType() {
    return relType;
  }

  @Override
  public String toString() {
    return getLabelName();
  }
}
