package org.index.enums;

import java.nio.charset.StandardCharsets;

/**
 * @author Index
 */
public enum GeodataExtensions
{
    L2J(".l2j".getBytes(StandardCharsets.UTF_8), false, false),
    CONV_DAT("_conv.dat".getBytes(StandardCharsets.UTF_8), true, false),
    L2D(".l2d".getBytes(StandardCharsets.UTF_8), false, false),
    L2S(".l2s".getBytes(StandardCharsets.UTF_8), false, true),
    L2G(".l2g".getBytes(StandardCharsets.UTF_8), false, true),
    RP(".rp".getBytes(StandardCharsets.UTF_8), false, false),
    PATH_TXT("_path.txt".getBytes(StandardCharsets.UTF_8), false, false),
    L2M(".l2m".getBytes(StandardCharsets.UTF_8), false, false),
    ;

    private final byte[] _extension;
    private final boolean _needToValid;
    private final boolean _canBeCrypted;

    GeodataExtensions(byte[] extension, boolean needToValid, boolean canBeCrypted)
    {
        _extension = extension;
        _needToValid = needToValid;
        _canBeCrypted = canBeCrypted;
    }

    public byte[] getExtension()
    {
        return _extension;
    }

    public boolean isNeedToValid()
    {
        return _needToValid;
    }

    public boolean isCanBeCrypted()
    {
        return _canBeCrypted;
    }
}
