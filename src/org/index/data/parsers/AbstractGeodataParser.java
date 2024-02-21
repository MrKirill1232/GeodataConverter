package org.index.data.parsers;

import org.index.config.configs.MainConfig;
import org.index.enums.GeodataExtensions;
import org.index.model.GeoRegion;
import org.index.model.blocks.GeoBlock;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Index
 */
public abstract class AbstractGeodataParser
{
    protected final AtomicInteger _pos = new AtomicInteger(0);
    private final GeodataExtensions _selectedType;
    private final File _pathToGeodataFile;
    private byte[] _fileAsByteArray;

    protected int[] _xycords = null;

    public static AbstractGeodataParser createNewInstance(GeodataExtensions type, File pathToFile)
    {
        switch (type)
        {
            case L2J:
            {
                return new L2JGeodataParser(pathToFile);
            }
            case CONV_DAT:
            {
                return new ConvDatGeodataParser(pathToFile);
            }
            case L2D:
            {
                return new L2DGeodataParser(pathToFile);
            }
            case L2S:
            {
                return new L2SGeodataParser(pathToFile, MainConfig.L2S_BIND_IP_ADDRESS);
            }
            case L2G:
            {
                return new L2GGeodataParser(pathToFile);
            }
            case L2M:
            {
                return new L2MGeodataParser(pathToFile);
            }
            case RP:
            {
                return new RPGeodataParser(pathToFile);
            }
            case PATH_TXT:
            {
                return new PatchTxtGeodataParser(pathToFile);
            }
            default:
            {
                return null;
            }
        }
    }

    public AbstractGeodataParser(GeodataExtensions type, File pathToFile)
    {
        _selectedType = type;
        _pathToGeodataFile = pathToFile;
        _fileAsByteArray = readFileAsBytes();
    }

    protected byte[] readFileAsBytes()
    {
        byte[] _fileAsByteArray = null;
        try (RandomAccessFile raf = new RandomAccessFile(_pathToGeodataFile, "r"))
        {
            int length = (int) Math.min(raf.length(), Integer.MAX_VALUE);
            raf.read(_fileAsByteArray = new byte[length], 0, length);
        }
        catch (IOException ignored)
        {
        }
        return _fileAsByteArray;
    }

    protected void updateFileByteArray(byte[] array)
    {
        _fileAsByteArray = array;
    }


    /**
     * conv_dat geo file contains some header
     * @return if validation is required - need to check and return true if geo valid.
     */
    public boolean validGeoFile()
    {
        if (getFileAsByteArray() == null || getFileAsByteArray().length == 0)
        {
            return false;
        }
        int x = getXYcord()[0];
        int y = getXYcord()[1];
        return  x >= MainConfig.MIN_X_COORDINATE && x <= MainConfig.MAX_X_COORDINATE
                &&
                y >= MainConfig.MIN_Y_COORDINATE && y <= MainConfig.MAX_Y_COORDINATE;
    }

    /**
     * l2script geodata use external crypt method. idk how to implement it - just will think you can :D
     * @return check if geodata has unique crypt signature
     */
    public boolean checkGeodataCrypt()
    {
        return !_selectedType.isCanBeCrypted();
    }

    public abstract GeoRegion read();

    public abstract int[] getXYcord();

    protected abstract void readBlocks(GeoRegion geoRegion);

    protected abstract GeoBlock readFlatData(GeoRegion geoRegion, int... args);

    protected abstract GeoBlock readComplexData(GeoRegion geoRegion, int... args);

    protected abstract GeoBlock readMultilevelData(GeoRegion geoRegion, int... args);

    public GeodataExtensions getSelectedType()
    {
        return _selectedType;
    }

    public File getPathToGeodataFile()
    {
        return _pathToGeodataFile;
    }

    public byte[] getFileAsByteArray()
    {
        return _fileAsByteArray;
    }

    protected static ByteBuffer getBuffer(byte[] inputArray, int allocate, int offset, boolean reverse)
    {
        ByteBuffer buffer = ByteBuffer.allocate(allocate);
        if (reverse)
        {
            for (int index = (offset + allocate - 1); index >= offset; index--)
            {
                buffer.put(inputArray[index]);
            }
        }
        else
        {
            for (int index = offset; index < offset + allocate; index++)
            {
                buffer.put(inputArray[index]);
            }
        }
        buffer.rewind();
        return buffer;
    }
}
