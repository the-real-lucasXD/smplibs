package lucas.smplibs.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lucas.smplibs.SMPInfo;
import lucas.smplibs.SMPLibs;
import lucas.smplibs.teams.TeamManager;
import lucas.smplibs.player.PlayerManager;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the utility class for the management of all config classes.
 *
 * @since 1.0.0
 */
public final class ConfigManager {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Map<String, Object> configs = new ConcurrentHashMap<>();

  @ApiStatus.Internal
  public static void loadAndAttach() {
    try {
      if (attached) return;
      Path path = SMPLibs.configDir();
      boolean created = false;
      Path resolved = path.resolve("configs.json");
      if (Files.exists(resolved)) {
        String json = Files.readString(resolved);
        try {
          Config.INSTANCE = GSON.fromJson(json, Config.class);
          if (Config.INSTANCE != null) created = true;
        } catch (Exception e) {
          SMPLibs.LOGGER.error("Incomplete or corrupted default configs, resetting");
          Path oldPath = path.resolve("configs-old.jsonc");
          Files.createDirectories(oldPath.getParent());
          Files.writeString(oldPath, """
          /*
            This file has corrupted or incomplete configs, as such a new
            default file has been created instead to allow the mod to load
            properly. To restore this set of configs,
            1) Look through this file and search for and fix problems with the
               JSON. Referring to the new config file can help.
            2) Remove this comment.
            3) Stop the server.
            4) Replace file contents of the original config file with
               the contents of this file.
            5) Restart the server. If the problem(s) have been fixed, the server
               should start properly.
          */
          \n
          \n
          """);
          Files.writeString(oldPath, json, StandardOpenOption.APPEND);
          SMPLibs.LOGGER.info("Successfully saved old configs to configs-old.jsonc");
        }
      } if (!created) Config.INSTANCE = new Config();

      for (SMPInfo info : ConfigManager.attachments().values()) {
        path = SMPLibs.configDir(info.id());
        created = false;
        resolved = path.resolve("playerdata.json");
        Object config = info.configClass().getConstructor().newInstance();
        if (Files.exists(resolved)) {
          String json = Files.readString(resolved);
          try {
            config = GSON.fromJson(json, info.configClass());
            if (config != null) created = true;
          } catch (Exception e) {
            SMPLibs.LOGGER.error("Incomplete or corrupted configs, resetting");
            Path oldPath = path.resolve("configs-old.jsonc");
            Files.createDirectories(oldPath.getParent());
            Files.writeString(oldPath, """
            /*
              This file has corrupted or incomplete configs, as such a new
              default file has been created instead to allow the mod to load
              properly. To restore this set of configs,
              1) Look through this file and search for and fix problems with the
                 JSON. Referring to the new config file can help.
              2) Remove this comment.
              3) Stop the server.
              4) Replace file contents of the original config file with
                 the contents of this file.
              5) Restart the server. If the problem(s) have been fixed, the server
                 should start properly.
            */
            \n
            \n
            """);
            Files.writeString(oldPath, json, StandardOpenOption.APPEND);
            SMPLibs.LOGGER.info("Successfully saved old configs to {}/configs-old.jsonc", info.id());
          }
        } if (!created) config = info.configClass().getConstructor().newInstance();
        configs.put(info.id(), config);
      } PlayerManager.loadAndAttach();
      TeamManager.loadAndAttach();
      attached = true;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  @ApiStatus.Internal
  public static void save() {
    try {
      Path path = SMPLibs.configDir().resolve("configs.json");
      Files.createDirectories(path.getParent());
      Files.writeString(path, GSON.toJson(Config.INSTANCE));

      for (SMPInfo info : attachments().values()) {
        path = SMPLibs.configDir(info.id()).resolve("configs.json");
        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(configs.get(info.id())));
      }

      PlayerManager.save();
      TeamManager.save();
    } catch (Exception e) {
      SMPLibs.LOGGER.error("Failed to save configs", e);
    }
  }

  private static final ConcurrentHashMap<String, SMPInfo> attachments = new ConcurrentHashMap<>();
  private static boolean attached = false;
  public static boolean attached() { return attached; }
  public static Map<String, SMPInfo> attachments() { return Collections.unmodifiableMap(attachments); }

  /**
   * Queues for registering (attaching) the SMP in the SMP registry, such that configs and player data will be loaded.
   * <p>
   * As attaching occurs as the last step of the startup phase of the server, {@code queueAttach} must only
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
   * Retrieve the data of a specific player of an SMP. Note that this method returns the data with {@link Object}
   * type. To get the player data in the correct class, cast it with:
   *  <blockquote><pre>
   *   public static CustomConfig customConfigs() {
   *     return (CustomConfig) ConfigManager.get("custom", uuid);
   *   }
   * </pre></blockquote>
   *
   * @param id the unique id of the mod
   * @return the configs for the mod of id {@code id}
   * @throws IllegalArgumentException if the mod of id {@code id} has not been registered
   */
  public static @NonNull Object get(String id) {
    if (!configs.containsKey(id)) throw new NoSuchElementException("No such attachment of name " + id + "!");
    return configs.get(id);
  }
}