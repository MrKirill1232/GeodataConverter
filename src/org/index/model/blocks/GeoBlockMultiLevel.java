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
    public GeoMainCell getCellForL2D(int height, int cellX, int cellY)
    {
        GeoMainCell[] cells = _cells[cellX][cellY];
        for (int layer = (cells.length - 1); layer > 0; layer--)
        {
            GeoMainCell cell = cells[layer];
            if (cell.getHeight() < height)
            {
                return cell;
            }
        }
        return null;
    }
}
