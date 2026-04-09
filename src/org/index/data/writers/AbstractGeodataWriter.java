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
        return type.getInstanceOfWriter(region, pathToFile);
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
