package lucas.smplibs.listeners.mixins;

import lucas.smplibs.listeners.ServerStartedListener;
import lucas.smplibs.listeners.ServerStartingListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedServer.class)
class ServerStarted {
  @Inject(method = "initServer", at = @At("HEAD"))
  private void initHead(CallbackInfoReturnable<Boolean> cir) {
    MinecraftServer server = (MinecraftServer) (Object) this;
    ServerStartingListener.trigger(server);
  }

  @Inject(method = "initServer", at = @At("RETURN"))
  private void initReturn(CallbackInfoReturnable<Boolean> cir) {
    if (!cir.getReturnValue()) return;
    MinecraftServer server = (MinecraftServer) (Object) this;
    ServerStartedListener.trigger(server);
  }
}
