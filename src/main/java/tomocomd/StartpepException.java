package tomocomd;

public class StartpepException extends RuntimeException {
  private final ExceptionType type;

  public StartpepException(ExceptionType type) {
    super(type.getMessage());
    this.type = type;
  }

  public StartpepException(ExceptionType type, Throwable cause) {
    super(type.getMessage(), cause);
    this.type = type;
  }

  public StartpepException(ExceptionType type, Throwable cause, String message) {
    super(type.formatMessage(message), cause);
    this.type = type;
  }

  public StartpepException(ExceptionType type, String message) {
    super(type.formatMessage(message));
    this.type = type;
  }

  public enum ExceptionType {
    INVALID_AGGREGATOR_OPERATOR("Invalid aggregator operator"),
    INVALID_GROUP_OPERATOR("Invalid group operator"),
    INVALID_PROPERTY_OPERATOR("Invalid property operator"),
    INVALID_SORT_CHOQUET_OPERATOR("Invalid sort operator for Choquet aggregator"),
    INVALID_SINGLETON_METHOD("Invalid singleton method for Choquet aggregator"),
    COMPUTE_MD_EXCEPTION("Error computing Startpep MD"),
    READ_PEPTIDE_FILE_EXCEPTION("Error reading peptide file");
    private final String message;

    ExceptionType(String message) {
      this.message = message;
    }

    public String formatMessage(String message) {
      return String.format("%s: %s", this.message, message);
    }

    public StartpepException get() {
      return new StartpepException(this);
    }

    public StartpepException get(String message) {
      return new StartpepException(this, message);
    }

    public StartpepException get(Throwable cause) {
      return new StartpepException(this, cause);
    }

    public StartpepException get(String message, Throwable cause) {
      return new StartpepException(this, cause, message);
    }

    public String getMessage() {
      return message;
    }
  }
}
