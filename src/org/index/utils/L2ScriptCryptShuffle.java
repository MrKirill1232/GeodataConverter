package org.index.utils;

import org.index.config.configs.MainConfig;
import org.index.data.parsers.L2SGeodataParser;
import org.index.enums.GeodataExtensions;
import org.index.model.GeoRegion;
import org.index.model.blocks.GeoBlock;

import java.io.File;
import java.util.Locale;

/**
 * @author Index
 */
public class L2ScriptCryptShuffle
{
    private final File _file;
    private String _ipAddress;

    public L2ScriptCryptShuffle(File file)
    {
        if (file == null || !file.exists())
        {
            throw new RuntimeException("File not found.");
        }
        if (file.isDirectory())
        {
            String  l2sExtension    = new String(GeodataExtensions.L2S.getExtension());
            File    minSizeOfFile   = null;
            File[]  files           = file.listFiles();
            if (files != null)
            {
                for (File reqFile : files)
                {
                    if (reqFile.getName().toLowerCase(Locale.ROOT).endsWith(l2sExtension))
                    {
                        minSizeOfFile = (minSizeOfFile == null) || (minSizeOfFile.length() < reqFile.length()) ? reqFile : minSizeOfFile;
                    }
                }
            }
            _file = minSizeOfFile;
        }
        else
        {
            _file = file;
        }
        if (_file == null)
        {
            throw new RuntimeException("File not found.");
        }
        shuffleCrypt();
    }

    private void shuffleCrypt()
    {
        L2SGeodataParser parser;
        for (int index01 = 0; index01 < 256; index01++)
        {
            for (int index02 = 0; index02 < 256; index02++)
            {
                for (int index03 = 0; index03 < 256; index03++)
                {
                    for (int index04 = 0; index04 < 256; index04++)
                    {
                        try
                        {
                            _ipAddress = String.format("%d.%d.%d.%d", index01, index02, index03, index04);
                            parser = new L2SGeodataParser(_file, _ipAddress);
                            parser.checkGeodataCrypt();
                            GeoRegion region = parser.read();
                            if (isContainsNullBlock(region))
                            {
                                continue;
                            }
                        }
                        catch (Exception e)
                        {
                            continue;
                        }
                        System.err.println("Your BIND IP IS " + _ipAddress);
                        return;
                    }
                }
            }
        }
    }

    private boolean isContainsNullBlock(GeoRegion region)
    {
        for (int blockX = 0; blockX < MainConfig.GEO_REGION_SIZE; blockX++)
        {
            for (int blockY = 0; blockY < MainConfig.GEO_REGION_SIZE; blockY++)
            {
                GeoBlock block = region.getBlocks()[blockX][blockY];
                if (block == null)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public String getBindIpAddress()
    {
        return _ipAddress;
    }
}
