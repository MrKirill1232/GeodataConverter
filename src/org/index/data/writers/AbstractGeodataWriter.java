package org.index.data.writers;

import org.index.enums.GeodataExtensions;
import org.index.model.GeoRegion;
import org.index.model.blocks.GeoBlock;

import java.io.File;

/**
 * @author Index
 */
public abstract class AbstractGeodataWriter
{
    private final GeoRegion _region;
    protected final File _pathToFile;

    public static AbstractGeodataWriter createNewInstance(GeoRegion region, GeodataExtensions type, File pathToFile)
    {
        switch (type)
        {
            case L2J:
            {
                return new L2JGeodataWriter(region, pathToFile);
            }
            case CONV_DAT:
            {
                return new ConvDatGeodataWriter(region, pathToFile);
            }
            case L2G:
            {
                return new L2GGeodataWriter(region, pathToFile);
            }
            case L2D:
            {
                return new L2DGeodataWriter(region, pathToFile);
            }
            default:
            {
                return null;
            }
        }
    }

    public AbstractGeodataWriter(GeoRegion region, File pathToFile)
    {
        _region = region;
        _pathToFile = pathToFile;
    }

    protected GeoRegion getRegion()
    {
        return _region;
    }

    public abstract void write();

    protected abstract byte[] storeFlatBlock(GeoBlock block);

    protected abstract byte[] storeComplexBlock(GeoBlock block);

    protected abstract byte[] storeMultilayerBlock(GeoBlock block);

    public byte[] cryptByteData(byte[] array)
    {
        return array;
    }
}
