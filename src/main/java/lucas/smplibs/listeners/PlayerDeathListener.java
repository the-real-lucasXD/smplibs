package lucas.smplibs.listeners;

import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

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
    private static final ArrayList<BiFunction<@NonNull ServerPlayer, @NonNull ServerPlayer, Void>> registered =
      new ArrayList<>();

    public static void register(BiFunction<@NonNull ServerPlayer, @NonNull ServerPlayer, Void> method) {
      registered.add(method);
    }

    static void trigger(ServerPlayer died, ServerPlayer attacker) {
      for (var method : registered) method.apply(died, attacker);
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
    private static final ArrayList<Function<ServerPlayer, Void>> registered =
      new ArrayList<>();

    public static void register(Function<ServerPlayer, Void> method) {
      registered.add(method);
    }

    static void trigger(ServerPlayer died) {
      for (var method : registered) method.apply(died);
    }
  }
}
