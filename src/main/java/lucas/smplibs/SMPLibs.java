package lucas.smplibs;

import net.fabricmc.api.ModInitializer;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMPLibs implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("smplibs");
	public static MinecraftServer server;

	@Override
	public void onInitialize() {
		LOGGER.info("Successfully loaded SMPLibs mod");
	}
}
