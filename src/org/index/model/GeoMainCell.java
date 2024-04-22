package org.index.model;

import org.index.config.configs.MainConfig;
import org.index.enums.GeodataBlockTypes;
import org.index.enums.GeodataCellDirectionFlag;
import org.index.model.blocks.GeoBlock;

/**
 * @author Index
 */
public class GeoMainCell
{
    private final static int HEIGHT_MASK = 0x0000FFF0;

    private final GeoBlock _block;
    private final int[] _cordinates;
    private final int _layer;
    private short _height;
    private Short _minHeight = null;
    private byte _nswe;

    public GeoMainCell(GeoBlock block, int x, int y, int layer)
    {
        _block = block;
        _cordinates = new int[] {x, y};
        _layer = layer;
    }

    public void setHeight(int height)
    {
        _height = (short) height;
    }

    public void setMinHeight(int height)
    {
        _minHeight = (short) height;
    }

    public short getMinHeight()
    {
        return _minHeight == null ? _height : _minHeight;
    }

    public void setNswe(int nswe)
    {
        _nswe = (byte) nswe;
    }

    public int getX()
    {
        return _cordinates[GeoBlock.X_CORD];
    }

    public int getY()
    {
        return _cordinates[GeoBlock.Y_CORD];
    }

    public int getLayer()
    {
        return _layer;
    }

    public short getHeightMask(short height)
    {
        return _block == null || GeodataBlockTypes.FLAT.equals(_block.getBlockType()) ? height : encodeNsweAndHeightToMask(height, _nswe);
    }

    public short getHeight()
    {
        return _height;
    }

    public byte getNswe()
    {
        return _nswe;
    }

    public GeoBlock getBlock()
    {
        return _block;
    }

    public static short decodeHeight(short height)
    {
        short iHeight = height;
        iHeight &= (short) HEIGHT_MASK;
        iHeight >>= 1;
        if (iHeight <= MainConfig.HEIGHT_MIN_VALUE)
        {
            return (short) MainConfig.HEIGHT_MIN_VALUE;
        }
        if (iHeight >= MainConfig.HEIGHT_MAX_VALUE)
        {
            return (short) MainConfig.HEIGHT_MAX_VALUE;
        }
        return (short) iHeight;
    }

    public static short decodeNswe(int height)
    {
        return (short) ((short) height & (byte) GeodataCellDirectionFlag.NSWE_MASK);
    }

    public static short encodeNsweAndHeightToMask(short height, short nswe)
    {
//        String binaryHeight = valuesByShort((short) ((height) << 1));
//        String binaryNswe = Integer.toBinaryString(nswe);
//        String binaryMaskValue = null;
//        binaryMaskValue = (binaryHeight.substring(0, 12) + binaryNswe);
//
//        return (short) ((int) (Integer.valueOf(binaryMaskValue, 2)));

        height <<= 1;
        height &= (short) HEIGHT_MASK;
        height |= (byte) nswe;
        return height;
    }

//    public static String valuesByShort(short shortValue)
//    {
//        StringBuilder binaryString = new StringBuilder(Short.SIZE);
//        for (int i = Short.SIZE - 1; i >= 0; i--)
//        {
//            char bit = ((shortValue >> i) & 1) == 1 ? '1' : '0';
//            binaryString.append(bit);
//        }
//        return binaryString.toString();
//    }
}

