package dankook.kanghyeyoung.capstone_2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import static dankook.kanghyeyoung.capstone_2._FORMAT.DECIMAL_FORMAT;

public class StatMainCatListAdapter extends RecyclerView.Adapter<StatMainCatListAdapter.ViewHolder> {
    static int mSelectedYear;
    static int mSelectedMonth;
    static ArrayList<SumOfCat> mItems=new ArrayList<>();

    /* 리스너 객체 참조를 저장하는 변수 */
    private OnItemClickListener mItemClickListener=null;

    /* onItemClickListener 객체 참조를 어댑터에 전달하는 메서드 */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener=listener;
    }

    public StatMainCatListAdapter(int year, int month) {
        super();

        mSelectedYear = year;
        mSelectedMonth = month;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.view_item_stat_list, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        SumOfCat item = mItems.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView_cat;
        TextView textView_name;
        TextView textView_price;

        public ViewHolder(View itemView) {
            super(itemView);
            textView_cat = itemView.findViewById(R.id.textView_cat);
            textView_name = itemView.findViewById(R.id.textView_percent);
            textView_price = itemView.findViewById(R.id.textView_total);

            /* itemView에 onClickListener 설정 (해당 메인 카테고리의 세부 카테고리 조회) */
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position=getAdapterPosition();
                    if(position!= RecyclerView.NO_POSITION) {
                        if(mItemClickListener!=null) {
                            mItemClickListener.onItemClick(v, position);
                        }
                    }
                }
            });
        }

        public void setItem(SumOfCat item) {
            // textView_cat 설정
            int catMain = item.getCat();
            textView_cat.setText(Spec.getCatMainName(catMain));

            // textView_percentage 설정
            int total = item.getSum();
            float percentage
                    = (float) total / AccountBookDB.sumAll(mSelectedYear, mSelectedMonth, Spec.TYPE_EXPENSE) * 100;
            textView_name.setText(Math.round(percentage * 10) / 10.0 + "%");
            Log.d("STAT", item.getCat()+", "+total+", "+
                    AccountBookDB.sumAll(mSelectedYear, mSelectedMonth, Spec.TYPE_EXPENSE));

            // textView_total 설정
            textView_price.setText(DECIMAL_FORMAT.format(total) + "원");
        }
    }

    public void updateDate(int year, int month) {
        mItems.clear();
        mSelectedYear=year;
        mSelectedMonth=month;
    }

    public void addItem(SumOfCat item) {
        mItems.add(item);
    }

    public void setItems(ArrayList<SumOfCat> items) {
        this.mItems = items;
    }

    public SumOfCat getItem(int position) {
        return mItems.get(position);
    }

    public void setItem(int position, SumOfCat item) {
        mItems.set(position, item);
    }
}