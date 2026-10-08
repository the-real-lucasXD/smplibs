package lucas.smplibs.listeners.mixins;

import lucas.smplibs.listeners.ServerSavedListener;
import lucas.smplibs.listeners.ServerStoppingListener;
import lucas.smplibs.listeners.SystemMessageManager;
import lucas.smplibs.listeners.TickListener;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(MinecraftServer.class)
class ServerListeners {
  @Unique private static boolean firstSave = true;

  @Inject(method = "tickServer", at = @At("HEAD"))
  private void tickServer(CallbackInfo ci) {
    TickListener.trigger();
    SystemMessageManager.tick();
  }

  @Inject(method = "saveEverything", at = @At("HEAD"))
  private void onWorldSave(
    boolean silent, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir
  ) {
    if (!firstSave) ServerSavedListener.trigger();
    firstSave = false;
  }

  @Inject(method = "stopServer", at = @At("HEAD"))
  private void onWorldClose(CallbackInfo ci) {
    ServerStoppingListener.trigger();
  }
}