package org.index.enums;

/**
 * @author Index
 */
public enum GeodataCellDirectionFlag
{
    NONE(0),
    FLAG_E(1),
    FLAG_W(2),
    FLAG_S(4),
    FLAG_N(8),
    FLAG_SE(1 << 4),
    FLAG_SW(1 << 5),
    FLAG_NE(1 << 6),
    FLAG_NW(1 << 7),
    NSWE_ALL(15),
    ;

    public final static int NSWE_MASK = 0x0000000F;

    private final byte _mask;

    GeodataCellDirectionFlag(int mask)
    {
        _mask = (byte) mask;
    }

    public byte getMask()
    {
        return _mask;
    }
}
