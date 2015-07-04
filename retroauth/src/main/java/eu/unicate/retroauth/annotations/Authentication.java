package eu.unicate.retroauth.annotations;

import android.support.annotation.StringRes;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation sets up the account type and the token type of the authenticated
 * requests in the interface itself
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface Authentication {
	/**
	 * @return a Resource String of the Account Type
	 */
	@StringRes int accountType();

	/**
	 * @return a Resource String of the Token Type
	 */
	@StringRes int tokenType();
}
