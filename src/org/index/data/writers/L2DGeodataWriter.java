package org.index.data.writers;

import org.index.config.configs.MainConfig;
import org.index.enums.GeodataBlockTypes;
import org.index.enums.GeodataCellDirectionFlag;
import org.index.model.GeoMainCell;
import org.index.model.GeoRegion;
import org.index.model.blocks.GeoBlock;
import org.index.utils.NetworkWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Index
 */
@Deprecated(forRemoval = true)
public class L2DGeodataWriter extends AbstractGeodataWriter
{
    private final AtomicInteger counter = new AtomicInteger(0);
    private final static int BUMP_COUNTER = 1406;

    public L2DGeodataWriter(GeoRegion geoRegion, File path)
    {
        // Will update in future, when understand how calculate l2d nswe value.
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
                GeoBlock block = getRegion().getBlocks()[blockX][blockY];
                if (block.getBlockType().equals(GeodataBlockTypes.FLAT))
                {
                    buffer.writeBytes(storeFlatBlock(block));
                }
                if (block.getBlockType().equals(GeodataBlockTypes.COMPLEX))
                {
                    buffer.writeBytes(storeComplexBlock(block));
                }
                if (block.getBlockType().equals(GeodataBlockTypes.MULTILEVEL))
                {
                    buffer.writeBytes(storeMultilayerBlock(block));
                }
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
        writer.writeByte((byte) block.getBlockType().getL2dType());
        if (counter.addAndGet(1) == BUMP_COUNTER)
        {
            System.err.println();
        }
        final GeoMainCell cell = block.getCells()[0][0][0];
        writer.writeShort(GeoMainCell.encodeNsweAndHeightToMask(cell.getHeight(), cell.getNswe()));
        if (counter.addAndGet(1) == BUMP_COUNTER)
        {
            System.err.println();
        }
        if (counter.addAndGet(1) == BUMP_COUNTER)
        {
            System.err.println();
        }
        return writer.getWrittenBytes();
    }

    @Override
    protected byte[] storeComplexBlock(GeoBlock block)
    {
        NetworkWriter writer = new NetworkWriter();
        writer.reverseBytes(true);
        writer.writeByte((byte) block.getBlockType().getL2dType());
        if (counter.addAndGet(1) == BUMP_COUNTER)
        {
            System.err.println();
        }
        for (int x = 0; x < 8; x++)
        {
            for (int y = 0; y < 8; y++)
            {
                final GeoMainCell cell = block.getCells()[x][y][0];
                writer.writeByte(cell.getNswe());
                if (counter.addAndGet(1) == BUMP_COUNTER)
                {
                    System.err.println();
                }
                writer.writeShort(cell.getHeight());
                if (counter.addAndGet(1) == BUMP_COUNTER)
                {
                    System.err.println();
                }
                if (counter.addAndGet(1) == BUMP_COUNTER)
                {
                    System.err.println();
                }
            }
        }
        return writer.getWrittenBytes();
    }

    @Override
    protected byte[] storeMultilayerBlock(GeoBlock block)
    {
        NetworkWriter writer = new NetworkWriter();
        writer.reverseBytes(true);

        writer.writeByte(block.getBlockType().getL2dType());

        if (counter.addAndGet(1) == BUMP_COUNTER)
        {
            System.err.println();
        }
        for (int x = 0; x < 8; x++)
        {
            for (int y = 0; y < 8; y++)
            {
                int layers = block.getCells()[x][y].length;
                writer.writeByte(layers);
                if (counter.addAndGet(1) == BUMP_COUNTER)
                {
                    System.err.println();
                }
                for (int layer = 0; layer < layers; layer++)
                {
                    final GeoMainCell cell = block.getCells()[x][y][layer];
                    writer.writeByte(cell.getNswe());
                    if (counter.addAndGet(1) == BUMP_COUNTER)
                    {
                        System.err.println();
                    }
                    writer.writeShort(cell.getHeight());
                    if (counter.addAndGet(1) == BUMP_COUNTER)
                    {
                        System.err.println();
                    }
                    if (counter.addAndGet(1) == BUMP_COUNTER)
                    {
                        System.err.println();
                    }
                }
            }
        }
        return writer.getWrittenBytes();
    }

    private int getL2dNswe(GeoBlock block, int height, byte nsweValue)
    {
        for (int x = 0; x < 8; )
        if (block.getBlockType().equals(GeodataBlockTypes.FLAT))
        {
            return nsweValue;
        }
        byte nswe = nsweValue;
        height = height; // + 48;

        final byte nsweN = getNsweBelow(block.getX(), block.getY() - 1, height);
        final byte nsweS = getNsweBelow(block.getX(), block.getY() + 1, height);
        final byte nsweW = getNsweBelow(block.getX() - 1, block.getY(), height);
        final byte nsweE = getNsweBelow(block.getX() + 1, block.getY(), height);

        // north-west
        if ((((nswe & GeodataCellDirectionFlag.FLAG_N.getMask()) != 0) && ((nsweN & GeodataCellDirectionFlag.FLAG_W.getMask()) != 0)) || (((nswe & GeodataCellDirectionFlag.FLAG_W.getMask()) != 0) && ((nsweW & GeodataCellDirectionFlag.FLAG_N.getMask()) != 0)))
        {
            nswe |= GeodataCellDirectionFlag.FLAG_NW.getMask();
        }

        // north-east
        if ((((nswe & GeodataCellDirectionFlag.FLAG_N.getMask()) != 0) && ((nsweN & GeodataCellDirectionFlag.FLAG_E.getMask()) != 0)) || (((nswe & GeodataCellDirectionFlag.FLAG_E.getMask()) != 0) && ((nsweE & GeodataCellDirectionFlag.FLAG_N.getMask()) != 0)))
        {
            nswe |= GeodataCellDirectionFlag.FLAG_NE.getMask();
        }

        // south-west
        if ((((nswe & GeodataCellDirectionFlag.FLAG_S.getMask()) != 0) && ((nsweS & GeodataCellDirectionFlag.FLAG_W.getMask()) != 0)) || (((nswe & GeodataCellDirectionFlag.FLAG_W.getMask()) != 0) && ((nsweW & GeodataCellDirectionFlag.FLAG_S.getMask()) != 0)))
        {
            nswe |= GeodataCellDirectionFlag.FLAG_SW.getMask();
        }

        // south-east
        if ((((nswe & GeodataCellDirectionFlag.FLAG_S.getMask()) != 0) && ((nsweS & GeodataCellDirectionFlag.FLAG_E.getMask()) != 0)) || (((nswe & GeodataCellDirectionFlag.FLAG_E.getMask()) != 0) && ((nsweE & GeodataCellDirectionFlag.FLAG_S.getMask()) != 0)))
        {
            nswe |= GeodataCellDirectionFlag.FLAG_SE.getMask();
        }

        return nswe;
    }

    private byte getNsweBelow(int blockX, int blockY, int height)
    {
        final GeoBlock[][] getRegionBlocks = getRegion().getBlocks();
        if (blockX < 0 || blockY < 0 || getRegionBlocks.length <= blockX || getRegionBlocks[blockX] == null || getRegionBlocks[blockX].length <= blockY || getRegionBlocks[blockX][blockY] == null)
        {
            return 0;
        }
        return (byte) getRegion().getBlocks()[blockX][blockY].getL2DNswe(height);
    }
}
