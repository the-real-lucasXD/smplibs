package lucas.smplibs.combat.mixins.limits;

import lucas.smplibs.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static lucas.smplibs.config.Config.configs;

@Mixin(EnderpearlItem.class)
class Pearls {
  @Inject(method = "use", at = @At("HEAD"), cancellable = true)
  private void pearl(Level level, net.minecraft.world.entity.player.Player player,
                               InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
    if (player instanceof ServerPlayer serverPlayer) {
      Player customPlayer = Player.get(serverPlayer);
      if (customPlayer.inCombat()) {
        if (configs().combat.limits.pearl == 0) serverPlayer.sendSystemMessage(Component.literal(
          "Pearls are disabled!"
        ).withColor(TextColor.RED), true);
        else if (customPlayer.combat().limitManager.pearlsUsed >= configs().combat.limits.pearl
          && configs().combat.limits.pearl != -1
        ) {
          serverPlayer.sendSystemMessage(Component.literal(
            "You have already used " + configs().combat.limits.pearl + " pearls!"
          ).withColor(TextColor.RED), true);
          cir.setReturnValue(InteractionResult.FAIL);
          customPlayer.inventoryRefresh();
        }
      }
    }
  }
  
  @Inject(method = "use", at = @At("RETURN"), cancellable = true)
  private void use(Level level, net.minecraft.world.entity.player.Player player,
                   InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
    if (player instanceof ServerPlayer serverPlayer) {
      Player customPlayer = Player.get(serverPlayer);
      if (customPlayer.inCombat()) {
        if (player.getCooldowns().isOnCooldown(Items.ENDER_PEARL.getDefaultInstance())) {
          cir.setReturnValue(InteractionResult.FAIL);
          return;
        } customPlayer.combat().limitManager.pearlsUsed++;
        if (configs().combat.limits.pearl != -1) serverPlayer.sendSystemMessage(Component.literal(String.format(
          "Pearls used: %d/%d", customPlayer.combat().limitManager.pearlsUsed, configs().combat.limits.pearl
        )), true);
      }
    }
  }
}
