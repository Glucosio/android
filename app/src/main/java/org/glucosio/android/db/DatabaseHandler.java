package org.glucosio.android.db;

import android.content.Context;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.Weeks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

public class DatabaseHandler {

    Context mContext;
    Realm realm;
    private static RealmConfiguration mRealmConfig;

    public DatabaseHandler(Context context) {
        this.mContext = context;

        if (mRealmConfig == null) {
            mRealmConfig = new RealmConfiguration.Builder(context)
                    .schemaVersion(2)
                    .migration(new Migration())
                    .build();
        }
        this.realm = Realm.getInstance(mRealmConfig); // Automatically run migration if needed
    }

    public void addUser(User user) {
        realm.beginTransaction();
        realm.copyToRealm(user);
        realm.commitTransaction();
    }

    public User getUser(long id) {
        return realm.where(User.class)
                .equalTo("id", id)
                .findFirst();
    }

    public void updateUser(User user) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(user);
        realm.commitTransaction();
    }

    public boolean addGlucoseReading(GlucoseReading reading) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(reading.getCreated());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        String id = "" + year+month+day+hours+minutes+reading.getReading();

        // Check for duplicates
        if (getGlucoseReadingById(Long.parseLong(id)) != null){
            return false;
        } else {
            realm.beginTransaction();
            reading.setId(Long.parseLong(id));
            realm.copyToRealm(reading);
            realm.commitTransaction();
            return true;
        }
    }

    public void deleteGlucoseReadings(GlucoseReading reading) {
        realm.beginTransaction();
        reading.removeFromRealm();
        realm.commitTransaction();
    }

    public ArrayList<GlucoseReading> getGlucoseReadings() {
        RealmResults<GlucoseReading> results =
                realm.where(GlucoseReading.class)
                        .findAllSorted("created", Sort.DESCENDING);
        ArrayList<GlucoseReading> readingList = new ArrayList<>();
        for (int i=0; i < results.size(); i++){
            readingList.add(results.get(i));
        }
        return readingList;
    }

    public ArrayList<GlucoseReading> getGlucoseReadings(Date from, Date to) {
        RealmResults<GlucoseReading> results =
                realm.where(GlucoseReading.class)
                .between("created", from, to)
                .findAllSorted("created", Sort.DESCENDING);
        ArrayList<GlucoseReading> readingList = new ArrayList<>();
        for (int i=0; i < results.size(); i++){
            readingList.add(results.get(i));
        }
        return readingList;
    }

    public GlucoseReading getGlucoseReading(long id) {
        return realm.where(GlucoseReading.class)
                .equalTo("id", id)
                .findFirst();
    }

    public ArrayList<Long> getGlucoseIdAsArray(){
        List<GlucoseReading> glucoseReading = getGlucoseReadings();
        ArrayList<Long> idArray = new ArrayList<Long>();
        int i;

        for (i = 0; i < glucoseReading.size(); i++){
            long id;
            GlucoseReading singleReading= glucoseReading.get(i);
            id = singleReading.getId();
            idArray.add(id);
        }

        return idArray;
    }

    public ArrayList<Integer> getGlucoseReadingAsArray(){
        List<GlucoseReading> glucoseReading = getGlucoseReadings();
        ArrayList<Integer> readingArray = new ArrayList<Integer>();
        int i;

        for (i = 0; i < glucoseReading.size(); i++){
            int reading;
            GlucoseReading singleReading= glucoseReading.get(i);
            reading = singleReading.getReading();
            readingArray.add(reading);
        }

        return readingArray;
    }

    public ArrayList<String> getGlucoseTypeAsArray(){
        List<GlucoseReading> glucoseReading = getGlucoseReadings();
        ArrayList<String> typeArray = new ArrayList<String>();
        int i;

        for (i = 0; i < glucoseReading.size(); i++){
            String reading;
            GlucoseReading singleReading= glucoseReading.get(i);
            reading = singleReading.getReading_type();
            typeArray.add(reading);
        }

        return typeArray;
    }

    public ArrayList<String> getGlucoseDateTimeAsArray(){
        List<GlucoseReading> glucoseReading = getGlucoseReadings();
        ArrayList<String> datetimeArray = new ArrayList<String>();
        int i;
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (i = 0; i < glucoseReading.size(); i++){
            String reading;
            GlucoseReading singleReading= glucoseReading.get(i);
            reading = inputFormat.format(singleReading.getCreated());
            datetimeArray.add(reading);
        }

        return datetimeArray;
    }

    public GlucoseReading getGlucoseReadingById(long id){
        return getGlucoseReading(id);
    }


/*    private ArrayList<Integer> getGlucoseReadingsForLastMonthAsArray(){
        Calendar calendar = Calendar.getInstance();
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String now = inputFormat.format(calendar.getTime());
        calendar.add(Calendar.MONTH, -1);
        String oneMonthAgo = inputFormat.format(calendar.getTime());


        String[] parameters = new String[] { oneMonthAgo, now } ;
        String[] columns = new String[] { "reading" };
        String whereString = "created_at between ? and ?";

        List<GlucoseReading> gReadings;
        ArrayList<Integer> readings = new ArrayList<Integer>();

        gReadings = GlucoseReading.getGlucoseReadings(whereString);
        int i;
        for (i=0; i < gReadings.size(); i++){
            readings.add(gReadings.get(i).getReading());
        }

        return readings;
    }

    public Integer getAverageGlucoseReadingForLastMonth() {
        ArrayList<Integer> readings = getGlucoseReadingsForLastMonthAsArray();
        int sum = 0;
        int numberOfReadings = readings.size();
        for (int i=0; i < numberOfReadings; i++) {
            sum += readings.get(i);
        }
        if (numberOfReadings > 0){
            return Math.round(sum / numberOfReadings);
        } else {
            return 0;
        }
    }*/

    public List<Integer> getAverageGlucoseReadingsByWeek(){
        JodaTimeAndroid.init(mContext);

        DateTime maxDateTime = new DateTime(realm.where(GlucoseReading.class).maximumDate("created").getTime());
        DateTime minDateTime = new DateTime(realm.where(GlucoseReading.class).minimumDate("created").getTime());

        DateTime currentDateTime = minDateTime;
        DateTime newDateTime = minDateTime;

        ArrayList<Integer> averageReadings = new ArrayList<Integer>();

	// The number of weeks is at least 1 since we do have average for the current week even if incomplete
        int weeksNumber = Weeks.weeksBetween(minDateTime, maxDateTime).getWeeks() + 1;

        for (int i=0; i<weeksNumber; i++) {
            newDateTime = currentDateTime.plusWeeks(1);
            RealmResults<GlucoseReading> readings = realm.where(GlucoseReading.class)
                    .between("created", currentDateTime.toDate(), newDateTime.toDate())
                    .findAll();
            averageReadings.add(((int)readings.average("reading")));
            currentDateTime = newDateTime;
        }
        return averageReadings;
    }

    public List<String> getGlucoseDatetimesByWeek(){
        JodaTimeAndroid.init(mContext);

        DateTime maxDateTime = new DateTime(realm.where(GlucoseReading.class).maximumDate("created").getTime());
        DateTime minDateTime = new DateTime(realm.where(GlucoseReading.class).minimumDate("created").getTime());

        DateTime currentDateTime = minDateTime;
        DateTime newDateTime = minDateTime;

        ArrayList<String> finalWeeks = new ArrayList<String>();

	// The number of weeks is at least 1 since we do have average for the current week even if incomplete
        int weeksNumber = Weeks.weeksBetween(minDateTime, maxDateTime).getWeeks() + 1;

        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (int i=0; i<weeksNumber; i++) {
            newDateTime = currentDateTime.plusWeeks(1);
            finalWeeks.add(inputFormat.format(newDateTime.toDate()));
            currentDateTime = newDateTime;
        }
        return finalWeeks;
    }

    public List<Integer> getAverageGlucoseReadingsByMonth(){
        JodaTimeAndroid.init(mContext);

        DateTime maxDateTime = new DateTime(realm.where(GlucoseReading.class).maximumDate("created").getTime());
        DateTime minDateTime = new DateTime(realm.where(GlucoseReading.class).minimumDate("created").getTime());

        DateTime currentDateTime = minDateTime;
        DateTime newDateTime = minDateTime;

        ArrayList<Integer> averageReadings = new ArrayList<Integer>();

        // The number of months is at least 1 since we do have average for the current week even if incomplete
        int monthsNumber = Months.monthsBetween(minDateTime, maxDateTime).getMonths() + 1;

        for (int i=0; i<monthsNumber; i++) {
            newDateTime = currentDateTime.plusMonths(1);
            RealmResults<GlucoseReading> readings = realm.where(GlucoseReading.class)
                    .between("created", currentDateTime.toDate(), newDateTime.toDate())
                    .findAll();
            averageReadings.add(((int)readings.average("reading")));
            currentDateTime = newDateTime;
        }
        return averageReadings;
    }

    public List<String> getGlucoseDatetimesByMonth(){
        JodaTimeAndroid.init(mContext);

        DateTime maxDateTime = new DateTime(realm.where(GlucoseReading.class).maximumDate("created").getTime());
        DateTime minDateTime = new DateTime(realm.where(GlucoseReading.class).minimumDate("created").getTime());

        DateTime currentDateTime = minDateTime;
        DateTime newDateTime = minDateTime;

        ArrayList<String> finalMonths = new ArrayList<String>();

        // The number of months is at least 1 since we do have average for the current week even if incomplete
        int monthsNumber = Months.monthsBetween(minDateTime, maxDateTime).getMonths() + 1;

        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (int i=0; i<monthsNumber; i++) {
            newDateTime = currentDateTime.plusMonths(1);
            finalMonths.add(inputFormat.format(newDateTime.toDate()));
            currentDateTime = newDateTime;
        }
        return finalMonths;
    }

    public void addHB1ACReading(HB1ACReading reading) {
        realm.beginTransaction();
        reading.setId(getNextKey("hb1ac"));
        realm.copyToRealm(reading);
        realm.commitTransaction();
    }

    public void deleteHB1ACReadingReading(HB1ACReading reading) {
        realm.beginTransaction();
        reading.removeFromRealm();
        realm.commitTransaction();
    }

    public HB1ACReading getHB1ACReading(long id) {
        return realm.where(HB1ACReading.class)
                .equalTo("id", id)
                .findFirst();
    }


    public ArrayList<HB1ACReading> getHB1ACReadings() {
        RealmResults<HB1ACReading> results =
                realm.where(HB1ACReading.class)
                        .findAllSorted("created", Sort.DESCENDING);
        ArrayList<HB1ACReading> readingList = new ArrayList<>();
        for (int i=0; i < results.size(); i++){
            readingList.add(results.get(i));
        }
        return readingList;
    }

    public ArrayList<Long> getHB1ACIdAsArray(){
        List<HB1ACReading> readings = getHB1ACReadings();
        ArrayList<Long> idArray = new ArrayList<Long>();
        int i;

        for (i = 0; i < readings.size(); i++){
            long id;
            HB1ACReading singleReading= readings.get(i);
            id = singleReading.getId();
            idArray.add(id);
        }

        return idArray;
    }

    public ArrayList<Double> getHB1ACReadingAsArray(){
        List<HB1ACReading> readings = getHB1ACReadings();
        ArrayList<Double> readingArray = new ArrayList<Double>();
        int i;

        for (i = 0; i < readings.size(); i++){
            double reading;
            HB1ACReading singleReading= readings.get(i);
            reading = singleReading.getReading();
            readingArray.add(reading);
        }

        return readingArray;
    }

    public ArrayList<String> getHB1ACDateTimeAsArray(){
        List<HB1ACReading> readings = getHB1ACReadings();
        ArrayList<String> datetimeArray = new ArrayList<String>();
        int i;
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (i = 0; i < readings.size(); i++){
            String reading;
            HB1ACReading singleReading= readings.get(i);
            reading = inputFormat.format(singleReading.getCreated());
            datetimeArray.add(reading);
        }

        return datetimeArray;
    }

    public void addKetoneReading(KetoneReading reading) {
        realm.beginTransaction();
        reading.setId(getNextKey("ketone"));
        realm.copyToRealm(reading);
        realm.commitTransaction();
    }

    public KetoneReading getKetoneReading(long id) {
        return realm.where(KetoneReading.class)
                .equalTo("id", id)
                .findFirst();
    }

    public void deleteKetoneReading(KetoneReading reading) {
        realm.beginTransaction();
        reading.removeFromRealm();
        realm.commitTransaction();
    }

    public ArrayList<KetoneReading> getKetoneReadings() {
        RealmResults<KetoneReading> results =
                realm.where(KetoneReading.class)
                        .findAllSorted("created", Sort.DESCENDING);
        ArrayList<KetoneReading> readingList = new ArrayList<>();
        for (int i=0; i < results.size(); i++){
            readingList.add(results.get(i));
        }
        return readingList;
    }

    public ArrayList<Long> getKetoneIdAsArray(){
        List<KetoneReading> readings = getKetoneReadings();
        ArrayList<Long> idArray = new ArrayList<Long>();
        int i;

        for (i = 0; i < readings.size(); i++){
            long id;
            KetoneReading singleReading= readings.get(i);
            id = singleReading.getId();
            idArray.add(id);
        }

        return idArray;
    }

    public ArrayList<Double> getKetoneReadingAsArray(){
        List<KetoneReading> readings = getKetoneReadings();
        ArrayList<Double> readingArray = new ArrayList<Double>();
        int i;

        for (i = 0; i < readings.size(); i++){
            double reading;
            KetoneReading singleReading = readings.get(i);
            reading = singleReading.getReading();
            readingArray.add(reading);
        }

        return readingArray;
    }

    public ArrayList<String> getKetoneDateTimeAsArray(){
        List<KetoneReading> readings = getKetoneReadings();
        ArrayList<String> datetimeArray = new ArrayList<String>();
        int i;
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (i = 0; i < readings.size(); i++){
            String reading;
            KetoneReading singleReading= readings.get(i);
            reading = inputFormat.format(singleReading.getCreated());
            datetimeArray.add(reading);
        }

        return datetimeArray;
    }

    public void addPressureReading(PressureReading reading) {
        realm.beginTransaction();
        reading.setId(getNextKey("pressure"));
        realm.copyToRealm(reading);
        realm.commitTransaction();
    }

    public PressureReading getPressureReading(long id) {
        return realm.where(PressureReading.class)
                .equalTo("id", id)
                .findFirst();
    }

    public void deletePressureReading(PressureReading reading) {
        realm.beginTransaction();
        reading.removeFromRealm();
        realm.commitTransaction();
    }

    public ArrayList<PressureReading> getPressureReadings() {
        RealmResults<PressureReading> results =
                realm.where(PressureReading.class)
                        .findAllSorted("created", Sort.DESCENDING);
        ArrayList<PressureReading> readingList = new ArrayList<>();
        for (int i=0; i < results.size(); i++){
            readingList.add(results.get(i));
        }
        return readingList;
    }

    public ArrayList<Long> getPressureIdAsArray(){
        List<PressureReading> readings = getPressureReadings();
        ArrayList<Long> idArray = new ArrayList<Long>();
        int i;

        for (i = 0; i < readings.size(); i++){
            long id;
            PressureReading singleReading= readings.get(i);
            id = singleReading.getId();
            idArray.add(id);
        }

        return idArray;
    }

    public ArrayList<Integer> getMinPressureReadingAsArray(){
        List<PressureReading> readings = getPressureReadings();
        ArrayList<Integer> readingArray = new ArrayList<Integer>();
        int i;

        for (i = 0; i < readings.size(); i++){
            int reading;
            PressureReading singleReading= readings.get(i);
            reading = singleReading.getMinReading();
            readingArray.add(reading);
        }

        return readingArray;
    }

    public ArrayList<Integer> getMaxPressureReadingAsArray(){
        List<PressureReading> readings = getPressureReadings();
        ArrayList<Integer> readingArray = new ArrayList<Integer>();
        int i;

        for (i = 0; i < readings.size(); i++){
            int reading;
            PressureReading singleReading= readings.get(i);
            reading = singleReading.getMaxReading();
            readingArray.add(reading);
        }

        return readingArray;
    }

    public ArrayList<String> getPressureDateTimeAsArray(){
        List<PressureReading> readings = getPressureReadings();
        ArrayList<String> datetimeArray = new ArrayList<String>();
        int i;
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (i = 0; i < readings.size(); i++){
            String reading;
            PressureReading singleReading= readings.get(i);
            reading = inputFormat.format(singleReading.getCreated());
            datetimeArray.add(reading);
        }

        return datetimeArray;
    }

    public void addWeightReading(WeightReading reading) {
        realm.beginTransaction();
        reading.setId(getNextKey("weight"));
        realm.copyToRealm(reading);
        realm.commitTransaction();
    }

    public WeightReading getWeightReading(long id) {
        return realm.where(WeightReading.class)
                .equalTo("id", id)
                .findFirst();
    }

    public void deleteWeightReading(WeightReading reading) {
        realm.beginTransaction();
        reading.removeFromRealm();
        realm.commitTransaction();
    }

    public ArrayList<WeightReading> getWeightReadings() {
        RealmResults<WeightReading> results =
                realm.where(WeightReading.class)
                        .findAllSorted("created", Sort.DESCENDING);
        ArrayList<WeightReading> readingList = new ArrayList<>();
        for (int i=0; i < results.size(); i++){
            readingList.add(results.get(i));
        }
        return readingList;
    }

    public ArrayList<Long> getWeightIdAsArray(){
        List<WeightReading> readings = getWeightReadings();
        ArrayList<Long> idArray = new ArrayList<Long>();
        int i;

        for (i = 0; i < readings.size(); i++){
            long id;
            WeightReading singleReading= readings.get(i);
            id = singleReading.getId();
            idArray.add(id);
        }

        return idArray;
    }

    public ArrayList<Integer> getWeightReadingAsArray(){
        List<WeightReading> readings = getWeightReadings();
        ArrayList<Integer> readingArray = new ArrayList<Integer>();
        int i;

        for (i = 0; i < readings.size(); i++){
            int reading;
            WeightReading singleReading= readings.get(i);
            reading = singleReading.getReading();
            readingArray.add(reading);
        }

        return readingArray;
    }

    public ArrayList<String> getWeightReadingDateTimeAsArray(){
        List<WeightReading> readings = getWeightReadings();
        ArrayList<String> datetimeArray = new ArrayList<String>();
        int i;
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (i = 0; i < readings.size(); i++){
            String reading;
            WeightReading singleReading= readings.get(i);
            reading = inputFormat.format(singleReading.getCreated());
            datetimeArray.add(reading);
        }

        return datetimeArray;
    }

    public void addCholesterolReading(CholesterolReading reading) {
        realm.beginTransaction();
        reading.setId(getNextKey("cholesterol"));
        realm.copyToRealm(reading);
        realm.commitTransaction();
    }

    public CholesterolReading getCholesterolReading(long id) {
        return realm.where(CholesterolReading.class)
                .equalTo("id", id)
                .findFirst();
    }

    public void deleteCholesterolReading(CholesterolReading reading) {
        realm.beginTransaction();
        reading.removeFromRealm();
        realm.commitTransaction();
    }

    public ArrayList<CholesterolReading> getCholesterolReadings() {
        RealmResults<CholesterolReading> results =
                realm.where(CholesterolReading.class)
                        .findAllSorted("created", Sort.DESCENDING);
        ArrayList<CholesterolReading> readingList = new ArrayList<>();
        for (int i=0; i < results.size(); i++){
            readingList.add(results.get(i));
        }
        return readingList;
    }

    public ArrayList<Long> getCholesterolIdAsArray(){
        List<CholesterolReading> readings = getCholesterolReadings();
        ArrayList<Long> idArray = new ArrayList<Long>();
        int i;

        for (i = 0; i < readings.size(); i++){
            long id;
            CholesterolReading singleReading= readings.get(i);
            id = singleReading.getId();
            idArray.add(id);
        }

        return idArray;
    }

    public ArrayList<Integer> getHDLCholesterolReadingAsArray(){
        List<CholesterolReading> readings = getCholesterolReadings();
        ArrayList<Integer> readingArray = new ArrayList<Integer>();
        int i;

        for (i = 0; i < readings.size(); i++){
            int reading;
            CholesterolReading singleReading= readings.get(i);
            reading = singleReading.getHDLReading();
            readingArray.add(reading);
        }

        return readingArray;
    }

    public ArrayList<Integer> getLDLCholesterolReadingAsArray(){
        List<CholesterolReading> readings = getCholesterolReadings();
        ArrayList<Integer> readingArray = new ArrayList<Integer>();
        int i;

        for (i = 0; i < readings.size(); i++){
            int reading;
            CholesterolReading singleReading= readings.get(i);
            reading = singleReading.getLDLReading();
            readingArray.add(reading);
        }

        return readingArray;
    }

    public ArrayList<Integer> getTotalCholesterolReadingAsArray(){
        List<CholesterolReading> readings = getCholesterolReadings();
        ArrayList<Integer> readingArray = new ArrayList<Integer>();
        int i;

        for (i = 0; i < readings.size(); i++){
            int reading;
            CholesterolReading singleReading= readings.get(i);
            reading = singleReading.getTotalReading();
            readingArray.add(reading);
        }

        return readingArray;
    }

    public ArrayList<String> getCholesterolDateTimeAsArray(){
        List<CholesterolReading> readings = getCholesterolReadings();
        ArrayList<String> datetimeArray = new ArrayList<String>();
        int i;
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (i = 0; i < readings.size(); i++){
            String reading;
            CholesterolReading singleReading= readings.get(i);
            reading = inputFormat.format(singleReading.getCreated());
            datetimeArray.add(reading);
        }

        return datetimeArray;
    }

    public long getNextKey(String where) {
        Number maxId = null;
        switch (where) {
            case "glucose":
                maxId = realm.where(GlucoseReading.class)
                        .max("id");
                break;
            case "weight":
                maxId = realm.where(WeightReading.class)
                        .max("id");
                break;
            case "hb1ac":
                maxId = realm.where(HB1ACReading.class)
                        .max("id");
                break;
            case "pressure":
                maxId = realm.where(PressureReading.class)
                        .max("id");
                break;
            case "ketone":
                maxId = realm.where(KetoneReading.class)
                        .max("id");
                break;
            case "cholesterol":
                maxId = realm.where(CholesterolReading.class)
                        .max("id");
                break;
        }
        if (maxId == null){
            return 0;
        } else {
            return Long.parseLong(maxId.toString())+1;
        }
    }
}
