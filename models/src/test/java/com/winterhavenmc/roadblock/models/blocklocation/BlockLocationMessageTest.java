package com.winterhavenmc.roadblock.models.blocklocation;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;


class BlockLocationMessageTest
{

	@Test
	void getLocalizedMessage_with_US_Locale()
	{
		// Act
		String result = BlockLocationMessage.LOCATION_NULL.getLocalizedMessage(Locale.US);

		// Assert
		assertEquals("The location was null.", result);
	}


	@Test
	void getLocalizedMessage_with_GERMAN_Locale()
	{
		// Act
		String result = BlockLocationMessage.LOCATION_NULL.getLocalizedMessage(Locale.GERMAN);

		// Assert
		assertEquals("Der Standort war null.", result);
	}


	@Test
	void getLocalizedMessage_with_null_Locale()
	{
		// Act
		String result = BlockLocationMessage.LOCATION_NULL.getLocalizedMessage(null);

		// Assert
		assertEquals("The location was null.", result);
	}


	@Test
	void getLocalizedMessage_with_missing_resource_Locale()
	{
		// Act
		String result = BlockLocationMessage.LOCATION_NULL.getLocalizedMessage(Locale.forLanguageTag("ru-RU"));

		// Assert
		assertEquals("The location was null.", result);
	}

}
