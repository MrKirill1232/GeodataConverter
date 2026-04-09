package org.index.enums;

import org.index.data.parsers.*;
import org.index.data.writers.*;
import org.index.model.GeoRegion;

import java.io.File;
import java.util.function.BiFunction;

/**
 * @author Index
 */
public enum GeodataExtensions
{
    L2J(        ".l2j"      , L2JGeodataParser      ::new   , L2JGeodataWriter  ::new   , false , false ),
    CONV_DAT(   "_conv.dat" , ConvDatGeodataParser  ::new   , ConvDatGeodataWriter::new , true  , false ),
    L2D(        ".l2d"      , L2DGeodataParser      ::new   , L2DGeodataWriter::new     , false , false ),
    L2S(        ".l2s"      , L2SGeodataParser      ::new   , null          , false , true  ),
    L2G(        ".l2g"      , L2GGeodataParser      ::new   , L2GGeodataWriter::new     , false , true  ),
    RP(         ".rp"       , L2DGeodataParser      ::new   , null          , false , false ),
    PATH_TXT(   "_path.txt" , PatchTxtGeodataParser ::new   , null          , false , false ),
    L2M(        ".l2m"      , L2MGeodataParser      ::new   , null          , false , false ),
    ;

    private final String _extension;
    private final BiFunction<GeodataExtensions, File, AbstractGeodataParser> _readerSupplier;
    private final BiFunction<GeoRegion, File, AbstractGeodataWriter> _writerSupplier;
    private final boolean _needToValid;
    private final boolean _canBeCrypted;

    GeodataExtensions(String extension,
                      BiFunction<GeodataExtensions, File, AbstractGeodataParser> readerSupplier,
                      BiFunction<GeoRegion, File, AbstractGeodataWriter> writerSupplier,
                      boolean needToValid, boolean canBeCrypted)
    {
        _extension = extension;
        _readerSupplier = readerSupplier;
        _writerSupplier = writerSupplier;
        _needToValid = needToValid;
        _canBeCrypted = canBeCrypted;
    }

    public String getExtension()
    {
        return _extension;
    }

    public boolean canCreateInstanceOfReader()
    {
        return _readerSupplier != null;
    }

    public boolean canCreateInstanceOfWriter()
    {
        return _writerSupplier != null;
    }

    public AbstractGeodataParser getInstanceOfReader(File pathToFile)
    {
        return _readerSupplier.apply(this, pathToFile);
    }

    public AbstractGeodataWriter getInstanceOfWriter(GeoRegion geoRegion, File savePath)
    {
        return _writerSupplier.apply(geoRegion, savePath);
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
