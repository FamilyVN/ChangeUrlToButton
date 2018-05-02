package com.vutuananh.changeurltobutton;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;

import com.vutuananh.changeurltobutton.databinding.ActivityMainBinding;
import com.vutuananh.changeurltobutton.databinding.ItemRecyclerViewButtonBinding;
import com.vutuananh.changeurltobutton.databinding.ItemRecyclerViewMessageBinding;
import com.vutuananh.changeurltobutton.model.ViewModel;
import com.vutuananh.changeurltobutton.view.adapter.BaseViewAdapter;
import com.vutuananh.changeurltobutton.view.adapter.BindingViewHolder;
import com.vutuananh.changeurltobutton.view.adapter.MultiTypeAdapter;
import com.vutuananh.changeurltobutton.view.adapter.SingleTypeAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

public class MainActivity extends AppCompatActivity {
    private static final String[] ARRAY = {
        "Bản dịch từ: https://ksylvest.com/posts/2017-08-12/fabrication-vs-factorygirl",
        "http://s2phim.net/xem-phim/nguoi-hau-cua-thieu-gia-mu/141347",
        "Đại gia Hà Nội đổi 10 cây vàng lấy sanh cổ gây “chấn động” một thời",
        "Giá vàng hôm nay 2/5: Bất ngờ giảm mạnh sau kì nghỉ lễ",
        "Trong tuần trước, vàng SJC mặc dù đã đứng sát ngưỡng 37 triệu đồng nhưng không thể chinh phục mốc này. Trong những phiên giữa tuần, vàng SJC hụt hơi và đã có lúc chỉ còn 36,75 triệu đồng. Phải tới phiên cuối cùng trước kỳ nghỉ lễ, giá vàng mới bật tăng trở lại mốc 36,84 triệu đồng mỗi lượng.",
        "Converting array to list in Java\n https://stackoverflow.com/questions/2607289/converting-array-to-list-in-java",
        "Fabrication hay FactoryGirl nhanh hơn khi viết Rspec https://viblo.asia https://viblo.asia https://viblo.asia https://viblo.asia Viblo"
    };
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        final SingleTypeAdapter<String> adapter = new SingleTypeAdapter<>(this, R.layout.item_recycler_view_message);
        adapter.addAll(Arrays.asList(ARRAY));
        mBinding.recyclerMessage.setAdapter(adapter);
        mBinding.recyclerMessage.setLayoutManager(new LinearLayoutManager(this));
        adapter.setDecorator(new BaseViewAdapter.Decorator() {
            @Override
            public void decorator(BindingViewHolder holder, int position, int viewType) {
                final List<ViewModel> viewModelList = getUrlList(adapter.get(position));
                final ItemRecyclerViewMessageBinding binding = (ItemRecyclerViewMessageBinding) holder.getBinding();
                if (viewModelList == null || viewModelList.isEmpty()) {
                    binding.tvMessage.setVisibility(View.VISIBLE);
                    binding.recyclerUrl.setVisibility(View.GONE);
                    return;
                }
                binding.tvMessage.setVisibility(View.GONE);
                binding.recyclerUrl.setVisibility(View.VISIBLE);
                MultiTypeAdapter adapterUrl = new MultiTypeAdapter(MainActivity.this);
                adapterUrl.addViewTypeToLayoutMap(ViewModel.TYPE_TEXT, R.layout.item_recycler_view_text);
                adapterUrl.addViewTypeToLayoutMap(ViewModel.TYPE_URL, R.layout.item_recycler_view_button);
                adapterUrl.addAll(viewModelList, new MultiTypeAdapter.MultiViewTyper() {
                    @Override
                    public int getViewType(Object item) {
                        if (item instanceof ViewModel) {
                            return ((ViewModel) item).getType();
                        }
                        return 0;
                    }
                });
                adapterUrl.setDecorator(new BaseViewAdapter.Decorator() {
                    @Override
                    public void decorator(BindingViewHolder holder, int positionChild, int viewType) {
                        if (holder.getBinding() instanceof ItemRecyclerViewButtonBinding) {
                            ItemRecyclerViewButtonBinding buttonBinding = (ItemRecyclerViewButtonBinding) holder.getBinding();
                            final String url = viewModelList.get(positionChild).getText();
                            buttonBinding.btnOpen.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openLinkInBrowser(MainActivity.this, url);
                                }
                            });
                        }
                    }
                });
                binding.recyclerUrl.setAdapter(adapterUrl);
                binding.recyclerUrl.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            }
        });
    }

    public List<ViewModel> getUrlList(String text) {
        if (TextUtils.isEmpty(text) || TextUtils.isEmpty(text.trim())) {
            return null;
        }
        Matcher urlMatcher = Patterns.WEB_URL.matcher(text);
        List<ViewModel.UrlPosition> urlPositionList = new ArrayList<>();
        while (urlMatcher.find()) {
            int start = urlMatcher.start();
            int end = urlMatcher.end();
            String url = text.substring(start, end);
            urlPositionList.add(new ViewModel.UrlPosition(url, start, end));
        }
        List<ViewModel> viewModelList = new ArrayList<>();
        if (urlPositionList.isEmpty()) return null;
        int start = 0;
        for (ViewModel.UrlPosition urlPosition : urlPositionList) {
            String startStr = text.substring(start, urlPosition.getStart());
            if (!TextUtils.isEmpty(startStr.trim())) {
                viewModelList.add(new ViewModel(startStr));
            }
            viewModelList.add(new ViewModel(urlPosition.getUrl(), ViewModel.TYPE_URL));
            start = urlPosition.getEnd();
            if (urlPositionList.indexOf(urlPosition) == urlPositionList.size() - 1) {
                if (start < text.length()) {
                    String endStr = text.substring(start, text.length());
                    if (!TextUtils.isEmpty(endStr.trim())) {
                        viewModelList.add(new ViewModel(endStr));
                    }
                }
            }
        }
        return viewModelList;
    }

    public void openLinkInBrowser(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"));
        List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(intent, 0);
        if (!resolveInfoList.isEmpty() && resolveIntent(context, resolveInfoList, url)) {
            return;
        }
        resolveInfoList = context.getPackageManager().queryIntentActivities(intent, 0);
        resolveIntent(context, resolveInfoList, url);
    }

    private boolean resolveIntent(Context context, List<ResolveInfo> resolveInfoList, String url) {
        url = url.toLowerCase();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        List<Intent> targetIntents = new ArrayList<>();
        for (ResolveInfo resolveInfo : resolveInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            if (context.getPackageName().equals(packageName)) continue;
            Intent targetIntent = new Intent(android.content.Intent.ACTION_VIEW);
            targetIntent.setData(Uri.parse(url));
            targetIntent.setPackage(packageName);
            targetIntents.add(targetIntent);
        }
        if (targetIntents.size() == 0) return false;
        context.startActivity(targetIntents.get(0));
        return true;
    }
}
