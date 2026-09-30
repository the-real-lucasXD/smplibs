package lucas.smplibs.listeners.mixins;

import lucas.smplibs.listeners.PlayerDeathListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
class PlayerDied {
  @Inject(method = "die", at = @At("RETURN"))
  private void die(DamageSource source, CallbackInfo ci) {
    ServerPlayer died = (ServerPlayer) (Object) this;
    if (source.getEntity() instanceof ServerPlayer attacked) PlayerDeathListener.ByPlayer.trigger(died, attacked);
    PlayerDeathListener.Any.trigger(died);
  }
}
