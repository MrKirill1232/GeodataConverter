package org.index.data.writers;

import org.index.config.configs.MainConfig;
import org.index.enums.GeodataBlockTypes;
import org.index.model.GeoMainCell;
import org.index.model.GeoRegion;
import org.index.model.blocks.GeoBlock;
import org.index.utils.NetworkWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Index
 */
public class L2JGeodataWriter extends AbstractGeodataWriter
{
    public L2JGeodataWriter(GeoRegion geoRegion, File path)
    {
        super(geoRegion, path);
    }

    @Override
    public void write()
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        for (int blockX = 0; blockX < MainConfig.GEO_REGION_SIZE; blockX++)
        {
            for (int blockY = 0; blockY < MainConfig.GEO_REGION_SIZE; blockY++)
            {
                byte[] blockAsByte = null;
                GeoBlock block = getRegion().getBlocks()[blockX][blockY];
                if (block.getBlockType().equals(GeodataBlockTypes.FLAT))
                {
                    blockAsByte = storeFlatBlock(block);
                }
                if (block.getBlockType().equals(GeodataBlockTypes.COMPLEX))
                {
                    blockAsByte = storeComplexBlock(block);
                }
                if (block.getBlockType().equals(GeodataBlockTypes.MULTILEVEL))
                {
                    blockAsByte = storeMultilayerBlock(block);
                }
                buffer.write(blockAsByte, 0, blockAsByte.length);
            }
        }

        try
        {
            Files.write(_pathToFile.toPath(), buffer.toByteArray());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected byte[] storeFlatBlock(GeoBlock block)
    {
        NetworkWriter writer = new NetworkWriter();
        writer.reverseBytes(true);
        writer.writeByte((byte) block.getBlockType().ordinal());
        final GeoMainCell cell = block.getCells()[0][0][0];
        writer.writeShort(cell.getHeightMask(cell.getHeight()));
        return writer.getWrittenBytes();
    }

    @Override
    protected byte[] storeComplexBlock(GeoBlock block)
    {
        NetworkWriter writer = new NetworkWriter();
        writer.reverseBytes(true);
        writer.writeByte(block.getBlockType().ordinal());
        for (int x = 0; x < 8; x++)
        {
            for (int y = 0; y < 8; y++)
            {
                final GeoMainCell cell = block.getCells()[x][y][0];
                writer.writeShort(cell.getHeightMask(cell.getHeight()));
            }
        }
        return writer.getWrittenBytes();
    }

    @Override
    protected byte[] storeMultilayerBlock(GeoBlock block)
    {
        NetworkWriter writer = new NetworkWriter();
        writer.reverseBytes(true);

        writer.writeByte(block.getBlockType().ordinal());

        for (int x = 0; x < 8; x++)
        {
            for (int y = 0; y < 8; y++)
            {
                int layers = block.getCells()[x][y].length;
                writer.writeByte(layers);
                for (int layer = 0; layer < layers; layer++)
                {
                    final GeoMainCell cell = block.getCells()[x][y][layer];
                    writer.writeShort(cell.getHeightMask(cell.getHeight()));
                }
            }
        }
        return writer.getWrittenBytes();
    }
}
