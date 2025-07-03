package com.winterhavenmc.roadblock.block_location;

import java.util.UUID;

public record ValidBlockLocation(String worldName,
                                 UUID worldUid,
                                 int blockX,
                                 int blockY,
                                 int blockZ,
                                 int chunkX,
                                 int chunkZ) implements BlockLocation
{ }
