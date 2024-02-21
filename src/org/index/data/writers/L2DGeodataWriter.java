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

/**
 * @author Index
 */
public class L2DGeodataWriter extends AbstractGeodataWriter
{
    public L2DGeodataWriter(GeoRegion geoRegion, File path)
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
        writer.writeByte(GeodataBlockTypes.FLAT.getL2dType());
        final GeoMainCell cell = block.getCells()[0][0][0];
        writer.writeShort(cell.getHeight());
        return writer.getWrittenBytes();
    }

    @Override
    protected byte[] storeComplexBlock(GeoBlock block)
    {
        NetworkWriter writer = new NetworkWriter();
        writer.reverseBytes(true);
        writer.writeByte(GeodataBlockTypes.COMPLEX.getL2dType());
        for (int x = 0; x < 8; x++)
        {
            for (int y = 0; y < 8; y++)
            {
                final GeoMainCell cell = block.getCells()[x][y][0];
                writer.writeByte(calculateL2DNswe(block, x, y, 0));
                writer.writeShort(cell.getHeight());
            }
        }
        return writer.getWrittenBytes();
    }

    @Override
    protected byte[] storeMultilayerBlock(GeoBlock block)
    {
        NetworkWriter writer = new NetworkWriter();
        writer.reverseBytes(true);

        writer.writeByte(GeodataBlockTypes.MULTILEVEL.getL2dType());

        for (int x = 0; x < 8; x++)
        {
            for (int y = 0; y < 8; y++)
            {
                int layers = block.getCells()[x][y].length;
                writer.writeByte(layers);
                for (int layer = 0; layer < layers; layer++)
                {
                    final GeoMainCell cell = block.getCells()[x][y][layer];
                    writer.writeByte(calculateL2DNswe(block, x, y, layer));
                    writer.writeShort(cell.getHeight());
                }
            }
        }
        return writer.getWrittenBytes();
    }

    private byte calculateL2DNswe(GeoBlock block, int cellX, int cellY, int layer)
    {
        if (GeodataBlockTypes.FLAT.equals(block.getBlockType()))
        {
            throw new IllegalArgumentException("Flat block not support nswe recalculation!");
        }
        GeoMainCell lookingCell = block.getCells()[cellX][cellY][layer];

        GeoMainCell upperCell = getUpperCell(block, cellX, cellY, layer);
        GeoMainCell  downCell = getLowerCell(block, cellX, cellY, layer);
        GeoMainCell  leftCell = getLeftCell(block, cellX, cellY, layer);
        GeoMainCell rightCell = getRightCell(block, cellX, cellY, layer);

        byte  nswe       = lookingCell.getNswe();
        final byte nsweN =  leftCell == null ? -1 :  leftCell.getNswe();
        final byte nsweS = rightCell == null ? -1 : rightCell.getNswe();
        final byte nsweW =  downCell == null ? -1 :  downCell.getNswe();
        final byte nsweE = upperCell == null ? -1 : upperCell.getNswe();

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

    /**
     * 00  01  02  03  04  05  06  07 <br>
     * 10  11  12  13  14  15  16  17 <br>
     * 20  21  22  23  24  25  26  27 <br>
     * 30  31  32  33  34  35  36  37 <br>
     * 40  41  42  43  44  45  46  47 <br>
     * 50  51  52  53  54  55  56  57 <br>
     * 60  61  62  63  64  65  66  67 <br>
     * 70  71  72  73  74  75  76  77 <br>
     */
    private GeoMainCell getUpperCell(GeoBlock block, int cellX, int cellY, int layer)
    {
        GeoMainCell lookingCell = block.getCells()[cellX][cellY][layer];

        GeoBlock upperBlock;
        if (block.getX() == 0 && cellX == 0)
        {
            return null;
        }
        else if (cellX == 0)
        {
            upperBlock = block.getRegion().getBlocks()[block.getX() - 1][block.getY()];
        }
        else
        {
            upperBlock = block;
        }
        return (cellX == 0) ? upperBlock.getCellForL2D(lookingCell.getHeight(), 7, cellY) :  upperBlock.getCellForL2D(lookingCell.getHeight(), cellX - 1, cellY);
    }

    private GeoMainCell getLowerCell(GeoBlock block, int cellX, int cellY, int layer)
    {
        GeoMainCell lookingCell = block.getCells()[cellX][cellY][layer];

        GeoBlock lowerBlock;
        if (block.getX() == 255 && cellX == 7)
        {
            return null;
        }
        else if (cellX == 7)
        {
            lowerBlock = block.getRegion().getBlocks()[block.getX() + 1][block.getY()];
        }
        else
        {
            lowerBlock = block;
        }
        return (cellX == 7) ? lowerBlock.getCellForL2D(lookingCell.getHeight(), 0, cellY) :  lowerBlock.getCellForL2D(lookingCell.getHeight(), cellX + 1, cellY);
    }

    private GeoMainCell getLeftCell(GeoBlock block, int cellX, int cellY, int layer)
    {
        GeoMainCell lookingCell = block.getCells()[cellX][cellY][layer];

        GeoBlock leftBlock;
        if (block.getY() == 0 && cellY == 0)
        {
            return null;
        }
        else if (cellY == 0)
        {
            leftBlock = block.getRegion().getBlocks()[block.getX()][block.getY() - 1];
        }
        else
        {
            leftBlock = block;
        }
        return (cellY == 0) ? leftBlock.getCellForL2D(lookingCell.getHeight(), cellX, 7) :  leftBlock.getCellForL2D(lookingCell.getHeight(), cellX, cellY - 1);
    }

    private GeoMainCell getRightCell(GeoBlock block, int cellX, int cellY, int layer)
    {
        GeoMainCell lookingCell = block.getCells()[cellX][cellY][layer];

        GeoBlock rightBlock;
        if (block.getY() == 255 && cellY == 7)
        {
            return null;
        }
        else if (cellY == 7)
        {
            rightBlock = block.getRegion().getBlocks()[block.getX()][block.getY() + 1];
        }
        else
        {
            rightBlock = block;
        }
        return (cellY == 7) ? rightBlock.getCellForL2D(lookingCell.getHeight(), cellX, 0) :  rightBlock.getCellForL2D(lookingCell.getHeight(), cellX, cellY + 1);
    }
}
