package org.index.model.blocks;

import org.index.enums.GeodataBlockTypes;
import org.index.model.GeoRegion;
import org.index.model.GeoMainCell;

/**
 * @author Index
 */
public abstract class GeoBlock
{
    public static final int X_CORD = 0;
    public static final int Y_CORD = 1;

    private final GeodataBlockTypes _blockType;
    private final GeoRegion _region;
    private int _x;
    private int _y;
    protected GeoMainCell[][][] _cells;
    // x, y, layers
    private int _convDatType;

    public GeoBlock(GeodataBlockTypes blockType, GeoRegion region)
    {
        _blockType = blockType;
        _region = region;
    }

    public abstract void addCell(GeoMainCell cell, int... args);

    public void extendLayers(int x, int y, int layers)
    {
        _cells[x][y] = new GeoMainCell[layers];
        if (_blockType != null && !_blockType.equals(GeodataBlockTypes.FLAT))
        {
            _region.addCellCount(layers);
        }
    }

    public void extendCells(int maxX, int maxY)
    {
        _cells = new GeoMainCell[maxX][maxY][0];
    }

    public GeodataBlockTypes getBlockType()
    {
        return _blockType;
    }

    public GeoMainCell[][][] getCells()
    {
        return _cells;
    }

    public void setConvDatType(int size)
    {
        _convDatType = 64 + (size - GeodataBlockTypes.MULTILEVEL.getInitialSize() / 2);
    }

    public short getConvDatType()
    {
        return (short) (_convDatType);
    }

    public void setXY(int x, int y)
    {
        _x = x;
        _y = y;
    }

    public int getX()
    {
        return _x;
    }

    public int getY()
    {
        return _y;
    }

    public GeoRegion getRegion()
    {
        return _region;
    }

    public abstract GeoMainCell getCellForL2D(int height, int cellX, int cellY);
}
