package com.winterhavenmc.roadblock.model.roadblock;

import com.winterhavenmc.roadblock.util.Reason;

/**
 * The set of possible failure reasons when evaluating a block location.
 * Each enum constant provides a human-readable explanation.
 */
enum RoadBlockReason implements Reason
{
	WORLD_NULL("The location world was null."),
	BLOCK_MATERIAL("The block is not a RoadBlock material."),
	;

	private final String message; //TODO: these will be message keys for a localized bundle


	/**
	 * Enum constructor assigns message String to constant field
	 *
	 * @param message a human-readable error message
	 */
	RoadBlockReason(String message)
	{
		this.message = message;
	}


	/**
	 * Gets a human-readable explanation for this failure.
	 *
	 * @return the failure description
	 */
	public String getMessage()
	{
		return message;
	}
}
