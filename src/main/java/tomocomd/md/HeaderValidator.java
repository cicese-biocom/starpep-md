package tomocomd.md;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tomocomd.StartpepException;
import tomocomd.model.AMINOACID_PROPERTY;
import tomocomd.model.AggregatorOperators;
import tomocomd.model.GROUPS;

public class HeaderValidator {

  private static final Logger log = LoggerFactory.getLogger(HeaderValidator.class);

  protected HeaderValidator() {
    throw new UnsupportedOperationException("This class cannot be instantiated");
  }

  public static Map<String, Object> validateHeaderAndGetOperators(String header) {
    if (header == null) {
      throw new IllegalArgumentException("Header is null");
    }

    if (header.isEmpty()) {
      throw new IllegalArgumentException("Header is empty");
    }

    String[] components = header.split("_");

    if (components.length < 3 || components.length > 4) {
      throw new IllegalArgumentException("Header must have at least three components");
    }

    AggregatorOperators[] aggregators = getAggregators(components);

    GROUPS g =
        components.length == 4 ? GROUPS.fromCode(components[2]) : GROUPS.fromCode(components[1]);

    AMINOACID_PROPERTY p =
        components.length == 4
            ? AMINOACID_PROPERTY.valueOf(components[3])
            : AMINOACID_PROPERTY.valueOf(components[2]);

    Map<String, Object> res = new LinkedHashMap<>();
    res.put("CLASSICAL", aggregators[0]);
    res.put("NO_CLASSICAL", aggregators[1]);
    res.put("GROUP", g);
    res.put("PROPERTY", p);
    return res;
  }

  private static AggregatorOperators[] getAggregators(String[] components) {
    AggregatorOperators cA = components.length == 4 ? new AggregatorOperators(components[0]) : null;
    if (Objects.nonNull(cA) && !cA.getType().equals("CLASSIC"))
      throw StartpepException.ExceptionType.INVALID_AGGREGATOR_OPERATOR.get(
          components[0] + " is not a classic operator");

    final AggregatorOperators nCA;
    if (components.length == 4) nCA = new AggregatorOperators(components[1]);
    else {
      nCA = new AggregatorOperators(components[0]);
      if (nCA.getType().equals("CLASSIC")) {
        cA = nCA;
      }
    }
    return new AggregatorOperators[] {cA, nCA};
  }

  public static boolean validateHeader(String header) {
    try {
      validateHeaderAndGetOperators(header);
      return true;
    } catch (Exception e) {
      log.error(e.getMessage());
      return false;
    }
  }
}
