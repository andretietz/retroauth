package retrofit2;

import java.util.concurrent.Executor;

import retrofit2.CallAdapter.Factory;

/**
 * Created by andre.tietz on 30/03/16.
 */
public final class Retrofit2Platform {

    private Retrofit2Platform() {
    }

    public static Factory defaultCallAdapterFactory(Executor executor) {
        if(executor == null) {
            executor = defaultCallbackExecutor();
        }
        return Platform.get().defaultCallAdapterFactory(executor);
    }

    private static Executor defaultCallbackExecutor() {
        return Platform.get().defaultCallbackExecutor();
    }
}
