package com.winterhavenmc.roadblock.models.blocklocation;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The set of possible failure reasons when evaluating a block location.
 * Each enum constant provides a human-readable explanation.
 */
public enum BlockLocationMessage
{
	LOCATION_NULL("The location was null."),
	WORLD_NULL("The location world was null."),
	WORLD_UNLOADED("The location world was not loaded."),
	WORLD_NAME_NULL("The world name was null."),
	WORLD_UID_NULL("The world UUID was null."),
	WORLD_NAME_BLANK("The world name was blank."),
	;

	private final String defaultMessage;


	/**
	 * Enum constructor assigns message String to constant field
	 *
	 * @param defaultMessage a human-readable error message
	 */
	BlockLocationMessage(String defaultMessage)
	{
		this.defaultMessage = defaultMessage;
	}


	/**
	 * Gets a localized, human-readable explanation for this failure.
	 *
	 * @return the failure description
	 */
	public String getLocalizedMessage(final Locale passedLocale)
	{
		Locale locale = (passedLocale != null)
				? passedLocale
				: Locale.US;

		try
		{
			ResourceBundle bundle = ResourceBundle.getBundle(getClass().getSimpleName(), locale);
			return bundle.getString(name());
		}
		catch (MissingResourceException exception)
		{
			return this.defaultMessage;
		}
	}
}
