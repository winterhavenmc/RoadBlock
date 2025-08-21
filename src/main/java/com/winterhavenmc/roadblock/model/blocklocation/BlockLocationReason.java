package com.winterhavenmc.roadblock.model.blocklocation;

import com.winterhavenmc.roadblock.util.Reason;

/**
 * The set of possible failure reasons when evaluating a block location.
 * Each enum constant provides a human-readable explanation.
 */
public enum BlockLocationReason implements Reason
{
	LOCATION_NULL("The location was null."),
	WORLD_NULL("The location world was null."),
	WORLD_UNLOADED("The location world was not loaded."),
	WORLD_NAME_NULL("The world name was null."),
	WORLD_UID_NULL("The world UUID was null."),
	WORLD_NAME_BLANK("The world name was blank."),
	;

	private final String defaultMessage; //TODO: these will be message keys for a localized bundle


	/**
	 * Enum constructor assigns message String to constant field
	 *
	 * @param defaultMessage a human-readable error message
	 */
	BlockLocationReason(String defaultMessage)
	{
		this.defaultMessage = defaultMessage;
	}


	/**
	 * Gets a human-readable explanation for this failure.
	 *
	 * @return the failure description
	 */
	public String getDefaultMessage()
	{
		return defaultMessage;
	}
}
