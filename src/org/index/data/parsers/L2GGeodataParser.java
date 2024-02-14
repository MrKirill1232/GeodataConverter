package org.index.data.parsers;

import org.index.config.ConfigParser;
import org.index.config.configs.MainConfig;
import org.index.enums.GeodataBlockTypes;
import org.index.enums.GeodataCellDirectionFlag;
import org.index.enums.GeodataExtensions;
import org.index.model.GeoRegion;
import org.index.model.blocks.GeoBlock;
import org.index.model.blocks.GeoBlockComplex;
import org.index.model.blocks.GeoBlockFlat;
import org.index.model.blocks.GeoBlockMultiLevel;
import org.index.model.GeoMainCell;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Index
 */
public class L2GGeodataParser extends AbstractGeodataParser
{
    public final static int CHECKSUMM = 0x814141AB;
    private int _checksumm;

    public L2GGeodataParser(File pathToFile)
    {
        super(GeodataExtensions.L2G, pathToFile);
        // first 4 bytes - xor key
        _pos.set(4);
    }

    @Override
    public boolean checkGeodataCrypt()
    {
        int value = getBuffer(getFileAsByteArray(), Integer.BYTES, 0, false).getInt();
        _checksumm = (CHECKSUMM ^ value);
        if (_checksumm != 0)
        {
            byte key = (byte)((_checksumm >> 24 & 0xFF) ^ (_checksumm >> 16 & 0xFF) ^ (_checksumm >> 8 & 0xFF) ^ (_checksumm >> 0 & 0xFF));
            ByteBuffer buffer = ByteBuffer.wrap(getFileAsByteArray(), 4, getFileAsByteArray().length - 4);
            while (buffer.hasRemaining())
            {
                buffer.put(buffer.position(), (byte) (buffer.get() ^ key));
                _checksumm -= (key = buffer.get(buffer.position() - 1));
            }
            buffer.rewind();
            updateFileByteArray(buffer.array());
        }
        return true;
    }

    @Override
    public GeoRegion read()
    {
        final GeoRegion geoRegion = new GeoRegion(getXYcord()[0], getXYcord()[1]);
        readBlocks(geoRegion);
        return geoRegion;
    }

    @Override
    protected void readBlocks(GeoRegion geoRegion)
    {
        for (int blockX = 0; blockX < MainConfig.GEO_REGION_SIZE; blockX++)
        {
            for (int blockY = 0; blockY < MainConfig.GEO_REGION_SIZE; blockY++)
            {
                int type = getBuffer(getFileAsByteArray(), Byte.BYTES, _pos.getAndAdd(Byte.BYTES), true).get();
                GeodataBlockTypes geodataBlockType = GeodataBlockTypes.getType(getSelectedType(), type);
                if (geodataBlockType == null)
                {
                    System.err.println(Arrays.toString(getXYcord()) + " unk block type " + type + ";");
                    continue;
                }
                final GeoBlock block;
                switch (geodataBlockType)
                {
                    case FLAT:
                    {
                        block = readFlatData(geoRegion);
                        break;
                    }
                    case COMPLEX:
                    {
                        block = readComplexData(geoRegion);
                        break;
                    }
                    case MULTILEVEL:
                    {
                        block = readMultilevelData(geoRegion);
                        break;
                    }
                    default:
                    {
                        continue;
                    }
                }
                block.setXY(blockX, blockY);
                geoRegion.addBlock(blockX, blockY, block);
            }
        }
    }

    @Override
    protected GeoBlock readFlatData(GeoRegion geoRegion, int... args)
    {
        short height = getBuffer(getFileAsByteArray(), Short.BYTES, _pos.getAndAdd(Short.BYTES), true).getShort();
        final GeoBlockFlat block = new GeoBlockFlat(geoRegion);

        final GeoMainCell cell = new GeoMainCell(block, 0, 0, 1);
        cell.setHeight(height);
        cell.setNswe(GeodataCellDirectionFlag.NSWE_ALL.getMask());

        block.addCell(cell);
        return block;
    }

    @Override
    protected GeoBlock readComplexData(GeoRegion geoRegion, int... args)
    {
        final GeoBlockComplex block = new GeoBlockComplex(geoRegion);
        block.extendCells(8, 8);
        for (int x = 0; x < 8; x++)
        {
            for (int y = 0; y < 8; y++)
            {
                block.extendLayers(x, y, 1);

                final short height = getBuffer(getFileAsByteArray(), Short.BYTES, _pos.getAndAdd(Short.BYTES), true).getShort();

                final GeoMainCell cell = new GeoMainCell(block, x, y, 1);
                cell.setHeight(GeoMainCell.decodeHeight(height));
                cell.setNswe(GeoMainCell.decodeNswe(height));

                block.addCell(cell);
            }
        }
        return block;
    }

    @Override
    protected GeoBlock readMultilevelData(GeoRegion geoRegion, int... args)
    {
        int start = _pos.get();
        final GeoBlockMultiLevel block = new GeoBlockMultiLevel(geoRegion);
        block.extendCells(8, 8);
        for (int x = 0; x < 8; x++)
        {
            for (int y = 0; y < 8; y++)
            {
                final int layers = getBuffer(getFileAsByteArray(), Byte.BYTES, _pos.getAndAdd(Byte.BYTES), true).get();
                block.extendLayers(x, y, layers);
                for (int index = 0; index < layers; index++)
                {
                    final short height = getBuffer(getFileAsByteArray(), Short.BYTES, _pos.getAndAdd(Short.BYTES), true).getShort();

                    final GeoMainCell cell = new GeoMainCell(block, x, y, index);
                    cell.setHeight(GeoMainCell.decodeHeight(height));
                    cell.setNswe(GeoMainCell.decodeNswe(height));

                    block.addCell(cell);
                }
            }
        }
        block.setConvDatType(_pos.get() - start);
        return block;
    }

    @Override
    public int[] getXYcord()
    {
        if (_xycords == null)
        {
            if (getPathToGeodataFile() == null || !getPathToGeodataFile().isFile() || !ConfigParser.isDigit(getPathToGeodataFile().getName().split("_")[0]) || !ConfigParser.isDigit(getPathToGeodataFile().getName().split("_")[1].split("\\.")[0]))
            {
                _xycords = new int[2];
            }
            else
            {
                return  _xycords = new int[]
                        {
                                Integer.parseInt(getPathToGeodataFile().getName().split("_")[0]),
                                Integer.parseInt(getPathToGeodataFile().getName().split("_")[1].split("\\.")[0])
                        };
            }
        }
        return _xycords;
    }
}
