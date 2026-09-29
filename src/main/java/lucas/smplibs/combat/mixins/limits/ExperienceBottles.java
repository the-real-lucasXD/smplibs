package lucas.smplibs.combat.mixins.limits;

import lucas.smplibs.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static lucas.smplibs.config.Config.configs;

@Mixin(ExperienceBottleItem.class)
class ExperienceBottles {
  @Inject(method = "use", at = @At("HEAD"), cancellable = true)
  private void place(Level level, net.minecraft.world.entity.player.Player player,
                     InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
    if (player instanceof ServerPlayer serverPlayer) {
      Player customPlayer = Player.get(serverPlayer);
      if (customPlayer.inCombat()) {
        if (configs().combat.limits.xp == 0) serverPlayer.sendSystemMessage(Component.literal(
          "Experience bottles are disabled!"
        ).withColor(TextColor.RED), true);
        else if (customPlayer.combat().limitManager.xpUsed >= configs().combat.limits.xp
          && configs().combat.limits.xp != -1
        ) {
          cir.setReturnValue(InteractionResult.FAIL);
          serverPlayer.sendSystemMessage(Component.literal(
            "You have already used " + configs().combat.limits.xp + " experience bottles!"
          ).withColor(TextColor.RED), true);
          customPlayer.inventoryRefresh();
        } else {
          customPlayer.combat().limitManager.xpUsed ++;
          if (configs().combat.limits.xp != -1) serverPlayer.sendSystemMessage(Component.literal(String.format(
            "Experience bottles used: %d/%d", customPlayer.combat().limitManager.xpUsed, configs().combat.limits.xp
          )), true);
        }
      }
    }
  }
}