package org.index.model;

import org.index.config.configs.MainConfig;
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
    private final short _heightMask;
    private short _height;
    private short _nswe;
    private int _encoded;
    private GeodataCellDirectionFlag _direction;

    public GeoMainCell(GeoBlock block, int x, int y, int layer, short heightMask)
    {
        _block = block;
        _cordinates = new int[] {x, y};
        _layer = layer;
        _heightMask = heightMask;
    }

    public void setHeight(int height)
    {
        _height = (short) height;
    }

    public void setNswe(short nswe)
    {
        _nswe = nswe;
    }

    public void setDirection(GeodataCellDirectionFlag direction)
    {
        _direction = direction;
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

    public int getHeightMask()
    {
        return _heightMask;
    }

    public short getHeight()
    {
        return _height;
    }

    public short getNswe()
    {
        return _nswe;
    }

    public void setEncode()
    {
        _encoded = encodeNsweAndHeightToMask(_height, _nswe);
    }

    public int getEncoded()
    {
        return _encoded;
    }

    public GeoBlock getBlock()
    {
        return _block;
    }

    public static short decodeHeight(short height)
    {
        if (height <= MainConfig.HEIGHT_MIN_VALUE)
        {
            return (short) MainConfig.HEIGHT_MIN_VALUE;
        }
        if (height >= MainConfig.HEIGHT_MAX_VALUE)
        {
            return (short) MainConfig.HEIGHT_MAX_VALUE;
        }
        height &= HEIGHT_MASK;
        height >>= 1;
        return height;
    }

    public static short decodeNswe(int height)
    {
        return (short) ((short) height & GeodataCellDirectionFlag.NSWE_MASK);
    }

    public static int encodeNsweAndHeightToMask(short height, short nswe)
    {
//        String binaryHeight = valuesByShort((short) ((height) << 1));
//        String binaryNswe = Integer.toBinaryString(nswe);
//        String binaryMaskValue = null;
//        binaryMaskValue = (binaryHeight.substring(0, 12) + binaryNswe);
//
//        return (short) ((int) (Integer.valueOf(binaryMaskValue, 2)));

        height <<= 1;
        height &= HEIGHT_MASK;
        height |= nswe;
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

