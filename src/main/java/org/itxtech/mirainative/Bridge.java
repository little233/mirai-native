package org.itxtech.mirainative;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.utils.MiraiLogger;
import org.itxtech.mirainative.message.MessageCache;
import org.itxtech.mirainative.plugin.Event;
import org.itxtech.mirainative.plugin.NativePlugin;
import org.itxtech.mirainative.plugin.PluginInfo;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;

/*
 *
 * Mirai Native
 *
 * Copyright (C) 2020 iTX Technologies
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author PeratX
 * @website https://github.com/iTXTech/mirai-native
 *
 */
public class Bridge {
    public static final int PRI_MSG_SUBTYPE_FRIEND = 11;

    public static final int PERM_SUBTYPE_CANCEL_ADMIN = 1;
    public static final int PERM_SUBTYPE_SET_ADMIN = 2;

    public static final int MEMBER_LEAVE_QUIT = 1;
    public static final int MEMBER_LEAVE_KICK = 2;

    public static final int MEMBER_JOIN_PERMITTED = 1;
    public static final int MEMBER_JOIN_INVITED_BY_ADMIN = 2;

    public static final int GROUP_UNMUTE = 1;
    public static final int GROUP_MUTE = 2;

    // Plugin
    public static int loadPlugin(NativePlugin plugin) {
        int code = loadNativePlugin(plugin.getFile().getAbsolutePath().replace("\\", "\\\\"), plugin.getId());
        if (plugin.getPluginInfo() != null) {
            PluginInfo info = plugin.getPluginInfo();
            getLogger().info("Native Plugin (w json) " + info.getName() + " has been loaded with code " + code);
        } else {
            getLogger().info("Native Plugin (w/o json) " + plugin.getFile().getName() + " has been loaded with code " + code);
        }
        return code;
    }

    public static int unloadPlugin(NativePlugin plugin) {
        int code = freeNativePlugin(plugin.getId());
        getLogger().info("Native Plugin " + plugin.getId() + " has been unloaded with code " + code);
        return code;
    }

    public static void disablePlugin(NativePlugin plugin) {
        if (plugin.getLoaded() && plugin.getEnabled()) {
            if (plugin.shouldCallEvent(Event.EVENT_DISABLE, true)) {
                callIntMethod(plugin.getId(), plugin.getEventOrDefault(Event.EVENT_DISABLE, "_eventDisable"));
            }
        }
    }

    public static void enablePlugin(NativePlugin plugin) {
        if (plugin.getLoaded() && !plugin.getEnabled()) {
            if (plugin.shouldCallEvent(Event.EVENT_ENABLE, true)) {
                callIntMethod(plugin.getId(), plugin.getEventOrDefault(Event.EVENT_ENABLE, "_eventEnable"));
            }
        }
    }

    public static void startPlugin(NativePlugin plugin) {
        if (plugin.shouldCallEvent(Event.EVENT_STARTUP, true)) {
            callIntMethod(plugin.getId(), plugin.getEventOrDefault(Event.EVENT_STARTUP, "_eventStartup"));
        }
    }

    public static void exitPlugin(NativePlugin plugin) {
        if (plugin.shouldCallEvent(Event.EVENT_EXIT, true)) {
            callIntMethod(plugin.getId(), plugin.getEventOrDefault(Event.EVENT_EXIT, "_eventExit"));
        }
    }

    public static void updateInfo(NativePlugin plugin) {
        String info = callStringMethod(plugin.getId(), "AppInfo");
        if (!"".equals(info)) {
            plugin.setInfo(info);
        }
    }

    // Events
    public static void eventPrivateMessage(int subType, int msgId, long fromAccount, String msg, int font) {
        for (NativePlugin plugin : getPlugins().values()) {
            if (plugin.shouldCallEvent(Event.EVENT_PRI_MSG) && pEvPrivateMessage(plugin.getId(),
                    plugin.getEventOrDefault(Event.EVENT_PRI_MSG, "_eventPrivateMsg"),
                    subType, msgId, fromAccount, plugin.processMessage(Event.EVENT_PRI_MSG, msg), font) == 1) {
                break;
            }
        }
    }

    public static void eventGroupMessage(int subType, int msgId, long fromGroup, long fromAccount, String fromAnonymous, String msg, int font) {
        for (NativePlugin plugin : getPlugins().values()) {
            if (plugin.shouldCallEvent(Event.EVENT_GROUP_MSG) && pEvGroupMessage(plugin.getId(),
                    plugin.getEventOrDefault(Event.EVENT_GROUP_MSG, "_eventGroupMsg"),
                    subType, msgId, fromGroup, fromAccount, fromAnonymous, plugin.processMessage(Event.EVENT_GROUP_MSG, msg), font) == 1) {
                break;
            }
        }
    }

    public static void eventGroupAdmin(int subType, int time, long fromGroup, long beingOperateAccount) {
        for (NativePlugin plugin : getPlugins().values()) {
            if (plugin.shouldCallEvent(Event.EVENT_GROUP_ADMIN) && pEvGroupAdmin(plugin.getId(),
                    plugin.getEventOrDefault(Event.EVENT_GROUP_ADMIN, "_eventSystem_GroupAdmin"),
                    subType, time, fromGroup, beingOperateAccount) == 1) {
                break;
            }
        }
    }

    public static void eventGroupMemberLeave(int subType, int time, long fromGroup, long fromAccount, long beingOperateAccount) {
        for (NativePlugin plugin : getPlugins().values()) {
            if (plugin.shouldCallEvent(Event.EVENT_GROUP_MEMBER_DEC) && pEvGroupMember(plugin.getId(),
                    plugin.getEventOrDefault(Event.EVENT_GROUP_MEMBER_DEC, "_eventSystem_GroupMemberDecrease"),
                    subType, time, fromGroup, fromAccount, beingOperateAccount) == 1) {
                break;
            }
        }
    }

    public static void eventGroupBan(int subType, int time, long fromGroup, long fromAccount, long beingOperateAccount, long duration) {
        for (NativePlugin plugin : getPlugins().values()) {
            if (plugin.shouldCallEvent(Event.EVENT_GROUP_BAN) && pEvGroupBan(plugin.getId(),
                    plugin.getEventOrDefault(Event.EVENT_GROUP_BAN, "_eventSystem_GroupBan"),
                    subType, time, fromGroup, fromAccount, beingOperateAccount, duration) == 1) {
                break;
            }
        }
    }

    public static void eventGroupMemberJoin(int subType, int time, long fromGroup, long fromAccount, long beingOperateAccount) {
        for (NativePlugin plugin : getPlugins().values()) {
            if (plugin.shouldCallEvent(Event.EVENT_GROUP_MEMBER_INC) && pEvGroupMember(plugin.getId(),
                    plugin.getEventOrDefault(Event.EVENT_GROUP_MEMBER_INC, "_eventSystem_GroupMemberIncrease"),
                    subType, time, fromGroup, fromAccount, beingOperateAccount) == 1) {
                break;
            }
        }
    }

    // Native

    public static native int loadNativePlugin(String file, int id);

    public static native int freeNativePlugin(int id);

    public static native int pEvPrivateMessage(int pluginId, String name, int subType, int msgId, long fromAccount, String msg, int font);

    public static native int pEvGroupMessage(int pluginId, String name, int subType, int msgId, long fromGroup, long fromAccount, String fromAnonymous, String msg, int font);

    public static native int pEvGroupAdmin(int pluginId, String name, int subType, int time, long fromGroup, long beingOperateAccount);

    public static native int pEvGroupMember(int pluginId, String name, int subType, int time, long fromGroup, long fromAccount, long beingOperateAccount);

    public static native int pEvGroupBan(int pluginId, String name, int subType, int time, long fromGroup, long fromAccount, long beingOperateAccount, long duration);

    public static native int callIntMethod(int pluginId, String name);

    public static native String callStringMethod(int pluginId, String name);

    public static native void processMessage();

    // Helper

    private static HashMap<Integer, NativePlugin> getPlugins() {
        return PluginManager.INSTANCE.getPlugins();
    }

    private static NativePlugin getPlugin(int pluginId) {
        return getPlugins().get(pluginId);
    }

    private static MiraiLogger getLogger() {
        return MiraiNative.INSTANCE.getLogger();
    }

    private static Bot getBot() {
        return MiraiNative.INSTANCE.getBot();
    }

    // Bridge

    @NativeBridgeMethod
    public static int sendFriendMessage(int pluginId, long account, String msg) {
        return BridgeHelper.sendFriendMessage(account, msg);
    }

    @NativeBridgeMethod
    public static int sendGroupMessage(int pluginId, long group, String msg) {
        return BridgeHelper.sendGroupMessage(group, msg);
    }

    @NativeBridgeMethod
    public static void addLog(int pluginId, int priority, String type, String content) {
        NativeLoggerHelper.log(getPlugin(pluginId), priority, type, content);
    }

    @NativeBridgeMethod
    public static String getPluginDataDir(int pluginId) {
        return getPlugin(pluginId).getAppDir().getAbsolutePath() + File.separatorChar;
    }

    @NativeBridgeMethod
    public static long getLoginQQ(int pluginId) {
        return getBot().getUin();
    }

    @NativeBridgeMethod
    public static String getLoginNick(int pluginId) {
        return getBot().getNick();
    }

    @NativeBridgeMethod
    public static int setGroupBan(int pluginId, long group, long member, long duration) {
        BridgeHelper.setGroupBan(group, member, (int) duration);
        return 0;
    }

    @NativeBridgeMethod
    public static int setGroupCard(int pluginId, long group, long member, String card) {
        getBot().getGroup(pluginId).get(member).setNameCard(card);
        return 0;
    }

    @NativeBridgeMethod
    public static int setGroupKick(int pluginId, long group, long member, boolean reject) {
        BridgeHelper.setGroupKick(group, member);
        return 0;
    }

    @NativeBridgeMethod
    public static int setGroupLeave(int pluginId, long group, boolean dismiss) {
        BridgeHelper.setGroupLeave(group);
        return 0;
    }

    @NativeBridgeMethod
    public static int setGroupSpecialTitle(int pluginId, long group, long member, String title, long duration) {
        getBot().getGroup(pluginId).get(member).setSpecialTitle(title);
        return 0;
    }

    @NativeBridgeMethod
    public static int setGroupWholeBan(int pluginId, long group, boolean enable) {
        getBot().getGroup(group).getSettings().setMuteAll(enable);
        return 0;
    }

    @NativeBridgeMethod
    public static int recallMsg(int pluginId, long msgId) {
        return MessageCache.INSTANCE.recall(Long.valueOf(msgId).intValue()) ? 0 : -1;
    }

    @NativeBridgeMethod
    public static String getFriendList(int pluginId, boolean reserved) {
        return BridgeHelper.getFriendList();
    }

    @NativeBridgeMethod
    public static String getGroupInfo(int pluginId, long groupId, boolean cache) {
        return BridgeHelper.getGroupInfo(groupId);
    }

    @NativeBridgeMethod
    public static String getGroupList(int pluginId) {
        return BridgeHelper.getGroupList();
    }

    @NativeBridgeMethod
    public static String getGroupMemberInfo(int pluginId, long group, long member, boolean cache) {
        return BridgeHelper.getGroupMemberInfo(group, member);
    }

    @NativeBridgeMethod
    public static String getGroupMemberList(int pluginId, long group) {
        return BridgeHelper.getGroupMemberList(group);
    }

    // Placeholder methods which mirai hasn't supported yet

    @NativeBridgeMethod
    public static int setGroupAnonymous(int pluginId, long group, boolean enable) {
        return 0;
    }

    @NativeBridgeMethod
    public static String getCookies(int pluginId, String domain) {
        return "";
    }

    @NativeBridgeMethod
    public static String getCsrfToken(int pluginId) {
        return "";
    }

    @NativeBridgeMethod
    public static String getImage(int pluginId, String image) {
        return "";
    }

    @NativeBridgeMethod
    public static String getRecord(int pluginId, String file, String format) {
        return "";
    }

    @NativeBridgeMethod
    public static String getStrangerInfo(int pluginId, long account, boolean cache) {
        return "";
    }

    @NativeBridgeMethod
    public static int sendDiscussMessage(int pluginId, long group, String msg) {
        return 0;
    }

    @NativeBridgeMethod
    public static int setDiscussLeave(int pluginId, long group) {
        return 0;
    }

    @NativeBridgeMethod
    public static int setFriendAddRequest(int pluginId, String requestId, int type, String remark) {
        return 0;
    }

    @NativeBridgeMethod
    public static int setGroupAddRequest(int pluginId, String requestId, int reqType, int fbType, String reason) {
        return 0;
    }

    @NativeBridgeMethod
    public static int setGroupAdmin(int pluginId, long group, long account, boolean admin) {
        //true => set, false => revoke
        return 0;
    }

    @NativeBridgeMethod
    public static int setGroupAnonymousBan(int pluginId, long group, String id, long duration) {
        return 0;
    }

    // Wont' Implement

    @NativeBridgeMethod
    public static int sendLike(int pluginId, long account, int times) {
        return 0;
    }

    // Legacy Methods

    // TODO

    // Mirai Unique Methods

    @NativeBridgeMethod
    public static int quoteMessage(int pluginId, int msgId, String msg) {
        return BridgeHelper.quoteMessage(msgId, msg);
    }

    // Annotation

    /**
     * Indicates the method is called from native code
     */
    @Retention(value = RetentionPolicy.SOURCE)
    @interface NativeBridgeMethod {
    }

    // Logger

    static class NativeLoggerHelper {
        public static final int LOG_DEBUG = 0;
        public static final int LOG_INFO = 10;
        public static final int LOG_INFO_SUCC = 11;
        public static final int LOG_INFO_RECV = 12;
        public static final int LOG_INFO_SEND = 13;
        public static final int LOG_WARNING = 20;
        public static final int LOG_ERROR = 21;
        public static final int LOG_FATAL = 22;

        static void log(NativePlugin plugin, int priority, String type, String content) {
            String c = "[NP " + plugin.getIdentifier();
            if (!"".equals(type)) {
                c += " " + type;
            }
            c += "] " + content;
            switch (priority) {
                case LOG_DEBUG:
                    getLogger().debug(c);
                    break;
                case LOG_INFO:
                case LOG_INFO_RECV:
                case LOG_INFO_SUCC:
                case LOG_INFO_SEND:
                    getLogger().info(c);
                    break;
                case LOG_WARNING:
                    getLogger().warning(c);
                    break;
                case LOG_ERROR:
                    getLogger().error(c);
                    break;
                case LOG_FATAL:
                    getLogger().error("[FATAL]" + c);
                    break;
            }
        }
    }
}
