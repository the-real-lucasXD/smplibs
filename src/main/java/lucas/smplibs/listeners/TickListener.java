package lucas.smplibs.listeners;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;

/**
 * Contains the listener class to register events to execute during each tick, reducing the need for mixins.
 * Register the method with a code like this:
 *
 * <blockquote><pre>
 *   TickListener.register(() -> {
 *     // your logic here
 *   })
 * </pre></blockquote>
 * @since 1.1.1
 */
public final class TickListener {
  private static final ArrayList<Runnable> registered = new ArrayList<>();

  public static void register(Runnable method) {
    registered.add(method);
  }

  @ApiStatus.Internal
  public static void trigger() {
    for (var method : registered) method.run();
  }
}
