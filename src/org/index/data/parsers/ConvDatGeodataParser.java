package org.index.data.parsers;

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
import java.util.Arrays;

/**
 * @author Index
 */
public class ConvDatGeodataParser extends AbstractGeodataParser
{
    private int[] _header = null;

    public ConvDatGeodataParser(File pathToGeoFile)
    {
        super(GeodataExtensions.CONV_DAT, pathToGeoFile);
    }

    @Override
    public GeoRegion read()
    {
        final GeoRegion geoRegion = new GeoRegion(getXYcord()[0], getXYcord()[1]);
        if (_header == null)
        {
            readHeader();
        }
        readBlocks(geoRegion);
        return geoRegion;
    }

    @Override
    public boolean validGeoFile()
    {
        if (_header == null)
        {
            readHeader();
        }
        boolean xValidation = _header[0] >= MainConfig.MIN_X_COORDINATE && _header[0] <= MainConfig.MAX_X_COORDINATE;
        boolean yValidation = _header[1] >= MainConfig.MIN_Y_COORDINATE && _header[1] <= MainConfig.MAX_Y_COORDINATE;
        return xValidation && yValidation && super.validGeoFile();
    }

    private void readHeader()
    {
        _header = new int[7];
        // int xRegion
        _header[0] = getBuffer(getFileAsByteArray(), Byte.BYTES, _pos.getAndAdd(Byte.BYTES), true).get();
        // int yRegion
        _header[1] = getBuffer(getFileAsByteArray(), Byte.BYTES, _pos.getAndAdd(Byte.BYTES), true).get();
        // int dummy01
        _header[2] = getBuffer(getFileAsByteArray(), Short.BYTES, _pos.getAndAdd(Short.BYTES), true).getShort();   // 128
        // int dummy02
        _header[3] = getBuffer(getFileAsByteArray(), Short.BYTES, _pos.getAndAdd(Short.BYTES), true).getShort();   // 16
        // int cellCount
        _header[4] = getBuffer(getFileAsByteArray(), Integer.BYTES, _pos.getAndAdd(Integer.BYTES), true).getInt();
        // int simpleBlocks
        _header[5] = getBuffer(getFileAsByteArray(), Integer.BYTES, _pos.getAndAdd(Integer.BYTES), true).getInt();
        // int flatCount
        _header[6] = getBuffer(getFileAsByteArray(), Integer.BYTES, _pos.getAndAdd(Integer.BYTES), true).getInt();
    }

    @Override
    protected void readBlocks(GeoRegion geoRegion)
    {
        for (int blockX = 0; blockX < MainConfig.GEO_REGION_SIZE; blockX++)
        {
            for (int blockY = 0; blockY < MainConfig.GEO_REGION_SIZE; blockY++)
            {
                int type = getBuffer(getFileAsByteArray(), Short.BYTES, _pos.getAndAdd(Short.BYTES), true).getShort();
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
        short higherHeight = getBuffer(getFileAsByteArray(), Short.BYTES, _pos.getAndAdd(Short.BYTES), true).getShort();
        short lowestHeight = getBuffer(getFileAsByteArray(), Short.BYTES, _pos.getAndAdd(Short.BYTES), true).getShort();
        short height = (short) Math.max(higherHeight, lowestHeight);

        final GeoBlockFlat block = new GeoBlockFlat(geoRegion);

        final GeoMainCell cell = new GeoMainCell(block, 0, 0, 1);
        cell.setHeight(higherHeight);
        cell.setMinHeight(lowestHeight);
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

                GeoMainCell cell = new GeoMainCell(block, x, y, 1);
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
        // 65 = 258 elements
        // 127 = 382 elements
        // each second element from 65 = +1.
        // каждый второй елемент с 65 = +1
        int start = _pos.get();
        final GeoBlockMultiLevel block = new GeoBlockMultiLevel(geoRegion);
        block.extendCells(8, 8);
        for (int x = 0; x < 8; x++)
        {
            for (int y = 0; y < 8; y++)
            {
                final int layers = getBuffer(getFileAsByteArray(), Short.BYTES, _pos.getAndAdd(Short.BYTES), true).getShort();
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
        return _xycords == null
                ? getFileAsByteArray() == null
                    ? new int[2]
                    : (
                        _xycords = new int[]
                            {
                                    getFileAsByteArray()[0],
                                    getFileAsByteArray()[1]
                            }
                       )
                : _xycords;
    }
}
