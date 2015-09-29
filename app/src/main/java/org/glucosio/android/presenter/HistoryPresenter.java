package org.glucosio.android.presenter;

import org.glucosio.android.activity.MainActivity;
import org.glucosio.android.db.DatabaseHandler;
import org.glucosio.android.db.GlucoseReading;
import org.glucosio.android.fragment.HistoryFragment;
import org.glucosio.android.tools.FormatDateTime;
import org.glucosio.android.tools.ReadingTools;

import java.text.DateFormat;
import java.util.ArrayList;

public class HistoryPresenter {

    DatabaseHandler dB;
    private ArrayList<Long> id;
    private ArrayList<Integer> reading;
    private ArrayList <String> type;
    private ArrayList<String> datetime;
    HistoryFragment fragment;

    public HistoryPresenter(HistoryFragment historyFragment) {
        this.fragment = historyFragment;
        dB = new DatabaseHandler();
    }

    public boolean isdbEmpty(){
        return dB.getGlucoseReadings().size() == 0;
    }

    public void loadDatabase(){
        this.id = dB.getGlucoseIdAsArray();
        this.reading = dB.getGlucoseReadingAsArray();
        this.type = dB.getGlucoseTypeAsArray();
        this.datetime = dB.getGlucoseDateTimeAsArray();
    }


    public String convertDate(String date){
        return fragment.convertDate(date);
    }

    public void onDeleteClicked(int idToDelete){
        removeReadingFromDb(dB.getGlucoseReadingById(idToDelete));
        fragment.notifyAdapter();
        fragment.updateToolbarBehaviour();
    }

    private void removeReadingFromDb(GlucoseReading gReading) {
        dB.deleteGlucoseReadings(gReading);
        fragment.reloadFragmentAdapter();
        loadDatabase();
    }

    // Getters
    public ArrayList<Long> getId() {
        return id;
    }

    public ArrayList<Integer> getReading() {
        return reading;
    }

    public ArrayList<String> getType() {
        return type;
    }

    public ArrayList<String> getDatetime() {
        return datetime;
    }

    public int getReadingsNumber(){
        return reading.size();
    }
}
