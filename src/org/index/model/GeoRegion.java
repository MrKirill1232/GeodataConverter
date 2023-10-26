package org.index.model;

import org.index.config.configs.MainConfig;
import org.index.model.blocks.GeoBlock;

/**
 * @author Index
 */
public class GeoRegion
{
    private final int _x;
    private final int _y;

    private final GeoBlock[][] _registeredBlocks;

    private int _flatBlocks = 0;
    private int _complexUndMultilayerBlocks = 0;
    private int _cellCount = 0;

    public GeoRegion(int x, int y)
    {
        _x = x;
        _y = y;
        _registeredBlocks = new GeoBlock[MainConfig.GEO_REGION_SIZE][MainConfig.GEO_REGION_SIZE];
    }

    public void addBlock(int blockX, int blockY, GeoBlock block)
    {
        _registeredBlocks[blockX][blockY] = block;
        switch (block.getBlockType())
        {
            case FLAT:
            {
                _flatBlocks += 1;
                _complexUndMultilayerBlocks += 1;
                return;
            }
            case COMPLEX:
            // case MULTILEVEL:
            {
                _complexUndMultilayerBlocks += 1;
                return;
            }
        }
    }

    public void addCellCount(int cellCount)
    {
        _cellCount += cellCount;
    }

    public int getX()
    {
        return _x;
    }

    public int getY()
    {
        return _y;
    }

    public GeoBlock[][] getBlocks()
    {
        return _registeredBlocks;
    }

    public int getFlatBlocks()
    {
        return _flatBlocks;
    }

    public int getComplexUndMultilayerBlocks()
    {
        return _complexUndMultilayerBlocks;
    }

    public int getCellCount()
    {
        return _cellCount;
    }
}
