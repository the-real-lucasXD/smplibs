package lucas.smplibs.listeners;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;

/**
 * Contains the listener class to register events to execute when the server is stopping. Register the method with a code
 * like this:
 *
 * <blockquote><pre>
 *   ServerStoppingListener.register(() -> {
 *     // your logic here
 *   })
 * </pre></blockquote>
 * @since 1.1.1
 */
public final class ServerStoppingListener {
  private static final ArrayList<Runnable> registered = new ArrayList<>();

  public static void register(Runnable method) {
    registered.add(method);
  }

  @ApiStatus.Internal
  public static void trigger() {
    for (var method : registered) method.run();
  }
}
