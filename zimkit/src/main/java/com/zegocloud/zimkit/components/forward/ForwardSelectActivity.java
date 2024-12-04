package com.zegocloud.zimkit.components.forward;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.components.forward.ForwardConfirmDialog.Callback;
import com.zegocloud.zimkit.components.message.model.AudioMessageModel;
import com.zegocloud.zimkit.components.message.model.CombineMessageModel;
import com.zegocloud.zimkit.components.message.model.FileMessageModel;
import com.zegocloud.zimkit.components.message.model.ImageMessageModel;
import com.zegocloud.zimkit.components.message.model.TextMessageModel;
import com.zegocloud.zimkit.components.message.model.VideoMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.utils.ChatMessageBuilder;
import com.zegocloud.zimkit.components.message.utils.OnRecyclerViewItemTouchListener;
import com.zegocloud.zimkit.databinding.ZimkitActivityForwardSelectBinding;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.callback.MessageSentCallback;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import com.zegocloud.zimkit.services.model.ZIMKitUser;
import com.zegocloud.zimkit.services.utils.ZIMMessageUtil;
import im.zego.zim.callback.ZIMCombineMessageDetailQueriedCallback;
import im.zego.zim.entity.ZIMCombineMessage;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMMessageSendConfig;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMMessageType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ForwardSelectActivity extends AppCompatActivity {

    private ZimkitActivityForwardSelectBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ZimkitActivityForwardSelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.title.hideRightButton();

        binding.title.setTitle(getString(R.string.zimkit_chat_list));

        ArrayList<ZIMKitConversation> conversations = new ArrayList<>(ZIMKitCore.getInstance().getConversations());

        ForwardConversionsAdapter conversionsAdapter = new ForwardConversionsAdapter();
        conversionsAdapter.setConversationList(conversations);
        binding.recyclerview.setAdapter(conversionsAdapter);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerview.addOnItemTouchListener(new OnRecyclerViewItemTouchListener(binding.recyclerview) {
            @Override
            public void onItemClick(ViewHolder vh) {
                if (vh.getAdapterPosition() == RecyclerView.NO_POSITION) {
                    return;
                }
                List<ZIMKitMessageModel> forwardMessages = ZIMKitCore.getInstance().getForwardMessages();
                if (forwardMessages == null || forwardMessages.isEmpty()) {
                    return;
                }

                ZIMKitConversation clickConversation = conversionsAdapter.getItemData(vh.getAdapterPosition());
                ForwardConfirmDialog forwardConfirmDialog = new ForwardConfirmDialog(ForwardSelectActivity.this);
                forwardConfirmDialog.setCallback(new Callback() {
                    @Override
                    public void onClickConfirm(ForwardConfirmDialog dialog) {
                        dialog.dismiss();
                        ZIMKitForwardType forwardType = ZIMKitCore.getInstance().getForwardType();
                        List<ZIMKitMessageModel> forwardMessages = ZIMKitCore.getInstance().getForwardMessages();
                        if (forwardType == ZIMKitForwardType.SINGLE) {
                            forwardSingleMessage(forwardMessages, clickConversation);
                        } else if (forwardType == ZIMKitForwardType.INDIVIDUAL) {
                            forwardIndividualMessages(forwardMessages, clickConversation);
                        } else if (forwardType == ZIMKitForwardType.MERGE) {
                            forwardMergeMessages(forwardMessages, clickConversation);
                        }

                        ZIMKitCore.getInstance().clearForwardMessages();

                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onClickCancel(ForwardConfirmDialog dialog) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onClickContent(ForwardConfirmDialog dialog) {
                        // 转发预览功能 去掉
                        //                        startForwardDetails();
                    }
                });
                forwardConfirmDialog.setClickConversation(clickConversation);
                forwardConfirmDialog.show();
            }
        });
    }

    private void startForwardDetails() {
        List<ZIMKitMessageModel> forwardMessages = ZIMKitCore.getInstance().getForwardMessages();
        ZIMKitForwardType forwardType = ZIMKitCore.getInstance().getForwardType();

        Intent intent = new Intent(ForwardSelectActivity.this, ForwardDetailsActivity.class);
        String title = "";
        if (forwardType == ZIMKitForwardType.SINGLE) {
            title = getString(R.string.zimkit_forward_detail_single);
        } else if (forwardType == ZIMKitForwardType.INDIVIDUAL || forwardType == ZIMKitForwardType.MERGE) {
            String conversationID = forwardMessages.get(0).getMessage().getConversationID();
            ZIMKitConversation forwardConversation = ZIMKitCore.getInstance().getZIMKitConversation(conversationID);
            String string;
            if (forwardConversation.getType() == ZIMConversationType.GROUP) {
                string = getString(R.string.zimkit_title_group_chat);
            } else {
                ZIMKitUser localUser = ZIMKit.getLocalUser();
                string = getString(R.string.zimkit_forward_content_s2, localUser.getName(),
                    forwardConversation.getName());
            }
            title = getString(R.string.zimkit_forward_detail_title, string);
        }
        intent.putExtra("title", title);
        startActivity(intent);
    }

    private void forwardMergeMessages(List<ZIMKitMessageModel> forwardMessages, ZIMKitConversation clickConversation) {
        ArrayList<ZIMMessage> collect = forwardMessages.stream().map(kitMessageModel -> kitMessageModel.getMessage())
            .collect(Collectors.toCollection(ArrayList::new));

        if (collect.isEmpty()) {
            return;
        }
        String conversationID = collect.get(0).getConversationID();
        ZIMKitConversation forwardConversation = ZIMKitCore.getInstance().getZIMKitConversation(conversationID);
        String string;
        if (forwardConversation.getType() == ZIMConversationType.GROUP) {
            string = getString(R.string.zimkit_title_group_chat);
        } else {
            ZIMKitUser localUser = ZIMKit.getLocalUser();
            string = getString(R.string.zimkit_forward_content_s2, localUser.getName(), forwardConversation.getName());
        }
        String title = getString(R.string.zimkit_forward_detail_title, string);

        String end = "...";
        int titleMaxByte = 100;
        String emptyTitle = getString(R.string.zimkit_forward_detail_title, "");
        int emptyTitleByte = emptyTitle.getBytes().length;
        int maxTitleLimit = titleMaxByte - emptyTitleByte;

        String titleSub = subStringToLimitBytes(string, maxTitleLimit);
        if (titleSub.length() != string.length()) {
            titleSub = subStringToLimitBytes(string, maxTitleLimit - end.getBytes().length);
            title = getString(R.string.zimkit_forward_detail_title, titleSub + end);
        } else {
            title = getString(R.string.zimkit_forward_detail_title, titleSub);
        }
        int summaryMaxByte = 500;
        int maxLineLength = 164;
        int maxNameLength = 15;
        int maxContentLength;
        int maxSummeryLines = 3;

        StringBuilder builder = new StringBuilder();

        int max = Math.min(collect.size(), maxSummeryLines);
        for (int i = 0; i < max; i++) {
            ZIMMessage zimMessage = collect.get(i);
            ZIMUserFullInfo memoryUserInfo = ZIMKitCore.getInstance().getMemoryUserInfo(zimMessage.getSenderUserID());
            if (memoryUserInfo != null) {
                String userName = memoryUserInfo.baseInfo.userName;
                String nameSub = subStringToLimitBytes(userName, maxNameLength);
                if (nameSub.length() != userName.length()) {
                    nameSub = subStringToLimitBytes(userName, maxNameLength - end.getBytes().length) + end;
                }
                maxContentLength = maxLineLength - nameSub.getBytes().length;
                String content = ZIMMessageUtil.simplifyZIMMessageContent(zimMessage).replace("\n", "")
                    .replace("\r", "");
                String contentSub = subStringToLimitBytes(content, maxContentLength);
                if (contentSub.length() != content.length()) {
                    contentSub = subStringToLimitBytes(content, maxContentLength - end.getBytes().length) + end;
                }
                String contentLines = nameSub + ": " + contentSub;
                builder.append(contentLines);

                if (i != max - 1) {
                    builder.append("\n");
                }
            }
        }

        ZIMCombineMessage combineMessage = new ZIMCombineMessage(title, builder.toString(), collect);
        ZIMKitCore.getInstance().sendMessage(combineMessage, clickConversation.getId(), clickConversation.getType(),
            new ZIMMessageSendConfig(), null);
    }

    private String subStringToLimitBytes(String string, int byteLength) {
        if (string.getBytes().length > byteLength) {
            int left = byteLength;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < string.length(); i++) {
                char c = string.charAt(i);
                int length = String.valueOf(c).getBytes().length;
                if (left - length > 0) {
                    left = left - length;
                    sb.append(c);
                } else {
                    break;
                }
            }
            return sb.toString();
        } else {
            return string;
        }
    }

    private void forwardIndividualMessages(List<ZIMKitMessageModel> forwardMessages, ZIMKitConversation conversation) {
        long time = 200;
        for (int i = 0; i < forwardMessages.size(); i++) {
            ZIMKitMessageModel messageModel = forwardMessages.get(i);
            binding.getRoot().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendForwardMessage(conversation, messageModel, false);
                }
            }, time * i);
        }
    }

    private void forwardSingleMessage(List<ZIMKitMessageModel> forwardMessages, ZIMKitConversation conversation) {
        ZIMKitMessageModel messageModel = forwardMessages.get(0);
        sendForwardMessage(conversation, messageModel, false);
    }

    private static void sendForwardMessage(ZIMKitConversation clickItem, ZIMKitMessageModel messageModel,
        boolean keepAudio) {
        if (messageModel.getMessage().getType() == ZIMMessageType.TEXT) {
            sendTextMessage(clickItem, (TextMessageModel) messageModel);
        } else if (messageModel.getMessage().getType() == ZIMMessageType.IMAGE) {
            sendImageMessage(clickItem, (ImageMessageModel) messageModel);
        } else if (messageModel.getMessage().getType() == ZIMMessageType.VIDEO) {
            sendVideoMessage(clickItem, (VideoMessageModel) messageModel);
        } else if (messageModel.getMessage().getType() == ZIMMessageType.AUDIO) {
            if (keepAudio) {
                sendAudioMessage(clickItem, (AudioMessageModel) messageModel);
            } else {
                String content = ZIMMessageUtil.simplifyZIMMessageContent(messageModel.getMessage());
                ZIMKitMessageModel chatMessage = ChatMessageBuilder.buildTextMessage(content);
                sendTextMessage(clickItem, (TextMessageModel) chatMessage);
            }
        } else if (messageModel.getMessage().getType() == ZIMMessageType.FILE) {
            sendFileMessage(clickItem, (FileMessageModel) messageModel);
        } else if (messageModel.getMessage().getType() == ZIMMessageType.COMBINE) {
            sendCombineMessage(clickItem, (CombineMessageModel) messageModel);
        }
    }

    private static void sendCombineMessage(ZIMKitConversation clickConversation,
        CombineMessageModel combineMessageModel) {
        ZIMKitCore.getInstance().queryCombineMessageDetail((ZIMCombineMessage) combineMessageModel.getMessage(),
            new ZIMCombineMessageDetailQueriedCallback() {
                @Override
                public void onCombineMessageDetailQueried(ZIMCombineMessage message, ZIMError error) {
                    ZIMCombineMessage combineMessage = new ZIMCombineMessage(message.title, message.summary,
                        message.messageList);
                    ZIMKitCore.getInstance()
                        .sendMessage(combineMessage, clickConversation.getId(), clickConversation.getType(),
                            new ZIMMessageSendConfig(), null);
                }
            });

    }

    private static void sendFileMessage(ZIMKitConversation clickItem, FileMessageModel messageModel) {
        FileMessageModel fileMessageModel = messageModel;
        if (clickItem.getType() == ZIMConversationType.PEER) {
            ZIMKit.sendFileMessage(fileMessageModel.getFileLocalPath(), clickItem.getId(), ZIMConversationType.PEER,
                new MessageSentCallback() {
                    @Override
                    public void onMessageSent(ZIMError error) {

                    }
                });
        } else {
            ZIMKit.sendGroupFileMessage(fileMessageModel.getFileLocalPath(), clickItem.getId(), clickItem.getName(),
                ZIMConversationType.GROUP, new MessageSentCallback() {
                    @Override
                    public void onMessageSent(ZIMError error) {

                    }
                });
        }
    }

    private static void sendAudioMessage(ZIMKitConversation clickItem, AudioMessageModel audioMessageModel) {
        if (clickItem.getType() == ZIMConversationType.PEER) {
            ZIMKit.sendAudioMessage(audioMessageModel.getFileLocalPath(), audioMessageModel.getAudioDuration(),
                clickItem.getId(), ZIMConversationType.PEER, new MessageSentCallback() {
                    @Override
                    public void onMessageSent(ZIMError error) {

                    }
                });
        } else {
            ZIMKit.sendGroupAudioMessage(audioMessageModel.getFileLocalPath(), audioMessageModel.getAudioDuration(),
                clickItem.getId(), clickItem.getName(), ZIMConversationType.GROUP, new MessageSentCallback() {
                    @Override
                    public void onMessageSent(ZIMError error) {

                    }
                });
        }
    }


    private static void sendVideoMessage(ZIMKitConversation clickItem, VideoMessageModel videoMessageModel) {
        if (clickItem.getType() == ZIMConversationType.PEER) {
            ZIMKit.sendVideoMessage(videoMessageModel.getFileLocalPath(), videoMessageModel.getVideoDuration(),
                clickItem.getId(), ZIMConversationType.PEER, new MessageSentCallback() {
                    @Override
                    public void onMessageSent(ZIMError error) {

                    }
                });
        } else {
            ZIMKit.sendGroupVideoMessage(videoMessageModel.getFileLocalPath(), videoMessageModel.getVideoDuration(),
                clickItem.getId(), clickItem.getName(), ZIMConversationType.GROUP, new MessageSentCallback() {
                    @Override
                    public void onMessageSent(ZIMError error) {

                    }
                });
        }
    }

    private static void sendImageMessage(ZIMKitConversation clickItem, ImageMessageModel imageMessageModel) {
        if (clickItem.getType() == ZIMConversationType.PEER) {
            ZIMKit.sendImageMessage(imageMessageModel.getFileLocalPath(), clickItem.getId(), ZIMConversationType.PEER,
                new MessageSentCallback() {
                    @Override
                    public void onMessageSent(ZIMError error) {

                    }
                });
        } else {
            ZIMKit.sendGroupImageMessage(imageMessageModel.getFileLocalPath(), clickItem.getId(), clickItem.getName(),
                ZIMConversationType.GROUP, new MessageSentCallback() {
                    @Override
                    public void onMessageSent(ZIMError error) {

                    }
                });
        }
    }

    private static void sendTextMessage(ZIMKitConversation clickItem, TextMessageModel textMessageModel) {
        ZIMKit.sendTextMessage(textMessageModel.getContent(), clickItem.getId(), clickItem.getName(),
            clickItem.getType(), new MessageSentCallback() {
                @Override
                public void onMessageSent(ZIMError error) {

                }
            });
    }
}