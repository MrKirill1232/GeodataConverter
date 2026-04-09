package org.index.config.parsers;

import git.index.configparser.model.AbstractConfigHolder;
import org.index.config.configs.MainConfig;

/**
 * @author Index
 */
public class MainConfigParser extends AbstractConfigHolder<MainConfig>
{
    @Override
    public String getConfigPath()
    {
        return "work/config/Main.ini";
    }

    @Override
    public Class<MainConfig> getAttachedConfig()
    {
        return MainConfig.class;
    }

    private final static MainConfigParser ACTIVE_INSTANCE = new MainConfigParser();

    public static MainConfigParser getInstance()
    {
        return ACTIVE_INSTANCE;
    }
}
