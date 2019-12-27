package cat.nyaa.musicapi.api;

import java.util.Collection;

public interface IMusicSheet {
    String addTrack(Collection<ISheetRecord> sheetRecord);

    boolean addTrack(String name, Collection<ISheetRecord> sheetRecord);

    Collection<ISheetRecord> getNotesForTick(int tick);

    int getLength();
}
