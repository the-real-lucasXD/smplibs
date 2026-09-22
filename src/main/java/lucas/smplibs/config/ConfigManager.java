package lucas.smplibs.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lucas.smplibs.SMPInfo;
import lucas.smplibs.SMPLibs;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the utility class for the initialization of all config classes.
 *
 * @since 1.0.0
 */
public final class ConfigManager {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final ConcurrentHashMap<String, SMPInfo> attachments = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Object> values = new ConcurrentHashMap<>();
  private static boolean attached = false;

  public static Map<String, SMPInfo> attachments() { return Collections.unmodifiableMap(attachments); }

  /**
   * Queues for registering (attaching) the SMP in the SMP registry, such that configs and player data will be loaded.
   *
   * @implNote As attaching occurs as the last step of the startup phase of the server, {@code queueAttach} must only
   * be called during the startup phase. It is recommended that the mod is initialized in the {@code onInitialize}
   * method in the mod entrypoint class.
   * @param info the {@link SMPInfo} object containing all metadata and components of the mod
   * @throws RuntimeException if the method is called after the server has loaded, or if another mod of that id has
   * already been registered.
   */
  public static void queueAttach(SMPInfo info) {
    if (attached) throw new RuntimeException("You can only call this command during the startup phase of the server.");
    if (attachments.containsKey(info.id())) throw new RuntimeException("Another mod of this id has already been registered.");
    attachments.put(info.id(), info);
  }

  /**
   * Attaches the configs for all queued SMPs into the registry. This method should not be called, as it is automatically
   * called at the end of server startup.
   */
  @ApiStatus.Internal
  public static void loadAndAttach() {
    try {
      if (attached) return;
      var path = FabricLoader.getInstance().getConfigDir().resolve("smp/configs.json");
      if (Files.exists(path)) {
        try {
          Config.INSTANCE = GSON.fromJson(Files.readString(path), Config.class);
          if (Config.INSTANCE == null) Config.INSTANCE = new Config();
        } catch (Exception e) {
          SMPLibs.LOGGER.error("An unexpected error occurred while attempting to parse default configs. " +
            "This could be due to a corrupted or malformed configs.json. Deleting that file may fix the problem. " +
            "The full error log will be included in the exception thrown below."
          );
          throw new RuntimeException(e);
        }
      }
      saveDefault();

      for (SMPInfo info : attachments.values()) {
        Object configs;
        path = FabricLoader.getInstance().getConfigDir().resolve("smp/" + info.id() + "/configs.json");
        if (Files.exists(path)) {
          try {
            configs = GSON.fromJson(Files.readString(path), info.configClass());
            if (configs == null) configs = info.configClass().getDeclaredConstructor().newInstance();
          } catch (Exception e) {
            SMPLibs.LOGGER.error("An unexpected error occurred while attempting to parse configs for the mod " +
                "with id '{}'. This could be due to a corrupted or malformed {}/configs.json. Deleting that file " +
                "may fix the problem. The full error log will be included in the exception thrown below.",
              info.id(), info.id()
            );
            throw new RuntimeException(e);
          }
        } else configs = info.configClass().getDeclaredConstructor().newInstance();
        values.put(info.id(), configs);
        save(info.id(), configs);
      }

      attached = true;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void save(String id, Object values) throws IOException {
    var path = FabricLoader.getInstance().getConfigDir().resolve("smp/"+id+"/configs.json");
    Files.createDirectories(path.getParent());
    if (Files.notExists(path)) Files.createFile(path);
    Files.writeString(path, GSON.toJson(values));
  }

  /**
   * Saves the default configs. This method should not be called, as it is automatically called each time
   * the server is saved.
   *
   * @throws IOException if an error occurrs while saving the configs.
   */
  @ApiStatus.Internal
  public static void saveDefault() throws IOException {
    var path = FabricLoader.getInstance().getConfigDir().resolve("smp/configs.json");
    Files.createDirectories(path.getParent());
    if (Files.notExists(path)) Files.createFile(path);
    Files.writeString(path, GSON.toJson(Config.INSTANCE));
  }

  /**
   * Retrieve the global configs of a mod. Note that this method returns the configs with {@link Object} type.
   * To get the configs of the mod in the correct class, cast it with:
   *  <blockquote><pre>
   *   public static CustomConfig customConfigs() {
   *     return (CustomConfig) ConfigManager.get("custom");
   *   }
   * </pre></blockquote>
   *
   *
   * @param id the unique id of the mod
   * @return the configs for that mod, with type {@link Object}
   */
  public static Object get(String id) {
    return values.get(id);
  }

  /**
   * Saves the configs of all custom SMPs. This method should not be called, as it is automatically called each time
   * the server is saved.
   */
  @ApiStatus.Internal
  public static void saveAll() {
    values.forEach((id, values) -> {
      try {
        save(id, values);
      } catch (IOException e) {
        SMPLibs.LOGGER.error("An error occurred while trying to save configs for mod {}: ", id, e);
      }
    });
  }
}
