package lucas.smplibs.mixins;

import lucas.smplibs.SMPLibs;
import lucas.smplibs.config.ConfigManager;
import lucas.smplibs.player.PlayerManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public class SaveConfigs {
  @Inject(method = "saveEverything", at = @At("HEAD"))
  private void onWorldSave(
    boolean silent, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir
  ) {
    try {
      ConfigManager.saveDefault();
      ConfigManager.saveAll();
      PlayerManager.saveDefault();
      PlayerManager.saveAll();
    } catch (Exception e) {
      SMPLibs.LOGGER.error("An unexpected error occurred while trying to save configs: ", e);
    }
  }

  @Inject(method = "stopServer", at = @At("HEAD"))
  private void onWorldClose(CallbackInfo ci) {
    try {
      ConfigManager.saveDefault();
      ConfigManager.saveAll();
      PlayerManager.saveDefault();
      PlayerManager.saveAll();
    } catch (Exception e) {
      SMPLibs.LOGGER.error("An unexpected error occurred while trying to save configs: ", e);
    }
  }
}