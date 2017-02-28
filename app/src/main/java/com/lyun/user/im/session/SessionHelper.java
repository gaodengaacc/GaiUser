package com.lyun.user.im.session;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.lyun.user.R;
import com.lyun.user.im.NimCache;
import com.lyun.user.im.contact.activity.UserProfileActivity;
import com.lyun.user.im.session.action.ImageAction;
import com.lyun.user.im.session.activity.MessageHistoryActivity;
import com.lyun.user.im.session.extension.CustomAttachParser;
import com.lyun.user.im.session.extension.CustomAttachment;
import com.lyun.user.im.session.extension.GuessAttachment;
import com.lyun.user.im.session.extension.SnapChatAttachment;
import com.lyun.user.im.session.extension.StickerAttachment;
import com.lyun.user.im.session.search.SearchMessageActivity;
import com.lyun.user.im.session.viewholder.MsgViewHolderAVChat;
import com.lyun.user.im.session.viewholder.MsgViewHolderDefCustom;
import com.lyun.user.im.session.viewholder.MsgViewHolderFile;
import com.lyun.user.im.session.viewholder.MsgViewHolderGuess;
import com.lyun.user.im.session.viewholder.MsgViewHolderSnapChat;
import com.lyun.user.im.session.viewholder.MsgViewHolderSticker;
import com.lyun.user.im.session.viewholder.MsgViewHolderTip;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.popupmenu.NIMPopupMenu;
import com.netease.nim.uikit.common.ui.popupmenu.PopupMenuItem;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nim.uikit.session.MessagePanelCustomization;
import com.netease.nim.uikit.session.SessionCustomization;
import com.netease.nim.uikit.session.SessionEventListener;
import com.netease.nim.uikit.session.ToolbarCustomization;
import com.netease.nim.uikit.session.actions.BaseAction;
import com.netease.nim.uikit.session.helper.MessageHelper;
import com.netease.nim.uikit.session.helper.MessageListPanelHelper;
import com.netease.nim.uikit.session.module.MsgForwardFilter;
import com.netease.nim.uikit.session.module.MsgRevokeFilter;
import com.netease.nim.uikit.session.module.input.InputPanelCustomization;
import com.netease.nim.uikit.team.model.TeamExtras;
import com.netease.nim.uikit.team.model.TeamRequestCode;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.model.AVChatAttachment;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.model.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * UIKit自定义消息界面用法展示类
 */
public class SessionHelper {

    private static final int ACTION_HISTORY_QUERY = 0;
    private static final int ACTION_SEARCH_MESSAGE = 1;
    private static final int ACTION_CLEAR_MESSAGE = 2;

    private static SessionCustomization p2pCustomization;
    private static SessionCustomization teamCustomization;
    private static SessionCustomization myP2pCustomization;

    private static NIMPopupMenu popupMenu;
    private static List<PopupMenuItem> menuItemList;

    public static void init() {
        // 注册自定义消息附件解析器
        NIMClient.getService(MsgService.class).registerCustomAttachmentParser(new CustomAttachParser());

        // 注册各种扩展消息类型的显示ViewHolder
        registerViewHolders();

        // 设置会话中点击事件响应处理
        setSessionListener();

        // 注册消息转发过滤器
        registerMsgForwardFilter();

        // 注册消息撤回过滤器
        registerMsgRevokeFilter();

        // 注册消息撤回监听器
        registerMsgRevokeObserver();

        NimUIKit.setCommonP2PSessionCustomization(getP2pCustomization());

        NimUIKit.setCommonTeamSessionCustomization(getTeamCustomization());
    }

    public static void startP2PSession(Context context, String account) {
        startP2PSession(context, account, null);
    }

    public static void startP2PSession(Context context, String account, IMMessage anchor) {
        if (!NimCache.getAccount().equals(account)) {
            NimUIKit.startP2PSession(context, account, anchor);
        } else {
            NimUIKit.startChatting(context, account, SessionTypeEnum.P2P, getMyP2pCustomization(), anchor);
        }
    }

    public static void startTeamSession(Context context, String tid) {
        startTeamSession(context, tid, null);
    }

    public static void startTeamSession(Context context, String tid, IMMessage anchor) {
        NimUIKit.startTeamSession(context, tid, anchor);
    }

    // 打开群聊界面(用于 UIKIT 中部分界面跳转回到指定的页面)
    public static void startTeamSession(Context context, String tid, Class<? extends Activity> backToClass, IMMessage anchor) {
        NimUIKit.startChatting(context, tid, SessionTypeEnum.Team, getTeamCustomization(), backToClass, anchor);
    }

    // 定制化单聊界面。如果使用默认界面，返回null即可
    private static SessionCustomization getP2pCustomization() {
        if (p2pCustomization == null) {

            //定制Toolbar
            ToolbarCustomization toolbarCustomization = new ToolbarCustomization();
            ToolBarOptions toolBarOptions = new ToolBarOptions();
            toolBarOptions.setLogoId(0);
            toolBarOptions.setNavigateId(R.mipmap.ic_arrow_back_black);
            toolBarOptions.setTitleString("text test");
            toolbarCustomization.setToolBarOptions(toolBarOptions);
            // 定制ActionBar右边的按钮，可以加多个
            ArrayList<ToolbarCustomization.OptionsButton> buttons = new ArrayList<>();
            toolbarCustomization.setOptionsButtons(buttons);

            //定制MessagePanel
            // 背景
            // p2pCustomization.backgroundColor = Color.BLUE;
            // p2pCustomization.backgroundUri = "file:///android_asset/xx/bk.jpg";
            // p2pCustomization.backgroundUri = "file:///sdcard/Pictures/bk.png";
            // p2pCustomization.backgroundUri = "android.resource://com.lyun.user.im/drawable/bk"
            MessagePanelCustomization messagePanelCustomization = new MessagePanelCustomization();
            messagePanelCustomization.setMsgLongClickEnabled(false);

            InputPanelCustomization inputPanelCustomization = new InputPanelCustomization() {
                @Override
                public MsgAttachment createStickerAttachment(String category, String item) {
                    return new StickerAttachment(category, item);
                }
            };

            // 定制InputPanel
            inputPanelCustomization.setShowAudioInputBar(false);
            inputPanelCustomization.setShowEmojiInputBar(false);
            // 定制加号点开后可以包含的操作， 默认已经有图片，视频等消息了
            ArrayList<BaseAction> actions = new ArrayList<>();
            actions.add(new ImageAction());
            inputPanelCustomization.setActions(actions);
            inputPanelCustomization.setWithSticker(false);
            inputPanelCustomization.setMsgInputBoxBackgroud(R.drawable.message_input_edittext_bg);

            p2pCustomization = new SessionCustomization(toolbarCustomization, messagePanelCustomization, inputPanelCustomization);
        }

        return p2pCustomization;
    }

    private static SessionCustomization getMyP2pCustomization() {
        if (myP2pCustomization == null) {

            //定制Toolbar
            ToolbarCustomization toolbarCustomization = new ToolbarCustomization() {
                // 由于需要Activity Result， 所以重载该函数。
                @Override
                public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                    if (requestCode == TeamRequestCode.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                        String result = data.getStringExtra(TeamExtras.RESULT_EXTRA_REASON);
                        if (result == null) {
                            return;
                        }
                        if (result.equals(TeamExtras.RESULT_EXTRA_REASON_CREATE)) {
                            String tid = data.getStringExtra(TeamExtras.RESULT_EXTRA_DATA);
                            if (TextUtils.isEmpty(tid)) {
                                return;
                            }

                            startTeamSession(activity, tid);
                            activity.finish();
                        }
                    }
                }
            };
            // 定制ActionBar右边的按钮，可以加多个
            ArrayList<ToolbarCustomization.OptionsButton> buttons = new ArrayList<>();
            ToolbarCustomization.OptionsButton cloudMsgButton = new ToolbarCustomization.OptionsButton() {
                @Override
                public void onClick(Context context, View view, String sessionId) {
                    initPopuptWindow(context, view, sessionId, SessionTypeEnum.P2P);
                }
            };
            cloudMsgButton.setIconId(R.drawable.nim_ic_messge_history);
            buttons.add(cloudMsgButton);
            toolbarCustomization.setOptionsButtons(buttons);

            //定制MessagePanel
            // 背景
            // p2pCustomization.backgroundColor = Color.BLUE;
            // p2pCustomization.backgroundUri = "file:///android_asset/xx/bk.jpg";
            // p2pCustomization.backgroundUri = "file:///sdcard/Pictures/bk.png";
            // p2pCustomization.backgroundUri = "android.resource://com.lyun.user.im/drawable/bk"
            MessagePanelCustomization messagePanelCustomization = new MessagePanelCustomization();
            messagePanelCustomization.setMsgLongClickEnabled(false);

            InputPanelCustomization inputPanelCustomization = new InputPanelCustomization() {
                @Override
                public MsgAttachment createStickerAttachment(String category, String item) {
                    return new StickerAttachment(category, item);
                }
            };

            // 定制InputPanel
            inputPanelCustomization.setShowAudioInputBar(false);
            inputPanelCustomization.setShowEmojiInputBar(false);
            // 定制加号点开后可以包含的操作， 默认已经有图片，视频等消息了
            ArrayList<BaseAction> actions = new ArrayList<>();
            actions.add(new ImageAction());
            // actions.add(new VideoAction());
            // actions.add(new LocationAction());
            // actions.add(new SnapChatAction());
            // actions.add(new GuessAction());
            // actions.add(new FileAction());
            inputPanelCustomization.setActions(actions);
            inputPanelCustomization.setWithSticker(true);
            inputPanelCustomization.setMsgInputBoxBackgroud(R.drawable.message_input_edittext_bg);

            myP2pCustomization = new SessionCustomization(toolbarCustomization, messagePanelCustomization, inputPanelCustomization);
        }
        return myP2pCustomization;
    }

    private static SessionCustomization getTeamCustomization() {
        if (teamCustomization == null) {

            //定制Toolbar
            ToolbarCustomization toolbarCustomization = new ToolbarCustomization() {
                @Override
                public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                    if (requestCode == TeamRequestCode.REQUEST_CODE) {
                        if (resultCode == Activity.RESULT_OK) {
                            String reason = data.getStringExtra(TeamExtras.RESULT_EXTRA_REASON);
                            boolean finish = reason != null && (reason.equals(TeamExtras
                                    .RESULT_EXTRA_REASON_DISMISS) || reason.equals(TeamExtras.RESULT_EXTRA_REASON_QUIT));
                            if (finish) {
                                activity.finish(); // 退出or解散群直接退出多人会话
                            }
                        }
                    }
                }
            };
            // 定制ActionBar右边的按钮，可以加多个
            ArrayList<ToolbarCustomization.OptionsButton> buttons = new ArrayList<>();
            ToolbarCustomization.OptionsButton cloudMsgButton = new ToolbarCustomization.OptionsButton() {
                @Override
                public void onClick(Context context, View view, String sessionId) {
                    initPopuptWindow(context, view, sessionId, SessionTypeEnum.Team);
                }
            };
            cloudMsgButton.setIconId(R.drawable.nim_ic_messge_history);

            ToolbarCustomization.OptionsButton infoButton = new ToolbarCustomization.OptionsButton() {
                @Override
                public void onClick(Context context, View view, String sessionId) {
                    Team team = TeamDataCache.getInstance().getTeamById(sessionId);
                    if (team != null && team.isMyTeam()) {
                        NimUIKit.startTeamInfo(context, sessionId);
                    } else {
                        Toast.makeText(context, R.string.team_invalid_tip, Toast.LENGTH_SHORT).show();
                    }
                }
            };
            infoButton.setIconId(R.drawable.nim_ic_message_actionbar_team);

            buttons.add(cloudMsgButton);
            buttons.add(infoButton);
            toolbarCustomization.setOptionsButtons(buttons);

            //定制MessagePanel
            // 背景
            // p2pCustomization.backgroundColor = Color.BLUE;
            // p2pCustomization.backgroundUri = "file:///android_asset/xx/bk.jpg";
            // p2pCustomization.backgroundUri = "file:///sdcard/Pictures/bk.png";
            // p2pCustomization.backgroundUri = "android.resource://com.lyun.user.im/drawable/bk"
            MessagePanelCustomization messagePanelCustomization = new MessagePanelCustomization();
            messagePanelCustomization.setMsgLongClickEnabled(false);

            InputPanelCustomization inputPanelCustomization = new InputPanelCustomization() {
                @Override
                public MsgAttachment createStickerAttachment(String category, String item) {
                    return new StickerAttachment(category, item);
                }
            };

            // 定制InputPanel
            inputPanelCustomization.setShowAudioInputBar(false);
            inputPanelCustomization.setShowEmojiInputBar(false);
            // 定制加号点开后可以包含的操作， 默认已经有图片，视频等消息了
            ArrayList<BaseAction> actions = new ArrayList<>();
            actions.add(new ImageAction());
            // actions.add(new VideoAction());
            // actions.add(new LocationAction());
            // actions.add(new GuessAction());
            // actions.add(new FileAction());
            // actions.add(new TipAction());
            inputPanelCustomization.setActions(actions);
            inputPanelCustomization.setWithSticker(false);
            inputPanelCustomization.setMsgInputBoxBackgroud(R.drawable.message_input_edittext_bg);

            teamCustomization = new SessionCustomization(toolbarCustomization, messagePanelCustomization, inputPanelCustomization);
        }

        return teamCustomization;
    }

    private static void registerViewHolders() {
        NimUIKit.registerMsgItemViewHolder(FileAttachment.class, MsgViewHolderFile.class);
        NimUIKit.registerMsgItemViewHolder(AVChatAttachment.class, MsgViewHolderAVChat.class);
        NimUIKit.registerMsgItemViewHolder(GuessAttachment.class, MsgViewHolderGuess.class);
        NimUIKit.registerMsgItemViewHolder(CustomAttachment.class, MsgViewHolderDefCustom.class);
        NimUIKit.registerMsgItemViewHolder(StickerAttachment.class, MsgViewHolderSticker.class);
        NimUIKit.registerMsgItemViewHolder(SnapChatAttachment.class, MsgViewHolderSnapChat.class);
        //NimUIKit.registerMsgItemViewHolder(RTSAttachment.class, MsgViewHolderRTS.class);
        NimUIKit.registerTipMsgViewHolder(MsgViewHolderTip.class);
    }

    private static void setSessionListener() {
        SessionEventListener listener = new SessionEventListener() {
            @Override
            public void onAvatarClicked(Context context, IMMessage message) {
                // 一般用于打开用户资料页面
                UserProfileActivity.start(context, message.getFromAccount());
            }

            @Override
            public void onAvatarLongClicked(Context context, IMMessage message) {
                // 一般用于群组@功能，或者弹出菜单，做拉黑，加好友等功能
            }
        };

        NimUIKit.setSessionListener(listener);
    }


    /**
     * 消息转发过滤器
     */
    private static void registerMsgForwardFilter() {
        NimUIKit.setMsgForwardFilter(new MsgForwardFilter() {
            @Override
            public boolean shouldIgnore(IMMessage message) {
                if (message.getDirect() == MsgDirectionEnum.In
                        && (message.getAttachStatus() == AttachStatusEnum.transferring
                        || message.getAttachStatus() == AttachStatusEnum.fail)) {
                    // 接收到的消息，附件没有下载成功，不允许转发
                    return true;
                } else if (message.getMsgType() == MsgTypeEnum.custom && message.getAttachment() != null
                        && (message.getAttachment() instanceof SnapChatAttachment)) {
                    //|| message.getAttachment() instanceof RTSAttachment)) {
                    // 白板消息和阅后即焚消息 不允许转发
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * 消息撤回过滤器
     */
    private static void registerMsgRevokeFilter() {
        NimUIKit.setMsgRevokeFilter(new MsgRevokeFilter() {
            @Override
            public boolean shouldIgnore(IMMessage message) {
                if (message.getAttachment() != null
                        && (message.getAttachment() instanceof AVChatAttachment)) {
                    //|| message.getAttachment() instanceof RTSAttachment)) {
                    // 视频通话消息和白板消息 不允许撤回
                    return true;
                } else if (NimCache.getAccount().equals(message.getSessionId())) {
                    // 发给我的电脑 不允许撤回
                    return true;
                }
                return false;
            }
        });
    }

    private static void registerMsgRevokeObserver() {
        NIMClient.getService(MsgServiceObserve.class).observeRevokeMessage(new Observer<IMMessage>() {
            @Override
            public void onEvent(IMMessage message) {
                if (message == null) {
                    return;
                }

                MessageHelper.getInstance().onRevokeMessage(message);
            }
        }, true);
    }


    private static void initPopuptWindow(Context context, View view, String sessionId, SessionTypeEnum sessionTypeEnum) {
        if (popupMenu == null) {
            menuItemList = new ArrayList<>();
            popupMenu = new NIMPopupMenu(context, menuItemList, listener);
        }
        menuItemList.clear();
        menuItemList.addAll(getMoreMenuItems(context, sessionId, sessionTypeEnum));
        popupMenu.notifyData();
        popupMenu.show(view);
    }

    private static NIMPopupMenu.MenuItemClickListener listener = new NIMPopupMenu.MenuItemClickListener() {
        @Override
        public void onItemClick(final PopupMenuItem item) {
            switch (item.getTag()) {
                case ACTION_HISTORY_QUERY:
                    MessageHistoryActivity.start(item.getContext(), item.getSessionId(), item.getSessionTypeEnum()); // 漫游消息查询
                    break;
                case ACTION_SEARCH_MESSAGE:
                    SearchMessageActivity.start(item.getContext(), item.getSessionId(), item.getSessionTypeEnum());
                    break;
                case ACTION_CLEAR_MESSAGE:
                    EasyAlertDialogHelper.createOkCancelDiolag(item.getContext(), null, "确定要清空吗？", true, new EasyAlertDialogHelper.OnDialogActionListener() {
                        @Override
                        public void doCancelAction() {

                        }

                        @Override
                        public void doOkAction() {
                            NIMClient.getService(MsgService.class).clearChattingHistory(item.getSessionId(), item.getSessionTypeEnum());
                            MessageListPanelHelper.getInstance().notifyClearMessages(item.getSessionId());
                        }
                    }).show();
                    break;
            }
        }
    };

    private static List<PopupMenuItem> getMoreMenuItems(Context context, String sessionId, SessionTypeEnum sessionTypeEnum) {
        List<PopupMenuItem> moreMenuItems = new ArrayList<PopupMenuItem>();
        moreMenuItems.add(new PopupMenuItem(context, ACTION_HISTORY_QUERY, sessionId,
                sessionTypeEnum, NimCache.getContext().getString(R.string.message_history_query)));
        moreMenuItems.add(new PopupMenuItem(context, ACTION_SEARCH_MESSAGE, sessionId,
                sessionTypeEnum, NimCache.getContext().getString(R.string.message_search_title)));
        moreMenuItems.add(new PopupMenuItem(context, ACTION_CLEAR_MESSAGE, sessionId,
                sessionTypeEnum, NimCache.getContext().getString(R.string.message_clear)));
        return moreMenuItems;
    }
}
