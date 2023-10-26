package org.index.enums;

/**
 * @author Index
 */
public enum GeodataBlockTypes
{
    FLAT(0, -48, 0),
    COMPLEX(64, -47, 0),
    MULTILEVEL(40, -46, 256),
    ;

    private final int _convDatType;
    private final int _l2dType;
    private final int _initialSize;

    GeodataBlockTypes(int convDatType, int l2dType, int initialSize)
    {
        _convDatType = convDatType;
        _l2dType = l2dType;
        _initialSize = initialSize;
    }

    public static GeodataBlockTypes getType(GeodataExtensions geoType, int value)
    {
        switch (geoType)
        {
            case CONV_DAT:
            {
                if (value == FLAT.getConvDatType())
                {
                    return FLAT;
                }
                else if (value == COMPLEX.getConvDatType())
                {
                    return COMPLEX;
                }
                return MULTILEVEL;
            }
            case L2D:
            case RP:
            {
                if (value == FLAT.getL2dType())   // 0xD0
                {
                    return FLAT;
                }
                else if (value == COMPLEX.getL2dType())  // 0xD1
                {
                    return COMPLEX;
                }
                else if (value == MULTILEVEL.getL2dType())  // 0xD2
                {
                    return MULTILEVEL;
                }
                return null;
            }
            default:
            {
                return value >= values().length || value < 0 ? null : values()[value];
            }
        }
    }

    public byte getConvDatType()
    {
        return (byte) _convDatType;
    }

    public byte getL2dType()
    {
        return (byte) _l2dType;
    }

    public int getInitialSize()
    {
        return _initialSize;
    }
}
