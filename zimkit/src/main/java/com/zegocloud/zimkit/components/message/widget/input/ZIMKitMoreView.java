package com.zegocloud.zimkit.components.message.widget.input;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.components.message.adapter.ZIMKitInputMoreAdapter;
import com.zegocloud.zimkit.components.message.model.ZIMKitInputButtonModel;
import com.zegocloud.zimkit.components.message.utils.OnRecyclerViewItemTouchListener;
import com.zegocloud.zimkit.databinding.ZimkitLayoutInputMoreBinding;
import java.util.List;

public class ZIMKitMoreView extends FrameLayout {

    private ZimkitLayoutInputMoreBinding binding;
    private InputMoreCallback mCallback;
    private ZIMKitInputMoreAdapter inputMoreAdapter;

    public ZIMKitMoreView(@NonNull Context context) {
        super(context);
        initView();
    }

    public ZIMKitMoreView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ZIMKitMoreView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.zimkit_layout_input_more, this,
            true);
        inputMoreAdapter = new ZIMKitInputMoreAdapter();

        binding.inputMoreRecyclerview.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                binding.inputMoreRecyclerview.getViewTreeObserver().removeOnPreDrawListener(this);
                binding.inputMoreRecyclerview.setAdapter(inputMoreAdapter);
                binding.inputMoreRecyclerview.setLayoutManager(new GridLayoutManager(getContext(), 4));
                binding.inputMoreRecyclerview.addOnItemTouchListener(
                    new OnRecyclerViewItemTouchListener(binding.inputMoreRecyclerview) {
                        @Override
                        public void onItemClick(ViewHolder vh) {
                            int position = vh.getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                ZIMKitInputButtonModel itemModel = inputMoreAdapter.getItemModel(position);
                                if (mCallback != null) {
                                    mCallback.onClickMoreItem(position, itemModel);
                                }
                            }
                        }
                    });
                return false;
            }
        });
    }

    public void setInputMoreCallback(InputMoreCallback callback) {
        mCallback = callback;
    }

    public void setInputMoreItems(List<ZIMKitInputButtonModel> inputMoreItems) {
        inputMoreAdapter.setItemModels(inputMoreItems);
    }

    public interface InputMoreCallback {

        void onClickMoreItem(int position, ZIMKitInputButtonModel itemModel);
    }
}
