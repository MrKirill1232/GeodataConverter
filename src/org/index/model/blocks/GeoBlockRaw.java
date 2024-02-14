package org.index.model.blocks;

import org.index.model.GeoMainCell;
import org.index.model.GeoRegion;

/**
 * @author Index
 */
public class GeoBlockRaw extends GeoBlock
{
    public GeoBlockRaw(GeoRegion region)
    {
        super(null, region);
    }

    @Override
    public void addCell(GeoMainCell cell, int... args)
    {
        _cells[cell.getX()][cell.getY()][cell.getLayer()] = cell;
    }

    @Override
    public GeoMainCell getCellForL2D(int height, int cellX, int cellY)
    {
        return null;
    }
}
