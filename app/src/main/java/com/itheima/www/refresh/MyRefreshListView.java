package com.itheima.www.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by guolinyao on 15/11/10.
 */
public class MyRefreshListView extends ListView implements AbsListView.OnScrollListener {


    private static final int PULL_DOWN = 1;
    private static final int REREESHING = 2;
    private static final int RELEASE_REFRESH = 3;
    private ImageView iv_header;
    private ProgressBar pb_header;
    private TextView tv_state;
    private TextView tv_time;
    private int headerViewHright;
    private RotateAnimation upRa;
    private RotateAnimation downRa;
    private View headerView;
    private int downY;//按下的y坐标
    private float moveY;//移动之后的y坐标

    private int currentState = PULL_DOWN;//下拉的状态
    private boolean isLoadingMore = false;
    private View footerView;
    private int footerViewHeight;
    private OnRefreshListener mOnRefreshListener;


    public MyRefreshListView(Context context) {
        this(context, null);//引用二个参数的构造方法
    }

    //布局文件调用
    public MyRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initHeaderView();
        initAnim();
        initFooterView();
        setOnScrollListener(this);//设置滑动监听
    }

    /**
     * 初始化脚布局
     */
    private void initFooterView() {
        footerView = View.inflate(getContext(), R.layout.listview_footer, null);
        footerView.measure(0, 0);//让系统测量脚布局的高度
        footerViewHeight = footerView.getMeasuredHeight();
        footerView.setPadding(0, -footerViewHeight, 0, 0);//隐藏脚布局
        //添加脚布局
        this.addFooterView(footerView);
    }

    /**
     * 初始化动画
     */
    private void initAnim() {
        //向上拉的动画
        upRa = new RotateAnimation(0, -180,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        upRa.setDuration(500);
        upRa.setFillAfter(true);
//        向下拉的动画
        downRa = new RotateAnimation(-180, -360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        downRa.setDuration(500);
        downRa.setFillAfter(true);
    }

    //初始化头布局
    private void initHeaderView() {
        headerView = View.inflate(getContext(), R.layout.listview_header, null);
        iv_header = (ImageView) headerView.findViewById(R.id.iv_header);
        pb_header = (ProgressBar) headerView.findViewById(R.id.pb_header);
        tv_state = (TextView) headerView.findViewById(R.id.tv_state);
        tv_time = (TextView) headerView.findViewById(R.id.tv_time);

        //让系统自己去测量自己的宽高

        headerView.measure(0, 0);
        // headerView.getHeight();//这里获得的值永远为0 因为没经过测量
        //获得headerView的height
        headerViewHright = headerView.getMeasuredHeight();
        headerView.setPadding(0, -headerViewHright, 0, 0);//给headerview设置padding
        //给listview添加headerView 头布局
        this.addHeaderView(headerView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = (int) ev.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                if (currentState == REREESHING) {//如果当前是正在刷新状态 那就不处理移动事件
                    break;
                }
                moveY = ev.getY();
                int dy = (int) (moveY - downY);
                int paddingTop = -headerViewHright + dy;
                int firstVisiblePosition = getFirstVisiblePosition();//获得当前列表显示的第一个条目的索引
                //只有当paddingTop大于头部负数时并且可见第一个条目的是listview的第一个条目时才进行处理
                if (firstVisiblePosition == 0 && paddingTop > -headerViewHright) {
                    if (paddingTop > 0 && currentState == PULL_DOWN) {//当头布局完全显示 并且是下拉状态时，显示松开刷新
                        System.out.println("松开刷新");
                        currentState = RELEASE_REFRESH;
                        switchViewOnStateChange();
                    } else if (paddingTop < 0 && currentState == RELEASE_REFRESH) {
                        //当头布局不完全显示，并且为松开刷新状态，松开刷新变成下拉刷新的时候
                        System.out.println("下拉刷新");
                        currentState = PULL_DOWN;
                        switchViewOnStateChange();
                    }
                    headerView.setPadding(0, paddingTop, 0, 0);//给headerview设置padding
                    return true;//自己处理触摸事件
                }
                break;

            case MotionEvent.ACTION_UP:
                if (currentState == PULL_DOWN) {
                    headerView.setPadding(0, -headerViewHright, 0, 0);
                } else if (currentState == RELEASE_REFRESH) {
                    currentState = REREESHING;
                    switchViewOnStateChange();
                    if(mOnRefreshListener!=null){
                        //回调方法
                        mOnRefreshListener.onRefresh();
                    }
                }

                break;

        }
        return super.onTouchEvent(ev);   //listview自己处理事件
    }

    //根据状态改变头布局的内容
    private void switchViewOnStateChange() {
        switch (currentState) {

            case PULL_DOWN:
                iv_header.startAnimation(downRa);
                tv_state.setText("下拉刷新");
                break;

            case RELEASE_REFRESH:
                iv_header.startAnimation(upRa);
                tv_state.setText("松开刷新");
                break;

            case REREESHING:
                iv_header.clearAnimation();
                iv_header.setVisibility(View.INVISIBLE);
                pb_header.setVisibility(View.VISIBLE);
                tv_state.setText("正在刷新...");
                headerView.setPadding(0, 0, 0, 0);
                break;

        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    //滑动状态改变的时候调用
//当滚动发生改变时，调用该方法
//	OnScrollListener.SCROLL_STATE_FLING;2     手指用力滑动一下，离开屏幕，listview有一个惯性的滑动状态
//	OnScrollListener.SCROLL_STATE_IDLE;0    listview列表处于停滞状态，手指没有触摸屏幕
//	OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;1	手指触摸着屏幕，上下滑动的状态
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        System.out.println("scrollState" + scrollState);
        //当手指离开屏幕，并且列表到达最后一个条目的时候
        int lastVisiblePosition = getLastVisiblePosition();
        if (lastVisiblePosition == (getCount() - 1) && scrollState != SCROLL_STATE_TOUCH_SCROLL &&
                !isLoadingMore) {
            System.out.println("加载更多");
            isLoadingMore = true;
            footerView.setPadding(0, 0, 0, 0);
            setSelection(getCount());//设置最后一条数据显示 设置当前选中的条目
            if(mOnRefreshListener!=null){
                mOnRefreshListener.onLoadMore();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {

    }

    //设置刷新监听器
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mOnRefreshListener = listener;
    }

    /**
     * 刷新或加载完成的调用的回调方法
     */
    public void onFinish() {

        if (isLoadingMore) { //加载完成
            footerView.setPadding(0, -footerViewHeight, 0, 0);
            isLoadingMore = false;
        } else {//刷新完成
            iv_header.setVisibility(View.VISIBLE);//箭头显示
            pb_header.setVisibility(INVISIBLE);//进度圈隐藏
            headerView.setPadding(0, -headerViewHright, 0, 0);
            tv_state.setText("下拉刷新");
            currentState = PULL_DOWN;
        //修改更新时间
            tv_time.setText("最近更新时间" + getCurrentTime());
        }
    }

    //刷新的回调接口
    public interface OnRefreshListener {
        //下拉刷新的回调方法
        void onRefresh();

        //加载更多地回调方法
        void onLoadMore();
    }

}
