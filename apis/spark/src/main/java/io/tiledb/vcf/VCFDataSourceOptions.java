package io.tiledb.vcf;

import com.amazonaws.auth.AWSSessionCredentialsProvider;
import io.tiledb.util.CredentialProviderUtils;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.sources.v2.DataSourceOptions;

/** Class holding option values for TileDB-VCF. */
public class VCFDataSourceOptions implements Serializable {
  private Map<String, String> options;

  /**
   * Parses TileDB-VCF specific options from generic Spark Datasource options
   *
   * @param options Spark Datasource options
   */
  public VCFDataSourceOptions(DataSourceOptions options) {
    this.options = new HashMap<>();
    this.options.putAll(options.asMap());
  }

  /** @return TileDB-VCF dataset URI */
  public Optional<URI> getDatasetURI() {
    if (options.containsKey("uri")) {
      return Optional.of(URI.create(options.get("uri")));
    }
    return Optional.empty();
  }

  /** @return Optional array of provided sample names to read */
  public Optional<String[]> getSamples() {
    if (options.containsKey("samples")) {
      return Optional.of(options.get("samples").split("\\s*,[,\\s]*"));
    }
    return Optional.empty();
  }

  /** @return Optional array of contig regions to read */
  public Optional<String[]> getRanges() {
    if (options.containsKey("ranges")) {
      return Optional.of(options.get("ranges").split("\\s*,[,\\s]*"));
    }
    return Optional.empty();
  }

  /** @return Whether or not to sort the regions */
  public Optional<Boolean> getSortRegions() {
    if (options.containsKey("sortRegions")) {
      return Optional.of(Boolean.parseBoolean(options.get("sortRegions")));
    }
    return Optional.empty();
  }

  /** @return Optional maximum memory budget (MB) */
  public Optional<Integer> getMemoryBudget() {
    if (options.containsKey("memory")) {
      return Optional.of(Integer.parseInt(options.get("memory")));
    }
    return Optional.empty();
  }

  /** @return Optional uri of BED file */
  public Optional<URI> getBedURI() {
    if (options.containsKey("bedfile")) {
      return Optional.of(URI.create(options.get("bedfile")));
    }
    return Optional.empty();
  }

  /** @return Optional uri of SampleFile file */
  public Optional<URI> getSampleURI() {
    if (options.containsKey("samplefile")) {
      return Optional.of(URI.create(options.get("samplefile")));
    }
    return Optional.empty();
  }

  /** @return Optional number of partitions */
  public Optional<Integer> getPartitions() {
    if (options.containsKey("partitions")) {
      return Optional.of(Integer.parseInt(options.get("partitions")));
    }
    return Optional.empty();
  }

  /** @return Optional number of range partitions */
  public Optional<Integer> getRangePartitions() {
    if (options.containsKey("range_partitions")) {
      return Optional.of(Integer.parseInt(options.get("range_partitions")));
    }
    return Optional.empty();
  }

  /** @return Optional number of sample partitions */
  public Optional<Integer> getSamplePartitions() {
    if (options.containsKey("sample_partitions")) {
      return Optional.of(Integer.parseInt(options.get("sample_partitions")));
    }
    return Optional.empty();
  }

  /** @return Optional credentials provider for managing AWS access to array buckets. */
  public Optional<AWSSessionCredentialsProvider> getCredentialsProvider() {
    if (options.containsKey("aws_role_arn") && options.containsKey("aws_credentials_provider")) {
      return CredentialProviderUtils.get(
          options.get("aws_credentials_provider"), options.get("aws_role_arn"));
    }
    return Optional.empty();
  }

  /** @return The log level for the VCFReader stats reporting */
  public Optional<String> getTileDBStatsLogLevel() {
    if (options.containsKey("tiledb_stats_log_level")) {
      String statsLogLevel = options.get("tiledb_stats_log_level");
      return Optional.of(statsLogLevel);
    }
    return Optional.empty();
  }

  /** @return Optional CSV String of config parameters */
  public Optional<String> getConfigCSV() {
    return getConfigCSV(options);
  }

  /**
   * Generic parser of key-value property maps into a CSV.
   *
   * @return Optional CSV String of config parameters
   */
  protected static Optional<String> getConfigCSV(final Map<String, String> configMap) {

    List<String> entries =
        configMap
            .entrySet()
            .stream()
            .filter(e -> e.getKey().startsWith("tiledb."))
            .map(
                e ->
                    String.format(
                        "%s=%s", e.getKey().substring(7) /* strip prefix */, e.getValue()))
            .collect(Collectors.toList());

    return entries.isEmpty() ? Optional.empty() : Optional.of(StringUtils.join(entries, ","));
  }

  /**
   * Combines two optional configuration into one.
   *
   * @return Optional CSV String of config parameters.
   */
  protected static Optional<String> combineCsvOptions(
      Optional<String> first, Optional<String> second) {
    if (!first.isPresent()) {
      return second;
    } else if (!second.isPresent()) {
      return first;
    } else {
      return Optional.of(first.get().concat(",").concat(second.get()));
    }
  }
}
