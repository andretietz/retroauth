package eu.unicate.retroauth;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * TODO
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
		AuthAccountManagerTests.class,
		AuthInvokerTests.class,
		AuthRestAdapterTests.class,
		AuthRestHandlerTests.class
})
public class AllTests {
}
