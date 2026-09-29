package lucas.smplibs.combat.mixins.limits;

import lucas.smplibs.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static lucas.smplibs.config.Config.configs;

@Mixin(Consumable.class)
class Egaps {
  @Inject(method = "canConsume", at = @At("HEAD"), cancellable = true)
  private void canConsume(LivingEntity user, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
    if (user instanceof ServerPlayer serverPlayer) {
      if (stack.getItem() != Items.ENCHANTED_GOLDEN_APPLE) return;
      Player customPlayer = Player.get(serverPlayer);
      if (customPlayer.inCombat()) {
        if (configs().combat.limits.egap == 0) serverPlayer.sendSystemMessage(Component.literal(
          "Enchanted golden apples are disabled!"
        ).withColor(TextColor.RED), true);
        else if (customPlayer.combat().limitManager.egapsUsed >= configs().combat.limits.egap
          && configs().combat.limits.egap != -1
        ) {
          serverPlayer.sendSystemMessage(Component.literal(
            "You have already used " + configs().combat.limits.egap + " enchanted golden apples!"
          ).withColor(TextColor.RED), true);
          cir.setReturnValue(false);
        }
      }
    }
  }
  
  @Inject(method = "onConsume", at = @At("HEAD"))
  private void onConsume(Level level, LivingEntity user, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
    if (!stack.is(Items.ENCHANTED_GOLDEN_APPLE)) return;
    if (user instanceof ServerPlayer serverPlayer) {
      Player customPlayer = Player.get(serverPlayer);
      if (customPlayer.inCombat()) {
        customPlayer.combat().limitManager.egapsUsed++;
        if (configs().combat.limits.egap != -1) serverPlayer.sendSystemMessage(Component.literal(String.format(
          "Enchanted golden apples used: %d/%d", customPlayer.combat().limitManager.egapsUsed, configs().combat.limits.egap
        )), true);
      }
    }
  }
}
