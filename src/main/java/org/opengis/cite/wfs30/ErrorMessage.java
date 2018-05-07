package org.opengis.cite.wfs30;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Utility class for retrieving and formatting localized error messages that
 * describe failed assertions.
 */
public class ErrorMessage {

    private static final String BASE_NAME =
            "org.opengis.cite.wfs30.MessageBundle";
    private static ResourceBundle msgResources =
            ResourceBundle.getBundle(BASE_NAME);

    /**
     * Produces a formatted error message using the supplied substitution
     * arguments and the current locale. The arguments should reflect the order
     * of the placeholders in the message template.
     * 
     * @param msgKey
     *            The key identifying the message template; it should be a
     *            member of {@code ErrorMessageKeys}.
     * @param args
     *            An array of arguments to be formatted and substituted in the
     *            content of the message.
     * @return A String containing the message content. If no message is found
     *         for the given key, a {@link java.util.MissingResourceException}
     *         is thrown.
     */		
    public static String format(String msgKey, Object... args) {
        return MessageFormat.format(msgResources.getString(msgKey), args);
    }

    /**
     * Retrieves a simple message according to the current locale.
     * 
     * @param msgKey
     *            The key identifying the message; it should be a member of
     *            {@code ErrorMessageKeys}.
     * @return A String containing the message content. If no message is found
     *         for the given key, a {@link java.util.MissingResourceException}
     *         is thrown.
     */
    public static String get(String msgKey) {
        return msgResources.getString(msgKey);
    }
}
