package org.index.data.parsers;

import git.index.fieldparser.FieldParserManager;
import git.index.fieldparser.interfaces.IFieldParser;
import git.index.fieldparser.model.FieldClassRef;
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
        return type.getInstanceOfReader(pathToFile);
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

    public int[] getXYcord()
    {
        if ((getPathToGeodataFile() == null) || (!getPathToGeodataFile().isFile()))
        {
            return new int[2];
        }
        String[] splitBySub = getPathToGeodataFile().getName().split("_", 2);
        if (splitBySub.length != 2)
        {
            return new int[2];
        }
        if ((splitBySub[0].length() != 2) || (splitBySub[1].length() < 2))
        {
            return new int[2];
        }
        String value01 = splitBySub[0];
        String value02 = splitBySub[1].substring(0, 2);
        IFieldParser<?> integerFieldParser = FieldParserManager.getInstance().getParserByFieldType(Integer.class);
        Integer xValue = integerFieldParser.parseValue(value01, new FieldClassRef<>(Integer.class), null);
        Integer yValue = integerFieldParser.parseValue(value02, new FieldClassRef<>(Integer.class), null);
        if (xValue == null || yValue == null)
        {
            return new int[2];
        }
        return new int[] { xValue, yValue };
    }

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
