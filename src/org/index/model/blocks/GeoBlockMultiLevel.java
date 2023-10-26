package org.index.model.blocks;

import org.index.enums.GeodataBlockTypes;
import org.index.model.GeoRegion;
import org.index.model.GeoMainCell;

/**
 * @author Index
 */
public class GeoBlockMultiLevel extends GeoBlock
{
    public GeoBlockMultiLevel(GeoRegion region)
    {
        super(GeodataBlockTypes.MULTILEVEL, region);
    }

    @Override
    public void addCell(GeoMainCell cell, int... args)
    {
        _cells[cell.getX()][cell.getY()][cell.getLayer()] = cell;
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
        int layers = getCells()[localX][localY].length;
        for (int layer = 0; layer < layers; layer++)
        {
            GeoMainCell cell = getCells()[localX][localY][layer];
            if (cell.getHeight() > height)
            {
                return cell.getNswe();
            }
        }
        return 0;
    }
}
