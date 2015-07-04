package eu.unicate.retroauth;

/**
 * This enum defines the different request types
 * handeled by retroauth
 */
enum AuthRequestType {
	// rxjava calls
	RXJAVA,
	// asynchronous calls using the retrofit Callback interface
	ASYNC,
	// synchronous calls returning the result object
	SYNC,
	// non, means that the original retrofit request will be executed
	NONE
}
