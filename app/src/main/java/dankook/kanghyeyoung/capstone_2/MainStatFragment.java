package dankook.kanghyeyoung.capstone_2;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;

import static dankook.kanghyeyoung.capstone_2._COLOR.COLOR_TEXT_INT;
import static dankook.kanghyeyoung.finalproject._COLOR.COLOR_TEXT_INT;

public class MainStatFragment extends Fragment implements MainFragment {
    private static final String TAG = "MainStatFragment";

    PieChart mChart;
    Context mContext;
    MainActivity activity;
    SummaryView mSummaryView;
    RecyclerView mRecyclerView;
    StatListAdapter mAdapter;

    int mCurYear;
    int mCurMonth;
    int mSelectedYear;
    int mSelectedMonth;

    /* DatePicker 리스너 정의 */
    DatePickerDialog.OnDateSetListener mDialogListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(android.widget.DatePicker datePicker, int i, int i1, int i2) {
            activity.updateSelectedDate(i, i1);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        /* layout inflate 및 context, activity 얻기 */
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main_stat, container, false);
        mContext = rootView.getContext();
        activity = (MainActivity) getActivity();

        /* 현재 날짜 설정 */
        Calendar calendar = Calendar.getInstance();
        mCurYear = calendar.get(Calendar.YEAR);
        mCurMonth = calendar.get(Calendar.MONTH) + 1; // 캘린더의 month는 0이 1월, 1이 2월

        /* summaryView 설정  */
        mSummaryView = new SummaryView(mContext);
        FrameLayout frameLayout = rootView.findViewById(R.id.frameLayout);
        frameLayout.addView(mSummaryView);

        // 초기에는 선택된 날짜를 현재 날짜로 설정
        updateSelectedDate(mCurYear, mCurMonth);

        // summaryView의 날짜 설정 버튼에 onClickListener 등록
        mSummaryView.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 현재 선택된 년/월을 DatePicker 프래그먼트로 보내기 위한 번들 생성
                Bundle bundle = new Bundle();
                bundle.putInt("curYear", mCurYear);
                bundle.putInt("selectedYear", mSelectedYear);
                bundle.putInt("selectedMonth", mSelectedMonth);

                // datePickerDialog 생성 및 show
                YearMonthPickerDialog yearMonthPickerDialog = new YearMonthPickerDialog();
                yearMonthPickerDialog.setArguments(bundle);
                yearMonthPickerDialog.setDateSetListener(mDialogListener);
                yearMonthPickerDialog.show(getChildFragmentManager(), "datePicker");
            }
        });

        /* view 참조 */
        mChart = rootView.findViewById(R.id.pieChart);
        mRecyclerView = rootView.findViewById(R.id.recyclerView);

        /* 파이차트 모양 설정 */
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(5, 5, 5, 5);
        mChart.setDrawHoleEnabled(true);

        Legend legend = mChart.getLegend();
        legend.setEnabled(false);

        mChart.setEntryLabelColor(COLOR_TEXT_INT);
        mChart.setEntryLabelTextSize(18f);

        // gridView에 레이아웃 추가
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new StatListAdapter(mSelectedYear, mSelectedMonth);
        mRecyclerView.setAdapter(mAdapter);

        /* 데이터 추가 */
        updateSelectedDate(mSelectedYear, mSelectedMonth);

        return rootView;
    }

    /* 밑의 메소드 호출 */
    public void updateSelectedDate() {
        updateSelectedDate(mSelectedYear, mSelectedMonth);
    }

    /* 설정된 년/월 변경 및 pieChart, adapter 갱신 */
    public void updateSelectedDate(int year, int month) {

        mSelectedYear = year;
        mSelectedMonth = month;
        mSummaryView.mTextViewYear.setText(mSelectedYear + "년");
        mSummaryView.mTextViewMonth.setText(mSelectedMonth + "월");
        mSummaryView.showSummary(mSelectedYear, mSelectedMonth);

        /* 변동사항 업데이트 */
        ArrayList<Integer> sumOfCats = new ArrayList();
        for (int i = 0; i < Spec.getCount_cat_main(); i++) {
            sumOfCats.add(AccountBookDB.SumForCat(mSelectedYear, mSelectedMonth, i));
        }

        /* pieChart 데이터 업데이트 */
        if (mChart != null) {

            // pieEntry 추가
            ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
            for (int i = 0; i < Spec.getCount_cat_main(); i++) {
                int tempTotal = sumOfCats.get(i);

                // 합계금액이 0이 아닐때만 넣기
                if (tempTotal != 0) {
                    entries.add(new PieEntry(tempTotal, Spec.getCat_main_name(i)));
                }
            }

            // 파이차트 데이터셋 정의 -> 파이엔트리, 색, 여백 등 설정
            PieDataSet dataSet = new PieDataSet(entries, "카테고리별 소비 통계");
            dataSet.setSliceSpace(3f);
            dataSet.setSelectionShift(30f);
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

            dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

            // 파이차트에 데이터 추가
            PieData data = new PieData(dataSet);
            data.setValueTextSize(24f);
            data.setValueTextColor(COLOR_TEXT_INT);
            data.setValueFormatter(new PercentFormatter(mChart));
            mChart.setData(data);
            mChart.invalidate();
        }

        /* gridView 데이터 업데이트 */
        if (mAdapter != null) {

            // 카테고리별 합계금액 갖는 hashMap 만들고 업데이트
            mAdapter.clear();
            for (int i = 0; i < Spec.getCount_cat_main(); i++) {
                mAdapter.addItem(new SumOfCat(i, sumOfCats.get(i)));
            }
            mAdapter.notifyDataSetChanged();
        }
    }
}