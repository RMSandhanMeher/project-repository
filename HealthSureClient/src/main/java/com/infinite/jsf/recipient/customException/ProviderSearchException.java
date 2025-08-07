package com.infinite.jsf.recipient.customException;

@SuppressWarnings("serial")
public class ProviderSearchException extends RuntimeException{
	
	
	/**
     * Constructs a new exception with the specified detail message.
     * Use this for simple, known errors without an underlying cause.
     * @param message The detail message(for the user/UI).
     * Suitable for User
     */
	public ProviderSearchException(String message) {
		super(message);
	}
	
	
	
	/**
     * Constructs a new exception with the specified cause.
     * Use this when the original cause's message is sufficient, and you only
     * need to wrap it in this custom exception type.
     * @param cause The original, underlying technical cause.
     * Suitable for Developer
     */
	public ProviderSearchException(Throwable cause) {
		super(cause);
	}
	
	
	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * This is the most common constructor for a custom wrapper exception.
	 * Use this when catching a low-level exception (e.g., HibernateException)
	 * and re-throwing it with a more meaningful, user-friendly message.
	 * @param message The detail message (for the user/UI).
	 * @param cause   The original, underlying technical cause.
	 * Suitable for both User and Developer
	 */
	public ProviderSearchException(String message, Throwable cause) {
		super(message,cause);
	}

}