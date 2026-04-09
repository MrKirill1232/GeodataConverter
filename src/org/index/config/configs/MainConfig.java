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

    @ConfigParameterVariable(parameterName = "RUN_OPTIMIZATION")
    public static boolean RUN_OPTIMIZATION = true;

    @ConfigParameterVariable(parameterName = "HEIGH_DIFF_FOR_OPTIMIZATION")
    public static int HEIGH_DIFF_FOR_OPTIMIZATION = 16;

    @ConfigParameterVariable(parameterName = "HEIGHT_DIFF_IN_SURROUNDED_BLOCK")
    public static int HEIGHT_DIFF_IN_SURROUNDED_BLOCK = 16;

    @ConfigParameterVariable(parameterName = "WALL_HEIGH_FOR_OPTIMIZATION")
    public static int WALL_HEIGH_FOR_OPTIMIZATION = 64;

    @ConfigParameterVariable(parameterName = "CAN_OPTIMIZE_BLOCK_ON_EDGE")
    public static boolean CAN_OPTIMIZE_BLOCK_ON_EDGE = false;

    @ConfigParameterVariable(parameterName = "LOG_FLAT_BLOCK_ISSUE")
    public static boolean LOG_FLAT_BLOCK_ISSUE_ON_OPTIMIZATION = false;

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
