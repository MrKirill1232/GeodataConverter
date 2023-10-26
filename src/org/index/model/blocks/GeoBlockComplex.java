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
    public int getL2DNswe(int height)
    {
        int localX = ((getX() % 8) * 8);
        int localY = (getY() % 8);
        if (localX < 0 || localY < 0 || getCells().length <= localX || getCells()[localX] == null || getCells()[localX].length <= localY || getCells()[localX][localY] == null)
        {
            return 0;
        }
        GeoMainCell cell = getCells()[localX][localY][0];
        return (byte) (cell.getHeight() < height ? cell.getNswe() : 0);
    }
}
