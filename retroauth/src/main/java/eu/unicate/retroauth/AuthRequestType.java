package eu.unicate.retroauth;

/**
 * This is a small class of method information, so that
 * reflection is used only while creating the service, not each time
 * it is called
 *
 * Right now it's pretty small, but it may grow
 */
public enum AuthRequestType {
	RXJAVA,
	ASYNC,
	SYNC,
	NONE
}
