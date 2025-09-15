package com.winterhavenmc.roadblock.models.blocklocation;

import org.bukkit.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class BlockLocationTest
{
	@Mock Server serverMock;
	@Mock World worldMock;
	@Mock Chunk chunkMock;
	@Mock Location locationMock;


	@Test
	void of_with_valid_location_parameter_returns_ValidBlockLocation()
	{
		// Arrange
		try (MockedStatic<Bukkit> staticBukkitMock = Mockito.mockStatic(Bukkit.class))
		{
			staticBukkitMock.when(Bukkit::getServer).thenReturn(serverMock);
			when(locationMock.isWorldLoaded()).thenReturn(true);
			when(locationMock.getWorld()).thenReturn(worldMock);
			when(locationMock.getChunk()).thenReturn(chunkMock);

			// Act
			BlockLocation result = BlockLocation.of(locationMock);

			// Assert
			assertInstanceOf(BlockLocation.Valid.class, result);

			// Verify
			verify(locationMock, atLeastOnce()).isWorldLoaded();
			verify(locationMock, atLeastOnce()).getWorld();
			verify(locationMock, atLeastOnce()).getChunk();
		}
	}


	@Test
	void of_with_null_location_parameter_returns_InvalidBlockLocation()
	{
		// Arrange
		try (MockedStatic<Bukkit> staticBukkitMock = Mockito.mockStatic(Bukkit.class))
		{
			staticBukkitMock.when(Bukkit::getServer).thenReturn(serverMock);

			// Act
			BlockLocation result = BlockLocation.of(null);

			// Assert
			assertInstanceOf(BlockLocation.Invalid.class, result);
			assertEquals(BlockLocationMessage.LOCATION_NULL, ((BlockLocation.Invalid) result).reason());
		}
	}


	@Test
	void of_with_unloaded_word_location_parameter_returns_InvalidBlockLocation()
	{
		// Arrange
		try (MockedStatic<Bukkit> staticBukkitMock = Mockito.mockStatic(Bukkit.class))
		{
			staticBukkitMock.when(Bukkit::getServer).thenReturn(serverMock);
			when(locationMock.getWorld()).thenReturn(worldMock);
			when(locationMock.isWorldLoaded()).thenReturn(false);

			// Act
			BlockLocation result = BlockLocation.of(locationMock);

			// Assert
			assertInstanceOf(BlockLocation.Invalid.class, result);
			assertEquals(BlockLocationMessage.WORLD_UNLOADED, ((BlockLocation.Invalid) result).reason());

			// Verify
			verify(locationMock, atLeastOnce()).isWorldLoaded();
			verify(locationMock, atLeastOnce()).getWorld();
		}
	}


	@Test
	void of_with_null_word_location_parameter_returns_InvalidBlockLocation()
	{
		// Arrange
		try (MockedStatic<Bukkit> staticBukkitMock = Mockito.mockStatic(Bukkit.class))
		{
			staticBukkitMock.when(Bukkit::getServer).thenReturn(serverMock);
			when(locationMock.getWorld()).thenReturn(null);

			// Act
			BlockLocation result = BlockLocation.of(locationMock);

			// Assert
			assertInstanceOf(BlockLocation.Invalid.class, result);
			assertEquals(BlockLocationMessage.WORLD_NULL, ((BlockLocation.Invalid) result).reason());

			// Verify
			verify(locationMock, atLeastOnce()).getWorld();
		}
	}


	@Test
	void of_with_valid_record_parameters_returns_ValidBlockLocation()
	{
		// Arrange
		UUID worldUid = new UUID(42, 42);

		// Act
		BlockLocation result = BlockLocation.of("worldName", worldUid, 1, 2, 3, 4, 5);

		// Assert
		assertInstanceOf(BlockLocation.Valid.class, result);
	}


	@Test
	void of_with_null_worldName_returns_InvalidBlockLocation()
	{
		// Arrange
		UUID worldUid = new UUID(42, 42);

		// Act
		BlockLocation result = BlockLocation.of(null, worldUid, 1, 2, 3, 4, 5);

		// Assert
		assertInstanceOf(BlockLocation.Invalid.class, result);
		assertEquals(BlockLocationMessage.WORLD_NAME_NULL, ((BlockLocation.Invalid) result).reason());
	}


	@Test
	void of_with_blank_worldName_returns_InvalidBlockLocation()
	{
		// Arrange
		UUID worldUid = new UUID(42, 42);

		// Act
		BlockLocation result = BlockLocation.of("", worldUid, 1, 2, 3, 4, 5);

		// Assert
		assertInstanceOf(BlockLocation.Invalid.class, result);
		assertEquals(BlockLocationMessage.WORLD_NAME_BLANK, ((BlockLocation.Invalid) result).reason());
	}


	@Test
	void of_with_null_worldUid_returns_InvalidBlockLocation()
	{
		// Arrange
		UUID worldUid = new UUID(42, 42);

		// Act
		BlockLocation result = BlockLocation.of("worldName", null, 1, 2, 3, 4, 5);

		// Assert
		assertInstanceOf(BlockLocation.Invalid.class, result);
		assertEquals(BlockLocationMessage.WORLD_UID_NULL, ((BlockLocation.Invalid) result).reason());
	}

}
