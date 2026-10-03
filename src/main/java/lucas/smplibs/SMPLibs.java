package lucas.smplibs;

import net.fabricmc.api.ModInitializer;

import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entrypoint for the SMPLibs mod.
 *
 * @since 1.0.0
 */
public final class SMPLibs implements ModInitializer {
	public static final @NonNull Logger LOGGER = LoggerFactory.getLogger("smplibs");
	public static MinecraftServer server;

	@Override
	public void onInitialize() {}
}