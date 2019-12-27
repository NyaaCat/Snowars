package cat.nyaa.musicapi.sheet;

import cat.nyaa.musicapi.api.INote;
import cat.nyaa.musicapi.api.ISheetRecord;

public class SheetRecord implements ISheetRecord {
    int tick;
    INote note;

    public SheetRecord(int tick, INote note) {
        this.tick = tick;
        this.note = note;
    }

    @Override
    public int getTick() {
        return tick;
    }

    @Override
    public INote getNote() {
        return note;
    }
}
