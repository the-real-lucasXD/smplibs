package lucas.smplibs.mixins;

import lucas.smplibs.SMPLibs;
import lucas.smplibs.config.ConfigManager;
import lucas.smplibs.player.PlayerManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
public class LoadConfigs {
	@Inject(method = "initServer", at = @At("RETURN"))
	private void init(CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue()) return;
		SMPLibs.server = (MinecraftServer) (Object) this;
    ConfigManager.loadAndAttach();
    PlayerManager.loadAndAttach();
	}
}