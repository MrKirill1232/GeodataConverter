package org.index.config.configs;

import org.index.config.IConfig;
import org.index.config.annotations.ConfigParameter;
import org.index.config.parsers.MainConfigParser;
import org.index.enums.GeodataExtensions;

import java.io.File;

/**
 * @author Index
 */
public class MainConfig implements IConfig
{
    @ConfigParameter(parameterName = "MIN_X_CORD")
    public static int MIN_X_COORDINATE = 10;
    @ConfigParameter(parameterName = "MAX_X_CORD")
    public static int MAX_X_COORDINATE = 26;
    @ConfigParameter(parameterName = "MIN_Y_CORD")
    public static int MIN_Y_COORDINATE = 10;
    @ConfigParameter(parameterName = "MAX_Y_CORD")
    public static int MAX_Y_COORDINATE = 25;

    @ConfigParameter(parameterName = "HEIGHT_MIN_VALUE")
    public static int HEIGHT_MIN_VALUE = -16384;
    @ConfigParameter(parameterName = "HEIGHT_MAX_VALUE")
    public static int HEIGHT_MAX_VALUE =  16376;

    public static GeodataExtensions PARSE_FORMAT = null;
    public static GeodataExtensions WRITE_FORMAT = null;

    @ConfigParameter(ignoredParameter = true)
    public static File PATH_TO_RUNNING = null;

    @ConfigParameter(parameterName = "GEO_REGION_SIZE")
    public static int GEO_REGION_SIZE = 256;

    @Override
    public void onLoad()
    {
        try
        {
            PATH_TO_RUNNING = new File("").getCanonicalFile();
        }
        catch (Exception e)
        {
            PATH_TO_RUNNING = null;
        }
        PARSE_FORMAT = GeodataExtensions.valueOf(MainConfigParser.getInstance().getParsedData().getString("READ_FORMAT", null).toUpperCase());
        WRITE_FORMAT = GeodataExtensions.valueOf(MainConfigParser.getInstance().getParsedData().getString("SAVE_FORMAT", null).toUpperCase());
    }
}
