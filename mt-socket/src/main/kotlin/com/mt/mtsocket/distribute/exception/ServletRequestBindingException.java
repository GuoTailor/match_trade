package com.mt.mtsocket.distribute.exception;

/**
 * Fatal binding exception, thrown when we want to
 * treat binding exceptions as unrecoverable.
 *
 * <p>Extends ServletException for convenient throwing in any Servlet resource
 * (such as a Filter), and NestedServletException for proper root cause handling
 * (as the plain ServletException doesn't expose its root cause at all).
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class ServletRequestBindingException extends Exception {

    /**
     * Constructor for ServletRequestBindingException.
     * @param msg the detail message
     */
    public ServletRequestBindingException(String msg) {
        super(msg);
    }

}
