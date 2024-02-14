package org.index.model.blocks;

import org.index.enums.GeodataBlockTypes;
import org.index.model.GeoRegion;
import org.index.model.GeoMainCell;

/**
 * @author Index
 */
public class GeoBlockFlat extends GeoBlock
{
    public GeoBlockFlat(GeoRegion region)
    {
        super(GeodataBlockTypes.FLAT, region);
        extendCells(1, 1);
        extendLayers(0, 0, 1);
    }

    @Override
    public void addCell(GeoMainCell cell, int... args)
    {
        _cells[0][0][0] = cell;
    }

    @Override
    public GeoMainCell getCellForL2D(int height, int cellX, int cellY)
    {
        return _cells[0][0][0];
    }
}
