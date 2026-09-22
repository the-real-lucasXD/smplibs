package lucas.smplibs.config;

/**
 * Represents the global configs for this SMP libraries, accessible by all other SMP mods.
 *
 * @since 1.0.0
 */
public final class Config {
  static Config INSTANCE = new Config();

  /**
   * The accessor method for the global config. This method is designed to be imported as a static method:
   * <blockquote><pre>
   *   import static lucas.smplibs.config.Config.configs;
   * </pre></blockquote>
   *
   * @return the {@link Config} object cointaining all config information.
   */
  public static Config configs() { return INSTANCE; }
}
