package com.zegocloud.zimkit.components.forward;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.utils.ZIMKitDateUtils;
import com.zegocloud.zimkit.components.message.adapter.ZIMKitMessageAdapter;
import com.zegocloud.zimkit.components.message.model.AudioMessageModel;
import com.zegocloud.zimkit.components.message.model.CombineMessageModel;
import com.zegocloud.zimkit.components.message.model.FileMessageModel;
import com.zegocloud.zimkit.components.message.model.ImageMessageModel;
import com.zegocloud.zimkit.components.message.model.VideoMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.utils.OnRecyclerViewItemTouchListener;
import com.zegocloud.zimkit.components.message.widget.ZIMKitMessageTimeLineDecoration;
import com.zegocloud.zimkit.components.message.widget.viewholder.AudioMessageHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.CombineMessageHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.FileMessageHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.ImageMessageHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.VideoMessageHolder;
import com.zegocloud.zimkit.databinding.ZimkitActivityForwardDetailsBinding;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.enums.ZIMMessageType;
import java.util.ArrayList;
import java.util.List;

public class ForwardDetailsActivity extends AppCompatActivity {

    private ZimkitActivityForwardDetailsBinding binding;
    //Timestamp interval,5min
    private final long mTimeLineInterval = 1000 * 60 * 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ZimkitActivityForwardDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        List<ZIMKitMessageModel> forwardMessages = ZIMKitCore.getInstance().getForwardMessages();

        String title = getIntent().getStringExtra("title");
        boolean oneSide = getIntent().getBooleanExtra("one_side", false);
        binding.title.hideRightButton();
        binding.title.setTitle(title);

        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        ZIMKitMessageAdapter mAdapter = new ZIMKitMessageAdapter(true, oneSide);

        mAdapter.setNewList(new ArrayList<>(forwardMessages));
        binding.recyclerview.setAdapter(mAdapter);

        binding.recyclerview.addItemDecoration(
            new ZIMKitMessageTimeLineDecoration(this, new ZIMKitMessageTimeLineDecoration.DecorationCallback() {
                @Override
                public boolean needAddTimeLine(int position) {
                    if (position < 0) {
                        return false;
                    }
                    if (position == 0) {
                        return mAdapter.getItemDataList().get(position).getMessage() != null;
                    }
                    ZIMMessage nowMessage = mAdapter.getItemDataList().get(position).getMessage();
                    ZIMMessage lastMessage = mAdapter.getItemDataList().get(position - 1).getMessage();
                    if (nowMessage == null || lastMessage == null) {
                        return false;
                    } else {
                        return (nowMessage.getTimestamp() - lastMessage.getTimestamp()) > mTimeLineInterval;
                    }
                }

                @Override
                public String getTimeLine(int position) {
                    if (position < 0) {
                        return "";
                    }
                    return ZIMKitDateUtils.getMessageDate(
                        mAdapter.getItemDataList().get(position).getMessage().getTimestamp(), true);
                }
            }));
        binding.recyclerview.addOnItemTouchListener(new OnRecyclerViewItemTouchListener(binding.recyclerview) {
            @Override
            public boolean onItemChildClick(ViewHolder vh, View itemChild) {
                int position = vh.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return false;
                }
                ZIMKitMessageModel itemData = mAdapter.getItemData(position);
                if (itemChild.getId() == R.id.msg_content_layout) {
                    if (itemData.getMessage().getType() == ZIMMessageType.COMBINE) {
                        (((CombineMessageHolder) vh)).onMessageLayoutClicked(vh.itemView.getContext(),
                            (CombineMessageModel) itemData);
                    } else if (itemData.getMessage().getType() == ZIMMessageType.AUDIO) {
                        (((AudioMessageHolder) vh)).onMessageLayoutClicked((AudioMessageModel) itemData);
                    } else if (itemData.getMessage().getType() == ZIMMessageType.IMAGE) {
                        (((ImageMessageHolder) vh)).onMessageLayoutClicked((ImageMessageModel) itemData);
                    } else if (itemData.getMessage().getType() == ZIMMessageType.FILE) {
                        (((FileMessageHolder) vh)).onMessageLayoutClicked((FileMessageModel) itemData);
                    } else if (itemData.getMessage().getType() == ZIMMessageType.VIDEO) {
                        (((VideoMessageHolder) vh)).onMessageLayoutClicked((VideoMessageModel) itemData);
                    }
                    return true;
                }
                return false;
            }
        });
    }
}