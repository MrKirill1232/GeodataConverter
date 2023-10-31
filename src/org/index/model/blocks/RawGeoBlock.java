package org.index.model.blocks;

import org.index.model.GeoMainCell;

/**
 * @author Index
 */
public class RawGeoBlock extends GeoBlock
{
    public RawGeoBlock()
    {
        super(null, null);
        extendCells(8, 8);
    }


    @Override
    public void addCell(GeoMainCell cell, int... args)
    {
        _cells[cell.getX()][cell.getY()][cell.getLayer()] = cell;
    }

    @Override
    public int getL2DNswe(int height)
    {
        return 0;
    }
}
