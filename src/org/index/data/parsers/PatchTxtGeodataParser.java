package org.index.data.parsers;

import org.index.enums.GeodataExtensions;
import org.index.model.GeoRegion;
import org.index.model.blocks.GeoBlock;

import java.io.File;

public class PatchTxtGeodataParser extends AbstractGeodataParser
{
    public PatchTxtGeodataParser(File pathToGeoFile)
    {
        super(GeodataExtensions.PATCH_TXT, pathToGeoFile);
    }

    @Override
    public GeoRegion read()
    {
        return null;
    }

    @Override
    public int[] getXYcord()
    {
        return new int[0];
    }

    @Override
    protected void readBlocks(GeoRegion geoRegion)
    {

    }

    @Override
    protected GeoBlock readFlatData(GeoRegion geoRegion)
    {
        return null;
    }

    @Override
    protected GeoBlock readComplexData(GeoRegion geoRegion)
    {
        return null;
    }

    @Override
    protected GeoBlock readMultilevelData(GeoRegion geoRegion)
    {
        return null;
    }
}
