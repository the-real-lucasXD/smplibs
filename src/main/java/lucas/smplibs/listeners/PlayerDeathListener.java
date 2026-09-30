package lucas.smplibs.listeners;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Contains the listener classes to register events to execute when players die, reducing the need for mixins.
 *
 * @since 1.1.0
 */
public final class PlayerDeathListener {
  /**
   * The listener class that triggers whenever a player dies to another player. Register the method with a code like this:
   * <blockquote><pre>
   *   PlayerDeathListener.ByPlayer.register((died, killer) -> {
   *     // your logic here
   *   })
   * </pre></blockquote>
   */
  public static final class ByPlayer {
    private static final ArrayList<BiConsumer<@NonNull ServerPlayer, @NonNull ServerPlayer>> registered =
      new ArrayList<>();

    public static void register(BiConsumer<@NonNull ServerPlayer, @NonNull ServerPlayer> method) {
      registered.add(method);
    }

    @ApiStatus.Internal
    public static void trigger(ServerPlayer died, ServerPlayer attacker) {
      for (var method : registered) method.accept(died, attacker);
    }
  }

  /**
   * The listener class that triggers whenever a player dies from any source, including by other players.
   * Register the method with a code like this:
   * <blockquote><pre>
   *   PlayerDeathListener.Any.register((died) -> {
   *     // your logic here
   *   })
   * </pre></blockquote>
   */
  public static class Any {
    private static final ArrayList<Consumer<ServerPlayer>> registered =
      new ArrayList<>();

    public static void register(Consumer<ServerPlayer> method) {
      registered.add(method);
    }

    @ApiStatus.Internal
    public static void trigger(ServerPlayer died) {
      for (var method : registered) method.accept(died);
    }
  }
}
