package org.index.config.configs;

import git.index.configparser.annotations.ConfigParameterVariable;
import git.index.fieldparser.annotations.FieldParser;
import org.index.enums.GeodataExtensions;

import java.io.File;

/**
 * @author Index
 */
public class MainConfig
{
    @ConfigParameterVariable(parameterName = "MIN_X_CORD")
    public static int MIN_X_COORDINATE = 10;
    @ConfigParameterVariable(parameterName = "MAX_X_CORD")
    public static int MAX_X_COORDINATE = 26;
    @ConfigParameterVariable(parameterName = "MIN_Y_CORD")
    public static int MIN_Y_COORDINATE = 10;
    @ConfigParameterVariable(parameterName = "MAX_Y_CORD")
    public static int MAX_Y_COORDINATE = 25;

    @ConfigParameterVariable(parameterName = "HEIGHT_MIN_VALUE")
    public static int HEIGHT_MIN_VALUE = -16384;
    @ConfigParameterVariable(parameterName = "HEIGHT_MAX_VALUE")
    public static int HEIGHT_MAX_VALUE =  16376;

    @ConfigParameterVariable(parameterName = "READ_FORMAT",
            fieldParser = @FieldParser(classType = GeodataExtensions.class))
    public static GeodataExtensions PARSE_FORMAT = null;
    @ConfigParameterVariable(parameterName = "SAVE_FORMAT",
            fieldParser = @FieldParser(classType = GeodataExtensions[].class, genericClasses = GeodataExtensions.class))
    public static GeodataExtensions[] WRITE_FORMAT = null;

    @ConfigParameterVariable(ignoredParameter = true, notPresentedInConfig = true)
    public static File PATH_TO_RUNNING = null;

    @ConfigParameterVariable(parameterName = "GEO_REGION_SIZE")
    public static int GEO_REGION_SIZE = 256;

    @ConfigParameterVariable(parameterName = "SHUFFLE_L2S_CRYPT")
    public static boolean SHUFFLE_L2S_CRYPT = false;

    @ConfigParameterVariable(parameterName = "L2S_BIND_IP_ADDRESS")
    public static String L2S_BIND_IP_ADDRESS = "127.0.0.1";

    public void onStartLoad()
    {
        try
        {
            PATH_TO_RUNNING = new File("").getCanonicalFile();
        }
        catch (Exception e)
        {
            PATH_TO_RUNNING = null;
        }
    }
}
