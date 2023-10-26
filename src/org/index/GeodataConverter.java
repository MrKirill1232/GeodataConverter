package org.index;

import org.index.config.configs.MainConfig;
import org.index.config.parsers.MainConfigParser;
import org.index.data.parsers.AbstractGeodataParser;
import org.index.data.parsers.L2DGeodataParser;
import org.index.data.writers.AbstractGeodataWriter;
import org.index.enums.GeodataExtensions;
import org.index.model.GeoRegion;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Index
 */
public class GeodataConverter
{
    // private final GeoRegion[][] _parsedRegions = new GeoRegion[MainConfig.MAX_X_COORDINATE + 1][MainConfig.MAX_Y_COORDINATE + 1];

    AtomicInteger counter = new AtomicInteger(0);

    public GeodataConverter()
    {
        GeodataExtensions read = MainConfig.PARSE_FORMAT;
        GeodataExtensions write = MainConfig.WRITE_FORMAT;
        if (read == null || write == null)
        {
            System.err.println("Cannot run convertor because writer or reader is null.");
            return;
        }
        System.out.println("Read format - " + read.name() + ";");
        System.out.println("Write format - " + write.name() + ";");
        final File readPath = new File(MainConfig.PATH_TO_RUNNING, "work/" + read.name().toLowerCase());
        final File writePath = new File(MainConfig.PATH_TO_RUNNING, "work/" + write.name().toLowerCase() + "_output");
        parseGeoFiles(read, readPath, write, writePath);
    }

    private void parseGeoFiles(GeodataExtensions read, File readPath, GeodataExtensions write, File writePath)
    {
        final File[] files = readPath.listFiles();
        if (files == null)
        {
            System.err.println("Cannot run convertor because writer or read files is null");
            return;
        }
        if (files.length == 0)
        {
            System.out.println("Any files for read. Searching path " + readPath.toString() + ";");
            return;
        }
        for (File file : files)
        {
            if (!file.getName().endsWith(new String(read.getExtension())))
            {
                System.err.println("Writing... " + 3 + ";");
                continue;
            }
            AbstractGeodataParser parserClass = AbstractGeodataParser.createNewInstance(read, file);
            if (parserClass == null)
            {
                System.err.println("Unknown error while parsing file " + file + ".");
                continue;
            }
            if (!parserClass.validGeoFile())
            {
                System.err.println("Geo file is not correct. " + file + ".");
                continue;
            }
            if (!parserClass.checkGeodataCrypt())
            {
                System.err.println("Geo file is under the crypt. Continue... " + file + ".");
                continue;
            }
            System.err.println("Reading... " + file.getName() + ";");
            GeoRegion region = parserClass.read();
            writeGeoFile(region, write, writePath);
            // _parsedRegions[region.getX()][region.getY()] = region;
            System.err.println((int) ((double) counter.addAndGet(1) / (double) files.length * 100d) + "% / " + "100%");
        }
    }

    private void writeGeoFile(GeoRegion region, GeodataExtensions write, File writePath)
    {
        if (!writePath.exists())
        {
            writePath.mkdir();
        }
        if (region != null)
        {
            final File file = new File(writePath, region.getX() + "_" + region.getY() + new String(write.getExtension()));
            AbstractGeodataWriter writeClass = AbstractGeodataWriter.createNewInstance(region, write, file);
            if (writeClass == null)
            {
                System.err.println("You select unsupported geodata write format... " + write.name() + "; Maybe it will be added in future!");
                return;
            }
            System.err.println("Writing... " + file.getName() + ";");
            writeClass.write();
        }
    }

    public static void main(String[] args)
    {
        MainConfigParser.getInstance().load();
        if (true)
        {
            new GeodataConverter();
        }
        else
        {
            AbstractGeodataParser dataOriginal = new L2DGeodataParser(new File(MainConfig.PATH_TO_RUNNING, "work\\conv_dat\\" +"22_20.l2d"));
            // GeoRegion original = dataOriginal.read();
            AbstractGeodataParser dataReParsed = new L2DGeodataParser(new File(MainConfig.PATH_TO_RUNNING, "22_20.l2d"));
            // GeoRegion reparsed = dataReParsed.read();

            for (int index = 0; index < dataOriginal.getFileAsByteArray().length; index++)
            {
                if (dataOriginal.getFileAsByteArray()[index] != dataReParsed.getFileAsByteArray()[index])
                {
                    System.err.println(index + " not match!" + "\n" + "original: " + dataOriginal.getFileAsByteArray()[index] + "\n" + "reparsed: " + dataReParsed.getFileAsByteArray()[index]);
                    break;
                }
            }
        }
    }
}
