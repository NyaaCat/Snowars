package cat.nyaa.snowars;

import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.plugin.Plugin;

public class I18n extends LanguageRepository {
    private String lang = "en_US";
    private static I18n INSTANCE;

    public I18n(String language) {
        lang = language;
        this.load();
        INSTANCE = this;
    }

    public static String format(String s, Object... args) {
        return INSTANCE.getFormatted(s, args);
    }

    @Override
    protected Plugin getPlugin() {
        return SnowarsPlugin.plugin;
    }

    public void setLanguage(String lang) {
        this.lang = lang;
    }

    @Override
    protected String getLanguage() {
        return lang;
    }

    public void reload(String language){
        this.lang = language;
        this.load();
    }
}
