package eu.unicate.retroauth;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		AuthAccountManagerTests.class,
		AuthInvokerTests.class,
		AuthRestAdapterTests.class,
		LockingStrategyTests.class
})
public class AllTests {
}
