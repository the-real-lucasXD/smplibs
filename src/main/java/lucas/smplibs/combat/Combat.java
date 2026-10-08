package lucas.smplibs.combat;

import lucas.smplibs.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

import static lucas.smplibs.config.Config.configs;
import static lucas.smplibs.player.Player.players;

/**
 * The {@code Combat} class represents the object instance and manager for each combat, which manages resetting, GUI, limits
 * and bans.
 *
 * @since 1.1.0
 */
public final class Combat {
  public @NonNull CombatLimitManager limitManager = new CombatLimitManager();
  public @NonNull ServerBossEvent bossBar;
  private final Player player;
  private int ticksLeft;
  public Player attacker;
  public boolean combatLogged = false;

  public void reset(@Nullable Player attacker) {
    this.attacker = attacker;
    ticksLeft = configs().combat.duration+1;
    bossBar.setProgress(1);
    tick();
  }

  public void tick() {
    ticksLeft--;
    bossBar.setProgress((float) ticksLeft / configs().combat.duration);
    bossBar.setName(Component.literal(String.format("Combat: %.1fs", (double) ticksLeft / 20)));
    if (ticksLeft <= 0) player.clearCombat();
  }

  public Combat(@NonNull Player player, @Nullable Player attacker) {
    this.player = player;
    ticksLeft = configs().combat.duration;
    bossBar = new ServerBossEvent(
      UUID.randomUUID(),
      Component.literal(String.format("Combat: %.1fs", (double) ticksLeft / 20)),
      BossEvent.BossBarColor.RED,
      BossEvent.BossBarOverlay.PROGRESS
    ); bossBar.setProgress(1);
    bossBar.addPlayer(Objects.requireNonNull(player.serverPlayer()));
    reset(attacker);
  }

  public static void clearAll() {
    for (Player i: players().values()) {
      if (i.online && i.inCombat()) i.clearCombat();
    }
  }
}
