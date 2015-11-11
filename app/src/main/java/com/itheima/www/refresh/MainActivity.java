package com.itheima.www.refresh;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MyRefreshListView refresh_lv;
    private List<String> mDatas;
    private MyAdapter adapter;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refresh_lv = (MyRefreshListView) findViewById(R.id.refresh_lv);
        initData();
        adapter = new MyAdapter();

        refresh_lv.setAdapter(adapter);
//设置刷新监听
        refresh_lv.setOnRefreshListener(new MyRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDatas.add(0, "刷新出来的数据");
                        adapter.notifyDataSetChanged();
                        refresh_lv.onFinish();
                    }
                }, 2000);
            }

            @Override
            public void onLoadMore() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDatas.add("加载出来的数据1");
                        mDatas.add("加载出来的数据2");
                        mDatas.add("加载出来的数据3");
                        adapter.notifyDataSetChanged();
                        refresh_lv.onFinish();
                    }
                }, 2000);
            }
        });
    }

    private void initData() {
        mDatas = new ArrayList<String>();
        for (int i = 0; i < 20; i++) {
            mDatas.add("条目" + i);
        }
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = null;

            tv = new TextView(getApplicationContext());
            tv.setText(mDatas.get(position));
            tv.setTextSize(20);
            tv.setTextColor(Color.BLACK);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setPadding(5, 5, 5, 5);
            return tv;
        }
    }


}
