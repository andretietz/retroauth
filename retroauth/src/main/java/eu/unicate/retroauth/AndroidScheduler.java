/*
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

package eu.unicate.retroauth;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.TimeUnit;

import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.internal.schedulers.ScheduledAction;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

/**
 * This is a Scheduler how it is implemented in rxandroid 0.24.0
 * since I did not want to have the whole library as dependency
 * <p/>
 * The original rxandroid project can be found <a href="https://github.com/ReactiveX/RxAndroid">here</a>.
 */
final class AndroidScheduler extends Scheduler {

	private static final Scheduler MAIN_THREAD_SCHEDULER =
			new AndroidScheduler(new Handler(Looper.getMainLooper()));
	private final Handler handler;

	private AndroidScheduler(Handler handler) {
		this.handler = handler;
	}

	/**
	 * Scheduler which will execute actions on the Android UI thread.
	 */
	public static Scheduler mainThread() {
		return MAIN_THREAD_SCHEDULER;
	}

	@Override
	public Worker createWorker() {
		return new InnerHandlerThreadScheduler(handler);
	}

	private static class InnerHandlerThreadScheduler extends Worker {

		private final Handler handler;

		private final CompositeSubscription compositeSubscription = new CompositeSubscription();

		public InnerHandlerThreadScheduler(Handler handler) {
			this.handler = handler;
		}

		@Override
		public void unsubscribe() {
			compositeSubscription.unsubscribe();
		}

		@Override
		public boolean isUnsubscribed() {
			return compositeSubscription.isUnsubscribed();
		}

		@Override
		public Subscription schedule(final Action0 action, long delayTime, TimeUnit unit) {
			final ScheduledAction scheduledAction = new ScheduledAction(action);
			scheduledAction.add(Subscriptions.create(new Action0() {
				@Override
				public void call() {
					handler.removeCallbacks(scheduledAction);
				}
			}));
			scheduledAction.addParent(compositeSubscription);
			compositeSubscription.add(scheduledAction);

			handler.postDelayed(scheduledAction, unit.toMillis(delayTime));

			return scheduledAction;
		}

		@Override
		public Subscription schedule(final Action0 action) {
			return schedule(action, 0, TimeUnit.MILLISECONDS);
		}

	}
}