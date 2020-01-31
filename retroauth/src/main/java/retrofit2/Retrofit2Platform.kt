/*
 * Copyright (c) 2016 Andre Tietz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package retrofit2

import retrofit2.CallAdapter.Factory
import java.util.concurrent.Executor

/**
 * This helper gets some os dependent information from retrofit.
 */
internal object Retrofit2Platform {
  @JvmStatic
  fun defaultCallAdapterFactories(executor: Executor?): List<Factory> {
    executor?.let {
      return Platform.get().defaultCallAdapterFactories(executor)
    }
    return Platform.get().defaultCallAdapterFactories(Platform.get().defaultCallbackExecutor())
  }
}
