package lucas.smplibs;

import lucas.smplibs.combat.Combat;
import lucas.smplibs.config.ConfigManager;
import lucas.smplibs.listeners.ServerSavedListener;
import lucas.smplibs.listeners.ServerStartedListener;
import lucas.smplibs.listeners.ServerStartingListener;
import lucas.smplibs.listeners.ServerStoppingListener;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Entrypoint for the SMPLibs mod.
 *
 * @since 1.0.0
 */
public final class SMPLibs implements ModInitializer {
	public static final @NonNull Logger LOGGER = LoggerFactory.getLogger("smplibs");
	private static MinecraftServer server;
  public static MinecraftServer server() { return server; }

	@Override
	public void onInitialize() {
    ServerStartingListener.register((server) -> SMPLibs.server = server, 1);
    ServerStartedListener.register((_) -> ConfigManager.loadAndAttach(), 1);
    ServerStoppingListener.register(Combat::clearAll, 1);
    ServerStoppingListener.register(ConfigManager::save, 1);
    ServerSavedListener.register(ConfigManager::save);
  }

  public static Path configDir() { return FabricLoader.getInstance().getConfigDir().resolve("smp"); }
  public static Path configDir(String modId) { return configDir().resolve(modId); }
}