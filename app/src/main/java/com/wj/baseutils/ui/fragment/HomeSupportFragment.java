package com.wj.baseutils.ui.fragment;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.wj.base.base.BaseFragment;
import com.wj.base.data.Constants;
import com.wj.base.utils.BannerImageLoader;
import com.wj.base.utils.ToastUtils;
import com.wj.baseutils.R;
import com.wj.baseutils.adapter.TopNewsAdapter;
import com.wj.baseutils.bean.HomeDataBean;
import com.wj.baseutils.contract.HomeSupportContract;
import com.wj.baseutils.model.HomeSupportModelImpl;
import com.wj.baseutils.presenter.HomeSupportPresenterImpl;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

/**
 * Created by wj on 2018/1/14.
 */

public class HomeSupportFragment extends BaseFragment<HomeSupportPresenterImpl, HomeSupportModelImpl>
        implements HomeSupportContract.HomeSupportView {

    @BindView(R.id.smart_layout)
    SmartRefreshLayout smartRefreshLayout;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private Banner layBanner;
    private LinearLayout llTopNewsType;
    private List<String> bannerImg;
    private List<String> bannerTitle;
    private List<HomeDataBean.DataBean.PostsBeanX> posts;
    private String key;
    private TopNewsAdapter adapter;

    @Override
    protected void initViewAndEvent(Bundle savedInstanceState) {
        initView();
    }

    @Override
    protected void lazyLoad() {
        super.lazyLoad();
        mPresenter.loadData(true, key, "");
    }

    private void initView() {
        setProgressIndicator(true);
        Bundle bundle = getArguments();
        key = bundle.getString(Constants.Key.KEY);

        bannerImg = new ArrayList<>();
        bannerTitle = new ArrayList<>();
        posts = new ArrayList<>();

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration
                .Builder(getActivity())
                .size(1)
                .colorResId(R.color.decoration_color)
                .build());
        adapter = new TopNewsAdapter(posts);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new SlideInLeftAnimator());
        recyclerView.getItemAnimator().setChangeDuration(500);
        initHeadView();

        layBanner.setImageLoader(new BannerImageLoader());
        layBanner.setDelayTime(5 * 1000);
        layBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
        layBanner.setIndicatorGravity(BannerConfig.LEFT);

        smartRefreshLayout.setOnRefreshListener(refreshlayout -> mPresenter.loadData(true, key, ""));

        smartRefreshLayout.setOnLoadmoreListener(refreshlayout -> loadMore(false));
    }

    private void loadMore(boolean isRefresh) {
        if (!isRefresh && posts != null && posts.size() > 0) {
            mPresenter.loadData(isRefresh, key, posts.get(posts.size() - 1).originalPublishDate + "");
        }
    }

    private void initHeadView() {
        View headView = View.inflate(getContext(), R.layout.layout_topnews_head, null);
        layBanner = headView.findViewById(R.id.lay_banner);
        llTopNewsType = headView.findViewById(R.id.ll_top_news_type);
        adapter.addHeaderView(headView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home_support;
    }

    @Override
    protected HomeSupportPresenterImpl createPresenter() {
        return new HomeSupportPresenterImpl();
    }

    @Override
    protected HomeSupportModelImpl createModel() {
        return new HomeSupportModelImpl();
    }

    @Override
    public void setTopNewsData(boolean isRefresh, HomeDataBean homeDataBean) {
        try {
            if (isRefresh) {
                posts.clear();
            }
            if (homeDataBean != null
                    && homeDataBean.data != null
                    && homeDataBean.data.posts != null
                    && homeDataBean.data.posts.size() > 0) {
                posts.addAll(homeDataBean.data.posts);
                adapter.notifyItemRangeChanged(posts.size() - Constants.Value.LIMIT, posts.size());
                //加载头条数据
                if (posts != null && posts.size() > 0
                        && posts.get(0).getItemType() == HomeDataBean.DataBean.PostsBeanX.ITEM_BANNER
                        && Constants.TYPE.TOP_NEWS.equals(key)) {
                    loadBanner();
                }
            }
        } catch (Exception e) {
            ToastUtils.showDebugShort(e.getMessage());
        }
        smartRefreshLayout.finishLoadmore();
        smartRefreshLayout.finishRefresh();
        setProgressIndicator(false);
    }

    private void loadBanner() {
        if (Constants.TYPE.TOP_NEWS.equals(key)) {
            layBanner.setVisibility(View.VISIBLE);
            llTopNewsType.setVisibility(View.VISIBLE);
            for (int i = 0; i < posts.size(); i++) {
                if (posts.get(i).getItemType() == HomeDataBean.DataBean.PostsBeanX.ITEM_BANNER) {
                    bannerImg.add(posts.get(i).imageUrls.get(0));
                    bannerTitle.add(posts.get(i).title);
                }
            }
            layBanner.setImages(bannerImg);
            layBanner.setBannerTitles(bannerTitle);
            layBanner.start();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        layBanner.startAutoPlay();
    }

    @Override
    public void onStop() {
        super.onStop();
        layBanner.stopAutoPlay();
    }

    @Override
    public void onEventMainThread(Message msg) {
        super.onEventMainThread(msg);
        switch (msg.what) {
            case Constants.Key_EventBus_Msg.CATEGORY_CHANGE:
                if (isVisible()){
                    mPresenter.loadData(true, key, "");
                }
                break;
        }
    }
}
