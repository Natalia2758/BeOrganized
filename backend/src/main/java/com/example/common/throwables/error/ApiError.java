package com.example.common.throwables.error;

/**
 * 
 * <b>ApiError</b><br>
 * 
 * A record used to transfer error data to the client side of the application.
 * 
 * @param code      code specific to the type of an error
 * @param message   message with an error details
 */
public record ApiError(
    String code,
    String message
) {

}
