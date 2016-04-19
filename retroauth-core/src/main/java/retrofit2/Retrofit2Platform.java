package retrofit2;

import java.util.concurrent.Executor;

import retrofit2.CallAdapter.Factory;

/**
 * This helper gets some os dependent information from retrofit.
 */
public final class Retrofit2Platform {

    private Retrofit2Platform() {
    }

    public static Factory defaultCallAdapterFactory(Executor executor) {
        if (executor == null) {
            executor = defaultCallbackExecutor();
        }
        return Platform.get().defaultCallAdapterFactory(executor);
    }

    public static Executor defaultCallbackExecutor() {
        return Platform.get().defaultCallbackExecutor();
    }
}
