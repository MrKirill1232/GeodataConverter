package org.index.model.blocks;

import org.index.enums.GeodataBlockTypes;
import org.index.model.GeoRegion;
import org.index.model.GeoMainCell;

/**
 * @author Index
 */
public class GeoBlockComplex extends GeoBlock
{
    public GeoBlockComplex(GeoRegion geoRegion)
    {
        super(GeodataBlockTypes.COMPLEX, geoRegion);
    }

    @Override
    public void addCell(GeoMainCell cell, int... args)
    {
        _cells[cell.getX()][cell.getY()][0] = cell;
    }

    @Override
    public GeoMainCell getCellForL2D(int height, int cellX, int cellY)
    {
        return _cells[cellX][cellY][0];
    }
}
