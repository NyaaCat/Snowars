package cat.nyaa.musicapi.api;

import org.bukkit.Sound;

public interface INote {
    float getVolume();
    float getPitch();
    Sound getSound();

}
