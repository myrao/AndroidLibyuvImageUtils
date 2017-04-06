package tech.shutu.androidlibyuvimageutils.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import tech.shutu.androidlibyuvimageutils.R;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.activity_main)
    RelativeLayout activityMain;
    @BindView(R.id.rv_main)
    ListView rvMain;

    MyRecyclerAdapter rvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    public void initViews() {
        initRecyclerView();
    }

    private void initRecyclerView() {
        rvAdapter = new MyRecyclerAdapter(this);
        rvMain.setAdapter(rvAdapter);
        rvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, " position clicked => " + position);
                Intent intent = new Intent(MainActivity.this, BitmapToYuvActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void initData() {
    }

    public static void startActivity(Context ctx) {
        Intent it = new Intent(ctx, MainActivity.class);
        ctx.startActivity(it);
    }


    public static class MyRecyclerAdapter extends BaseAdapter {

        private String[] mTitles;

        public MyRecyclerAdapter(Context context) {
            mTitles = context.getResources().getStringArray(R.array.array_titles);
        }

        @Override
        public int getCount() {
            return mTitles != null ? mTitles.length : 0;
        }

        @Override
        public Object getItem(int position) {
            return mTitles[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_items, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tvTitle.setText(mTitles[position]);
            return convertView;
        }

        static class ViewHolder {
            @BindView(R.id.tv_title)
            TextView tvTitle;

            public ViewHolder(View itemView) {
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
