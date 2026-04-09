package org.index;

import git.index.dummylogger.LoggerImpl;
import org.index.config.configs.MainConfig;
import org.index.config.parsers.MainConfigParser;
import org.index.data.parsers.AbstractGeodataParser;
import org.index.data.writers.AbstractGeodataWriter;
import org.index.enums.GeodataExtensions;
import org.index.model.GeoRegion;
import org.index.utils.L2ScriptCryptShuffle;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Index
 */
public class GeodataConverter
{
    private final static LoggerImpl LOGGER = new LoggerImpl(GeodataConverter.class);

    private final AtomicInteger counter = new AtomicInteger(0);

    public GeodataConverter()
    {
        GeodataExtensions read = MainConfig.PARSE_FORMAT;
        GeodataExtensions[] writes = MainConfig.WRITE_FORMAT;
        if (read == null || writes == null)
        {
            LOGGER.error("Cannot run convertor because writer or reader is null.");
            return;
        }
        LOGGER.info(String.format("Reading format - '%s';", read.name()));
        LOGGER.info(String.format("Writing format - '%s';", Arrays.toString(writes)));
        final File readPath = new File(MainConfig.PATH_TO_RUNNING, "work/" + "input");
        final File writePath = new File(MainConfig.PATH_TO_RUNNING, "work/" + "output");
        parseGeoFiles(read, readPath, writes, writePath);
    }

    private void parseGeoFiles(GeodataExtensions read, File readPath, GeodataExtensions[] writes, File writePath)
    {
        final File[] files = readPath.listFiles();
        if (files == null)
        {
            LOGGER.error("Cannot run convertor because writer or reader is null.");
            return;
        }
        if (files.length == 0)
        {
            LOGGER.error(String.format("Any files for read. Searching path '%s'.", readPath));
            return;
        }
        final String extension = read.getExtension();
        for (File file : files)
        {
            if (!file.getName().toLowerCase().endsWith(extension))
            {
                counter.addAndGet(1);
                LOGGER.info(String.format("Wrong file format '%s'... Continue...", file.getName()));
                continue;
            }
            AbstractGeodataParser parserClass = AbstractGeodataParser.createNewInstance(read, file);
            if (parserClass == null)
            {
                LOGGER.error(String.format("Unknown error while parsing file '%s'.", file));
                continue;
            }
            if (!parserClass.validGeoFile())
            {
                LOGGER.error(String.format("Geo file is not correct '%s'.", file));
                continue;
            }
            if (!parserClass.checkGeodataCrypt())
            {
                LOGGER.error(String.format("Geo file is under the crypt. Continue... '%s'.", file));
                continue;
            }
            LOGGER.info(String.format("Reading... '%s'.", file.getName()));
            GeoRegion region = parserClass.read();
            writeGeoFile(region, writes, writePath);
            LOGGER.info(String.format("%d / %S", (int) ((double) counter.addAndGet(1) / (double) files.length * 100d), "100%"));
        }
    }

    private void writeGeoFile(GeoRegion region, GeodataExtensions[] writes, File writePath)
    {
        if (!writePath.exists())
        {
            writePath.mkdir();
        }
        if (region != null)
        {
            for (GeodataExtensions write : writes)
            {
                final File file = new File(writePath, region.getX() + "_" + region.getY() + write.getExtension());
                AbstractGeodataWriter writeClass = AbstractGeodataWriter.createNewInstance(region, write, file);
                if (writeClass == null)
                {
                    LOGGER.info("You select unsupported geodata write format... " + write.name() + "; Maybe it will be added in future!");
                    return;
                }
                LOGGER.info("Writing... " + file.getName() + ";");
                writeClass.write();
            }
        }
    }

    public static void main(String[] args)
    {
        MainConfigParser.getInstance().load();
        if (GeodataExtensions.L2S.equals(MainConfig.PARSE_FORMAT) && MainConfig.SHUFFLE_L2S_CRYPT)
        {
            L2ScriptCryptShuffle shuffle = new L2ScriptCryptShuffle(new File(MainConfig.PATH_TO_RUNNING, "work/" + "input"));
            MainConfig.L2S_BIND_IP_ADDRESS = shuffle.getBindIpAddress();
        }
        new GeodataConverter();
    }
}
