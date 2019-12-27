package cat.nyaa.musicapi.api;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface MusicApi {
    IMusicTask startPlaying(IMusicSheet musicSheet, Location location);
    IMusicTask startPlaying(IMusicSheet musicSheet, Entity entity);
}
