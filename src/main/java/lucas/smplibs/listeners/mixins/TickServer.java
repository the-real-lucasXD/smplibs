package lucas.smplibs.listeners.mixins;

import lucas.smplibs.listeners.SystemMessageManager;
import lucas.smplibs.listeners.TickListener;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MinecraftServer.class)
class TickServer {
  @Inject(method = "tickServer", at = @At("HEAD"))
  private void tickServer(CallbackInfo ci) {
    TickListener.trigger();
    SystemMessageManager.tick();
  }
}