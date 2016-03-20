package org.glucosio.android.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.glucosio.android.R;
import org.glucosio.android.presenter.OverviewPresenter;
import org.glucosio.android.tools.FormatDateTime;
import org.glucosio.android.tools.GlucoseConverter;
import org.glucosio.android.tools.GlucoseRanges;
import org.glucosio.android.tools.TipsManager;

import java.util.ArrayList;
import java.util.Collections;

public class OverviewFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    private LineChart chart;
    private TextView lastReadingTextView;
    private TextView lastDateTextView;
    private TextView trendTextView;
    private TextView tipTextView;
    private TextView HB1ACTextView;
    private TextView HB1ACDateTextView;
    private ImageButton graphExport;
    private Spinner graphSpinnerRange;
    private Spinner graphSpinnerMetric;
    private OverviewPresenter presenter;
    private View mFragmentView;

    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();


        return fragment;
    }

    public OverviewFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        presenter = new OverviewPresenter(this);
        if (!presenter.isdbEmpty()) {
            presenter.loadDatabase();
        }

        mFragmentView = inflater.inflate(R.layout.fragment_overview, container, false);

        chart = (LineChart) mFragmentView.findViewById(R.id.chart);
        disableTouchTheft(chart);
        Legend legend = chart.getLegend();

        if (!presenter.isdbEmpty()) {
            Collections.reverse(presenter.getGlucoseReading());
            Collections.reverse(presenter.getGlucoseDatetime());
            Collections.reverse(presenter.getGlucoseType());
        }

        lastReadingTextView = (TextView) mFragmentView.findViewById(R.id.item_history_reading);
        lastDateTextView = (TextView) mFragmentView.findViewById(R.id.fragment_overview_last_date);
        trendTextView = (TextView) mFragmentView.findViewById(R.id.item_history_trend);
        tipTextView = (TextView) mFragmentView.findViewById(R.id.random_tip_textview);
        graphSpinnerRange = (Spinner) mFragmentView.findViewById(R.id.chart_spinner_range);
        graphSpinnerMetric = (Spinner) mFragmentView.findViewById(R.id.chart_spinner_metrics);
        graphExport = (ImageButton) mFragmentView.findViewById(R.id.fragment_overview_graph_export);
        HB1ACTextView = (TextView) mFragmentView.findViewById(R.id.fragment_overview_hb1ac);
        HB1ACDateTextView = (TextView) mFragmentView.findViewById(R.id.fragment_overview_hb1ac_date);

        // Set array and adapter for graphSpinnerRange
        String[] selectorRangeArray = getActivity().getResources().getStringArray(R.array.fragment_overview_selector_range);
        String[] selectorMetricArray = getActivity().getResources().getStringArray(R.array.fragment_overview_selector_metric);
        ArrayAdapter<String> dataRangeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, selectorRangeArray);
        ArrayAdapter<String> dataMetricAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, selectorMetricArray);
        dataRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataMetricAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        graphSpinnerRange.setAdapter(dataRangeAdapter);
        graphSpinnerMetric.setAdapter(dataMetricAdapter);

        graphSpinnerRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!presenter.isdbEmpty()) {
                    setData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        graphSpinnerRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!presenter.isdbEmpty()) {
                    setData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(getResources().getColor(R.color.glucosio_text_light));
        xAxis.setAvoidFirstLastClipping(true);

      /*  LimitLine ll1 = new LimitLine(130f, "High");
        ll1.setLineWidth(1f);
        ll1.setLineColor(getResources().getColor(R.color.glucosio_gray_light));
        ll1.setTextColor(getResources().getColor(R.color.glucosio_text));

        LimitLine ll2 = new LimitLine(70f, "Low");
        ll2.setLineWidth(1f);
        ll2.setLineColor(getResources().getColor(R.color.glucosio_gray_light));
        ll2.setTextColor(getResources().getColor(R.color.glucosio_text));

        LimitLine ll3 = new LimitLine(200f, "Hyper");
        ll3.setLineWidth(1f);
        ll3.enableDashedLine(10, 10, 10);
        ll3.setLineColor(getResources().getColor(R.color.glucosio_gray_light));
        ll3.setTextColor(getResources().getColor(R.color.glucosio_text));

        LimitLine ll4 = new LimitLine(50f, "Hypo");
        ll4.setLineWidth(1f);
        ll4.enableDashedLine(10, 10, 10);
        ll4.setLineColor(getResources().getColor(R.color.glucosio_gray_light));
        ll4.setTextColor(getResources().getColor(R.color.glucosio_text));*/

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
/*        leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);
        leftAxis.addLimitLine(ll3);
        leftAxis.addLimitLine(ll4);*/
        leftAxis.setTextColor(getResources().getColor(R.color.glucosio_text_light));
        leftAxis.setStartAtZero(false);
        //leftAxis.setYOffset(20f);
        leftAxis.disableGridDashedLine();
        leftAxis.setDrawGridLines(false);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        chart.getAxisRight().setEnabled(false);
        chart.setBackgroundColor(Color.parseColor("#FFFFFF"));
        chart.setDescription("");
        chart.setGridBackgroundColor(Color.parseColor("#FFFFFF"));
        if (!presenter.isdbEmpty()) {
            setData();
        }
        legend.setEnabled(false);


        graphExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // If we don't have permission, ask the user

                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                    Snackbar.make(mFragmentView, getString(R.string.fragment_overview_permission_storage), Snackbar.LENGTH_SHORT).show();
                } else {
                    // else save the image to gallery
                    exportGraphToGallery();
                }
            }
        });

        loadLastReading();
        loadHB1AC();
/*
        loadGlucoseTrend();
*/
        loadRandomTip();

        return mFragmentView;
    }

    private void exportGraphToGallery() {
        long timestamp = System.currentTimeMillis()/1000;
        boolean saved = chart.saveToGallery("glucosio_" + timestamp , 50);
        if (saved) {
            Snackbar.make(mFragmentView, R.string.fragment_overview_graph_export_true, Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(mFragmentView, R.string.fragment_overview_graph_export_false, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void setData() {
        // TODO: Refactor all this mess ASAP! -- @paolorotolo
        // int metricSpinnerPosition = graphSpinnerMetric.getSelectedItemPosition();
        // if (metricSpinnerPosition == 0) {
        ArrayList<String> xVals = new ArrayList<String>();


        if (graphSpinnerRange.getSelectedItemPosition() == 0) {
            // Day view
            for (int i = 0; i < presenter.getGlucoseDatetime().size(); i++) {
                String date = presenter.convertDate(presenter.getGlucoseDatetime().get(i));
                    xVals.add(date + "");
            }
        } else if (graphSpinnerRange.getSelectedItemPosition() == 1){
            // Week view
            for (int i = 0; i < presenter.getGlucoseReadingsWeek().size(); i++) {
                String date = presenter.convertDate(presenter.getGlucoseDatetimeWeek().get(i));
                xVals.add(date + "");
            }
        } else {
            // Month view
            for (int i = 0; i < presenter.getGlucoseReadingsMonth().size(); i++) {
                String date = presenter.convertDateToMonth(presenter.getGlucoseDatetimeMonth().get(i));
                xVals.add(date + "");
            }
        }

        GlucoseConverter converter = new GlucoseConverter();
        GlucoseRanges ranges = new GlucoseRanges(getActivity().getApplicationContext());


        ArrayList<Entry> yValsLow = new ArrayList<Entry>();
        ArrayList<Entry> yValsNormal = new ArrayList<Entry>();
        ArrayList<Entry> yValsHigh = new ArrayList<Entry>();


        if (graphSpinnerRange.getSelectedItemPosition() == 0) {
                // Day view
                for (int i = 0; i < presenter.getGlucoseReading().size(); i++) {
                    if (presenter.getUnitMeasuerement().equals("mg/dL")) {
                        float val = Float.parseFloat(presenter.getGlucoseReading().get(i).toString());
                        String range = ranges.colorFromReading(presenter.getGlucoseReading().get(i));

                        if (range.equals("purple") || range.equals("blue")){
                            // low
                            yValsLow.add(new Entry(val, i));
                        } else if (range.equals("red") || range.equals("orange")){
                            // high
                            yValsHigh.add(new Entry(val, i));
                        } else {
                            // normal
                            yValsNormal.add(new Entry(val, i));
                        }
                    } else {
                        double val = converter.glucoseToMmolL(Double.parseDouble(presenter.getGlucoseReading().get(i).toString()));
                        float converted = (float) val;
                        String range = ranges.colorFromReading(presenter.getGlucoseReading().get(i));

                        if (range.equals("purple") || range.equals("blue")){
                            // low
                            yValsLow.add(new Entry(converted, i));
                        } else if (range.equals("red") || range.equals("orange")){
                            // high
                            yValsHigh.add(new Entry(converted, i));
                        } else {
                            // normal
                            yValsNormal.add(new Entry(converted, i));
                        }
                    }
                }
            } else if (graphSpinnerRange.getSelectedItemPosition() == 1){
                // Week view
                for (int i = 0; i < presenter.getGlucoseReadingsWeek().size(); i++) {
                    if (presenter.getUnitMeasuerement().equals("mg/dL")) {
                        float val = Float.parseFloat(presenter.getGlucoseReadingsWeek().get(i)+"");
                        String range = ranges.colorFromReading(presenter.getGlucoseReadingsWeek().get(i));

                        if (range.equals("purple") || range.equals("blue")){
                            // low
                            yValsLow.add(new Entry(val, i));
                        } else if (range.equals("red") || range.equals("orange")){
                            // high
                            yValsHigh.add(new Entry(val, i));
                        } else {
                            // normal
                            yValsNormal.add(new Entry(val, i));
                        }

                    } else {
                        double val = converter.glucoseToMmolL(Double.parseDouble(presenter.getGlucoseReadingsWeek().get(i)+""));
                        float converted = (float) val;
                        String range = ranges.colorFromReading(presenter.getGlucoseReadingsWeek().get(i));

                        if (range.equals("purple") && range.equals("blue")){
                            // low
                            yValsLow.add(new Entry(converted, i));
                        } else if (range.equals("red") && range.equals("orange")){
                            // high
                            yValsHigh.add(new Entry(converted, i));
                        } else {
                            // normal
                            yValsNormal.add(new Entry(converted, i));
                        }                    }
                }
            } else {
                // Month view
                for (int i = 0; i < presenter.getGlucoseReadingsMonth().size(); i++) {
                    if (presenter.getUnitMeasuerement().equals("mg/dL")) {
                        float val = Float.parseFloat(presenter.getGlucoseReadingsMonth().get(i)+"");
                        String range = ranges.colorFromReading(presenter.getGlucoseReadingsMonth().get(i));

                        if (range.equals("purple") && range.equals("blue")){
                            // low
                            yValsLow.add(new Entry(val, i));
                        } else if (range.equals("red") && range.equals("orange")){
                            // high
                            yValsHigh.add(new Entry(val, i));
                        } else {
                            // normal
                            yValsNormal.add(new Entry(val, i));
                        }
                    } else {
                        double val = converter.glucoseToMmolL(Double.parseDouble(presenter.getGlucoseReadingsMonth().get(i)+""));
                        float converted = (float) val;
                        String range = ranges.colorFromReading(presenter.getGlucoseReadingsWeek().get(i));

                        if (range.equals("purple") && range.equals("blue")){
                            // low
                            yValsLow.add(new Entry(converted, i));
                        } else if (range.equals("red") && range.equals("orange")){
                            // high
                            yValsHigh.add(new Entry(converted, i));
                        } else {
                            // normal
                            yValsNormal.add(new Entry(converted, i));
                        }
                    }
                }
            }
        /*} else if (metricSpinnerPosition == 1){
            // A1C
            ArrayList<String> xVals = new ArrayList<String>();

            for (int i = 0; i < presenter.getGlucoseDatetime().size(); i++) {
                String date = presenter.convertDateTime(presenter.getGlucoseDatetime().get(i));
                xVals.add(date + "");
            }

            ArrayList<Entry> yVals = new ArrayList<Entry>();


            for (int i = 0; i < presenter.getGlucoseReading().size(); i++) {
                float val = Float.parseFloat(presenter.getGlucoseReading().get(i).toString());
                yVals.add(new Entry(val, i));
            }

        } else if (metricSpinnerPosition == 2){
            // Cholesterol

        } else if (metricSpinnerPosition == 3){
            // Pressure

        } else if (metricSpinnerPosition == 4){
            // Ketones

        } else {
            // Weight
        }*/

//        set1.setDrawFilled(true);
        // set1.setShader(new LinearGradient(0, 0, 0, mChart.getHeight(),
        // Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));I

        // add the datasets
        ArrayList<ILineDataSet> dataSet = new ArrayList<ILineDataSet>();

        dataSet.add(createLineDataSet(yValsLow, getResources().getColor(R.color.glucosio_reading_low), "low"));
        dataSet.add(createLineDataSet(yValsNormal, getResources().getColor(R.color.glucosio_reading_ok), "normal"));
        dataSet.add(createLineDataSet(yValsHigh, getResources().getColor(R.color.glucosio_reading_high), "high"));


        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSet);

        // set data
        chart.setData(data);
        chart.setPinchZoom(true);
        chart.setHardwareAccelerationEnabled(true);
        chart.animateY(1000, Easing.EasingOption.EaseOutCubic);
        chart.invalidate();
        chart.notifyDataSetChanged();
        chart.fitScreen();
        chart.setVisibleXRangeMaximum(20);
        chart.moveViewToX(data.getXValCount());
    }

    private LineDataSet createLineDataSet(ArrayList<Entry> yVals, int color, String label) {
        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, label);
        set1.setColor(color);
        set1.setDrawFilled(false);
        set1.setLineWidth(1f);
        set1.setCircleColor(color);
        set1.setCircleSize(4f);
        set1.setDrawCircleHole(true);
        set1.disableDashedLine();
        set1.setDrawCubic(true);
        set1.setCubicIntensity(0.2f);
        set1.setValueTextSize(0);
        set1.setValueTextColor(Color.parseColor("#FFFFFF"));

        return  set1;
    }

    private void loadHB1AC(){
        if (!presenter.isdbEmpty()){
            HB1ACTextView.setText(presenter.getHB1AC());
            HB1ACDateTextView.setText(presenter.getH1ACMonth());
            if (HB1ACDateTextView.getText().equals(" ")){
                HB1ACDateTextView.setVisibility(View.GONE);
            }
        }
    }

    private void loadLastReading(){
        if (!presenter.isdbEmpty()) {
            if (presenter.getUnitMeasuerement().equals("mg/dL")) {
                lastReadingTextView.setText(presenter.getLastReading() + " mg/dL");
            } else {
                GlucoseConverter converter = new GlucoseConverter();
                lastReadingTextView.setText(converter.glucoseToMmolL(Double.parseDouble(presenter.getLastReading().toString())) + " mmol/L");
            }

            FormatDateTime dateTime = new FormatDateTime(getActivity().getApplicationContext());

            lastDateTextView.setText(dateTime.convertDateTime(presenter.getLastDateTime()));
            GlucoseRanges ranges = new GlucoseRanges(getActivity().getApplicationContext());
            String color = ranges.colorFromReading(Integer.parseInt(presenter.getLastReading()));
            lastReadingTextView.setTextColor(ranges.stringToColor(color));
        }
    }

/*    private void loadGlucoseTrend(){
        if (!presenter.isdbEmpty()) {
            trendTextView.setText(presenter.getGlucoseTrend() + "");
        }
    }*/

    private void loadRandomTip(){
        TipsManager tipsManager = new TipsManager(getActivity().getApplicationContext(), presenter.getUserAge());
        tipTextView.setText(presenter.getRandomTip(tipsManager));
    }

    public String convertDate(String date){
        FormatDateTime dateTime = new FormatDateTime(getActivity().getApplicationContext());
        return dateTime.convertDateTime(date);
    }

    public String convertDateToMonth(String date){
        FormatDateTime dateTime = new FormatDateTime((getActivity().getApplication()));
        return dateTime.convertDateToMonthOverview(date);
    }

    public static void disableTouchTheft(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    exportGraphToGallery();
                } else {
                    Snackbar.make(mFragmentView, R.string.fragment_overview_permission_storage, Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }
}