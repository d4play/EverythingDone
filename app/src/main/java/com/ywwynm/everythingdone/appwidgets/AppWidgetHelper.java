package com.ywwynm.everythingdone.appwidgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.ywwynm.everythingdone.App;
import com.ywwynm.everythingdone.Def;
import com.ywwynm.everythingdone.R;
import com.ywwynm.everythingdone.activities.AuthenticationActivity;
import com.ywwynm.everythingdone.activities.DetailActivity;
import com.ywwynm.everythingdone.activities.ThingsActivity;
import com.ywwynm.everythingdone.appwidgets.list.ThingsListWidget;
import com.ywwynm.everythingdone.appwidgets.single.BaseThingWidget;
import com.ywwynm.everythingdone.appwidgets.single.ThingWidgetLarge;
import com.ywwynm.everythingdone.appwidgets.single.ThingWidgetMiddle;
import com.ywwynm.everythingdone.appwidgets.single.ThingWidgetSmall;
import com.ywwynm.everythingdone.appwidgets.single.ThingWidgetTiny;
import com.ywwynm.everythingdone.database.AppWidgetDAO;
import com.ywwynm.everythingdone.database.HabitDAO;
import com.ywwynm.everythingdone.database.ReminderDAO;
import com.ywwynm.everythingdone.helpers.AttachmentHelper;
import com.ywwynm.everythingdone.helpers.CheckListHelper;
import com.ywwynm.everythingdone.managers.ThingManager;
import com.ywwynm.everythingdone.model.Habit;
import com.ywwynm.everythingdone.model.Reminder;
import com.ywwynm.everythingdone.model.Thing;
import com.ywwynm.everythingdone.model.ThingWidgetInfo;
import com.ywwynm.everythingdone.appwidgets.single.ChecklistWidgetService;
import com.ywwynm.everythingdone.appwidgets.list.ThingsListWidgetService;
import com.ywwynm.everythingdone.receivers.HabitWidgetActionReceiver;
import com.ywwynm.everythingdone.receivers.ReminderNotificationActionReceiver;
import com.ywwynm.everythingdone.utils.DateTimeUtil;
import com.ywwynm.everythingdone.utils.DisplayUtil;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by ywwynm on 2016/7/27.
 * helper class for app widgets, especially for showing thing
 */
public class AppWidgetHelper {

    public static final String TAG = "AppWidgetHelper";

    private static final float screenDensity = DisplayUtil.getScreenDensity(App.getApp());

    private static final int dp12 = (int) (screenDensity * 12);

    private static final int ROOT_WIDGET_THING        = R.id.root_widget_thing;

    private static final int IV_IMAGE_ATTACHMENT      = R.id.iv_thing_image;
    private static final int TV_IMAGE_COUNT           = R.id.tv_thing_image_attachment_count;

    private static final int TV_TITLE                 = R.id.tv_thing_title;
    private static final int IV_PRIVATE_THING         = R.id.iv_private_thing;

    private static final int TV_CONTENT               = R.id.tv_thing_content;

    private static final int LV_CHECKLIST             = R.id.lv_thing_check_list;
    private static final int LL_CHECK_LIST_ITEMS      = R.id.ll_check_list_items_container;
    private static final int LL_CHECK_LIST_ITEM_ROOT  = R.id.ll_check_list_tv;
    private static final int IV_STATE_CHECK_LIST      = R.id.iv_check_list_state;
    private static final int TV_CONTENT_CHECK_LIST    = R.id.tv_check_list;

    private static final int LL_AUDIO_ATTACHMENT      = R.id.ll_thing_audio_attachment;
    private static final int TV_AUDIO_COUNT           = R.id.tv_thing_audio_attachment_count;

    private static final int RL_REMINDER              = R.id.rl_thing_reminder;
    private static final int V_REMINDER_SEPARATOR     = R.id.view_reminder_separator;
    private static final int IV_REMINDER              = R.id.iv_thing_reminder;
    private static final int TV_REMINDER_TIME         = R.id.tv_thing_reminder_time;

    private static final int RL_HABIT                 = R.id.rl_thing_habit;
    private static final int V_HABIT_SEPARATOR_1      = R.id.view_habit_separator_1;
    private static final int TV_HABIT_SUMMARY         = R.id.tv_thing_habit_summary;
    private static final int TV_HABIT_NEXT_REMINDER   = R.id.tv_thing_habit_next_reminder;
    private static final int V_HABIT_SEPARATOR_2      = R.id.view_habit_separator_2;
    private static final int LL_HABIT_RECORD          = R.id.ll_thing_habit_record;
    private static final int TV_HABIT_LAST_FIVE       = R.id.tv_thing_habit_last_five_record;
    private static final int TV_HABIT_FINISHED_THIS_T = R.id.tv_thing_habit_finished_this_t;

    private static final int RL_THING_STATE           = R.id.rl_thing_state;
    private static final int V_STATE_SEPARATOR        = R.id.view_state_separator;
    private static final int TV_THING_STATE           = R.id.tv_thing_state;
    private static final int IV_THING_STATE           = R.id.iv_thing_state;

    private static final int LL_THING_ACTION          = R.id.ll_thing_action;
    private static final int TV_THING_ACTION          = R.id.tv_thing_action;

    private static final int V_PADDING_BOTTOM         = R.id.view_thing_padding_bottom;

    private static final int LV_THINGS_LIST           = R.id.lv_things_list;
    private static final int LL_THINGS_LIST_HEADER    = R.id.ll_things_list_header;
    private static final int TV_THINGS_LIST_TITLE     = R.id.tv_things_list_title;
    private static final int IV_THINGS_LIST_CREATE    = R.id.iv_things_list_create;

    private AppWidgetHelper() {}

    /**
     * Update single thing widgets whose UI components are bind with a {@link Thing} with {@param thingId}.
     * This method should be called at any time if a {@link Thing} is updated by user(simple
     * behaviors include updating in DetailActivity, swipe in ThingsActivity and so on; More
     * complicated behaviors include click checklist item in single thing appwidget...), or by app
     * itself(for example, daily update for habits).
     * @param thingId this param will be used to find appwidgets from database to update
     */
    public static void updateSingleThingAppWidgets(Context context, long thingId) {
        AppWidgetDAO appWidgetDAO = AppWidgetDAO.getInstance(context);
        List<ThingWidgetInfo> thingWidgetInfos = appWidgetDAO.getThingWidgetInfosByThingId(thingId);
        for (ThingWidgetInfo thingWidgetInfo : thingWidgetInfos) {
            Intent intent = new Intent(context, getProviderClassBySize(thingWidgetInfo.getSize()));
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                    new int[] { thingWidgetInfo.getId() });
            context.sendBroadcast(intent);
        }
    }

    /**
     * Update things list widget whose UI components are bind with a list of things under {@param limit}.
     * This method should be called at any time if any thing was created/updated by user or by app
     * itself. Please see {@link #updateSingleThingAppWidgets(Context, long)}'s annotation to get
     * examples for these actions. Especially, this method should be called after user create new thing
     * from create appwidget or things list appwidget, which won't influence single thing appwidget.
     * @param limit this param will be used to find appwidgets from database to update.
     */
    public static void updateThingsListAppWidgets(Context context, int limit) {
        AppWidgetDAO appWidgetDAO = AppWidgetDAO.getInstance(context);
        int storedLimit = -limit - 1;
        List<ThingWidgetInfo> thingWidgetInfos = appWidgetDAO.getThingWidgetInfosByThingId(storedLimit);
        for (ThingWidgetInfo thingWidgetInfo : thingWidgetInfos) {
            Intent intent = new Intent(context, ThingsListWidget.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                    new int[] { thingWidgetInfo.getId() });
            context.sendBroadcast(intent);
        }
    }

    public static void updateThingsListAppWidgetsForType(Context context, @Thing.Type int type) {
        int[] limits = Thing.getLimits(type, Thing.UNDERWAY);
        for (int limit : limits) {
            updateThingsListAppWidgets(context, limit);
        }
    }

    public static void updateAllThingsListAppWidgets(Context context) {
        for (int limit = Def.LimitForGettingThings.ALL_UNDERWAY;
             limit <= Def.LimitForGettingThings.GOAL_UNDERWAY; limit++) {
            updateThingsListAppWidgets(context, limit);
        }
    }

    public static void updateAllAppWidgets(Context context) {
        AppWidgetDAO dao = AppWidgetDAO.getInstance(context);
        List<ThingWidgetInfo> thingWidgetInfos = dao.getAllThingWidgetInfos();
        for (ThingWidgetInfo thingWidgetInfo : thingWidgetInfos) {
            long thingId = thingWidgetInfo.getThingId();
            Intent intent;
            if (thingId < 0) { // for things list widgets
                intent = new Intent(context, ThingsListWidget.class);
            } else {
                intent = new Intent(context, getProviderClassBySize(thingWidgetInfo.getSize()));
            }
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                    new int[] { thingWidgetInfo.getId() });
            context.sendBroadcast(intent);
        }
    }

    public static Class getProviderClassBySize(@ThingWidgetInfo.Size int size) {
        if (size == ThingWidgetInfo.SIZE_TINY) {
            return ThingWidgetTiny.class;
        } else if (size == ThingWidgetInfo.SIZE_SMALL) {
            return ThingWidgetSmall.class;
        } else if (size == ThingWidgetInfo.SIZE_MIDDLE) {
            return ThingWidgetMiddle.class;
        } else if (size == ThingWidgetInfo.SIZE_LARGE) {
            return ThingWidgetLarge.class;
        }
        return BaseThingWidget.class;
    }

    public static @ThingWidgetInfo.Size int getSizeByProviderClass(Class clazz) {
        if (clazz.equals(ThingWidgetTiny.class)) {
            return ThingWidgetInfo.SIZE_TINY;
        } else if (clazz.equals(ThingWidgetSmall.class)) {
            return ThingWidgetInfo.SIZE_SMALL;
        } else if (clazz.equals(ThingWidgetMiddle.class)) {
            return ThingWidgetInfo.SIZE_MIDDLE;
        } else if (clazz.equals(ThingWidgetLarge.class)) {
            return ThingWidgetInfo.SIZE_LARGE;
        }
        return ThingWidgetInfo.SIZE_MIDDLE;
    }

    public static RemoteViews createRemoteViewsForSingleThing(
            Context context, Thing thing, int position, int appWidgetId, Class clazz) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget_thing);
        setAppearance(context, remoteViews, thing, appWidgetId, clazz);
        final Intent contentIntent = AuthenticationActivity.getOpenIntent(
                context, TAG, thing.getId(), position);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, (int) thing.getId(), contentIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.root_widget_thing, pendingIntent);
        return remoteViews;
    }

    public static RemoteViews createRemoteViewsForThingsList(Context context, int limit, int appWidgetId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget_things_list);

        remoteViews.setTextViewText(TV_THINGS_LIST_TITLE, getStringForLimit(context, limit));

        Intent intent = new Intent(context, ThingsActivity.class);
        intent.putExtra(Def.Communication.KEY_LIMIT, limit);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(LL_THINGS_LIST_HEADER, pendingIntent);

        // create image view click event
        intent = DetailActivity.getOpenIntentForCreate(context, TAG, 0);
        intent.putExtra(Def.Communication.KEY_LIMIT, limit);
        pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(IV_THINGS_LIST_CREATE, pendingIntent);

        // adapter for things
        intent = new Intent(context, ThingsListWidgetService.class);
        intent.putExtra(Def.Communication.KEY_LIMIT, limit);
        intent.putExtra(Def.Communication.KEY_WIDGET_ID, appWidgetId);
        // Very important! without this line, things list widgets of two different limits
        // may have same ListView content
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        remoteViews.setRemoteAdapter(LV_THINGS_LIST, intent);
        // don't set empty view since I want to show NOTIFY_EMPTY-type things

        // thing item click event
        intent = new Intent(context, AuthenticationActivity.class);
        intent.putExtra(Def.Communication.KEY_SENDER_NAME, TAG);
        intent.putExtra(Def.Communication.KEY_DETAIL_ACTIVITY_TYPE,
                DetailActivity.UPDATE);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(LV_THINGS_LIST, pendingIntent);

        return remoteViews;
    }

    private static String getStringForLimit(Context context, int limit) {
        if (limit < Def.LimitForGettingThings.ALL_UNDERWAY
                || limit > Def.LimitForGettingThings.GOAL_UNDERWAY) {
            return null;
        }
        int[] resources = {
                R.string.underway,
                R.string.note,
                R.string.reminder,
                R.string.habit,
                R.string.goal
        };
        return context.getString(resources[limit]);
    }

    public static RemoteViews createRemoteViewsForThingsListItem(
            Context context, Thing thing, int appWidgetId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.app_widget_item_thing);
        setAppearance(context, remoteViews, thing, appWidgetId, ThingsListWidget.class);
        return remoteViews;
    }

    public static RemoteViews createRemoteViewsForChecklistItem(
            Context context, String item, int itemsSize) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.check_list_tv);

        int white_76 = ContextCompat.getColor(context, R.color.white_76p);
        int white_50 = Color.parseColor("#80FFFFFF");

        rv.setViewPadding(LL_CHECK_LIST_ITEM_ROOT, (int) (-6 * screenDensity), 0, 0, 0);

        char state = item.charAt(0);
        String text = item.substring(1, item.length());
        if (state == '0') {
            rv.setImageViewResource(IV_STATE_CHECK_LIST, R.drawable.checklist_unchecked_card);
            rv.setTextColor(TV_CONTENT_CHECK_LIST, white_76);
            rv.setTextViewText(TV_CONTENT_CHECK_LIST, text);
        } else if (state == '1') {
            rv.setImageViewResource(IV_STATE_CHECK_LIST, R.drawable.checklist_checked_card);
            rv.setTextColor(TV_CONTENT_CHECK_LIST, white_50);
            SpannableString spannable = new SpannableString(text);
            spannable.setSpan(new StrikethroughSpan(), 0, text.length(),
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            rv.setTextViewText(TV_CONTENT_CHECK_LIST, spannable);
        }

        if (itemsSize >= 8) {
            rv.setTextViewTextSize(TV_CONTENT_CHECK_LIST, TypedValue.COMPLEX_UNIT_SP, 14);
            rv.setViewPadding(TV_CONTENT_CHECK_LIST, 0, (int) (screenDensity * 2), 0, 0);
        } else {
            float textSize = -4 * itemsSize / 7f + 130f / 7;
            rv.setTextViewTextSize(TV_CONTENT_CHECK_LIST, TypedValue.COMPLEX_UNIT_SP, textSize);
            float mt = - 2 * textSize / 3 + 34f / 3;
            rv.setViewPadding(TV_CONTENT_CHECK_LIST, 0, (int) mt, 0, 0);
        }
        return rv;
    }

    private static void setAppearance(
            Context context, RemoteViews remoteViews, Thing thing, int appWidgetId, Class clazz) {
        remoteViews.setInt(ROOT_WIDGET_THING, "setBackgroundColor", thing.getColor());

        setImageAttachment(context, remoteViews, thing, appWidgetId, clazz);
        setTitleAndPrivate(remoteViews, thing);
        setContent(context, remoteViews, thing, appWidgetId, clazz);
        setAudioAttachment(context, remoteViews, thing);

        setState(context, remoteViews, thing);
        setAction(context, remoteViews, thing, clazz);

        setReminder(context, remoteViews, thing);
        setHabit(context, remoteViews, thing);
    }

    private static void setSeparatorVisibilities(
            RemoteViews remoteViews, int visibility) {
        remoteViews.setViewVisibility(V_STATE_SEPARATOR,    visibility);
        remoteViews.setViewVisibility(V_REMINDER_SEPARATOR, visibility);
        remoteViews.setViewVisibility(V_HABIT_SEPARATOR_1,  visibility);
    }

    private static void setImageAttachment(
            Context context, RemoteViews remoteViews, Thing thing, int appWidgetId, Class clazz) {
        if (thing.isPrivate()) {
            remoteViews.setViewVisibility(IV_IMAGE_ATTACHMENT, View.GONE);
            remoteViews.setViewVisibility(TV_IMAGE_COUNT,      View.GONE);
            remoteViews.setViewVisibility(V_PADDING_BOTTOM,    View.VISIBLE);
            return;
        }

        String attachment = thing.getAttachment();
        String firstImageTypePathName = AttachmentHelper.getFirstImageTypePathName(attachment);
        if (firstImageTypePathName == null) {
            remoteViews.setViewVisibility(IV_IMAGE_ATTACHMENT,  View.GONE);
            remoteViews.setViewVisibility(TV_IMAGE_COUNT,       View.GONE);
            remoteViews.setViewVisibility(V_PADDING_BOTTOM,     View.VISIBLE);
            return;
        }

        remoteViews.setViewVisibility(IV_IMAGE_ATTACHMENT,  View.VISIBLE);
        remoteViews.setViewVisibility(TV_IMAGE_COUNT,       View.VISIBLE);

        final String pathName = firstImageTypePathName.substring(1, firstImageTypePathName.length());
        if (clazz.getSuperclass().equals(BaseThingWidget.class)) {
            loadImageForSingleThing(context, pathName, remoteViews, appWidgetId);
        } else {
            loadImageForThingsListItem(context, pathName, remoteViews);
        }

        remoteViews.setTextViewText(TV_IMAGE_COUNT,
                AttachmentHelper.getImageAttachmentCountStr(attachment, context));

        remoteViews.setViewVisibility(V_PADDING_BOTTOM, View.GONE);
        setSeparatorVisibilities(remoteViews, View.GONE);
    }

    private static void loadImageForSingleThing(
            Context context, String pathName, RemoteViews remoteViews, int appWidgetId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        Glide.with(context)
                .load(pathName)
                .asBitmap()
                .override(options.outWidth, options.outWidth * 3 / 4)
                .centerCrop()
                .into(new AppWidgetTarget(
                        context, remoteViews, IV_IMAGE_ATTACHMENT, new int[] { appWidgetId }));
    }

    private static void loadImageForThingsListItem(
            Context context, String pathName, RemoteViews remoteViews) {
        int width  = (int) (screenDensity * 180);
        int height = width * 3 / 4;
        BitmapRequestBuilder builder =
                Glide.with(context)
                        .load(pathName)
                        .asBitmap()
                        .centerCrop();
        FutureTarget futureTarget = builder.into(width, height);
        try {
            remoteViews.setImageViewBitmap(IV_IMAGE_ATTACHMENT, (Bitmap) futureTarget.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static void setTitleAndPrivate(RemoteViews remoteViews, Thing thing) {
        String title = thing.getTitleToDisplay();
        if (!title.isEmpty()) {
            remoteViews.setViewVisibility(TV_TITLE, View.VISIBLE);
            remoteViews.setTextViewText(TV_TITLE, title);
            remoteViews.setViewPadding(TV_TITLE, dp12, dp12, dp12, 0);
            remoteViews.setViewVisibility(V_PADDING_BOTTOM, View.VISIBLE);
            setSeparatorVisibilities(remoteViews, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(TV_TITLE, View.GONE);
        }

        if (thing.isPrivate()) {
            remoteViews.setViewVisibility(R.id.view_private_helper_1, View.VISIBLE);
            remoteViews.setViewVisibility(IV_PRIVATE_THING,           View.VISIBLE);
            remoteViews.setViewVisibility(R.id.view_private_helper_2, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(R.id.view_private_helper_1, View.GONE);
            remoteViews.setViewVisibility(IV_PRIVATE_THING,           View.GONE);
            remoteViews.setViewVisibility(R.id.view_private_helper_2, View.GONE);
        }
    }

    private static void setContent(
            Context context, RemoteViews remoteViews, Thing thing, int appWidgetId, Class clazz) {
        String content = thing.getContent();
        if (content.isEmpty() || thing.isPrivate()) {
            remoteViews.setViewVisibility(LV_CHECKLIST,        View.GONE);
            remoteViews.setViewVisibility(LL_CHECK_LIST_ITEMS, View.GONE);
            remoteViews.setViewVisibility(TV_CONTENT,          View.GONE);
            return;
        }

        if (!CheckListHelper.isCheckListStr(content)) {
            remoteViews.setViewVisibility(LV_CHECKLIST,        View.GONE);
            remoteViews.setViewVisibility(LL_CHECK_LIST_ITEMS, View.GONE);
            remoteViews.setViewVisibility(TV_CONTENT,          View.VISIBLE);
            remoteViews.setViewPadding(TV_CONTENT, dp12, dp12, dp12, 0);
            remoteViews.setTextViewText(TV_CONTENT, content);
            int length = content.length();
            if (length <= 60) {
                remoteViews.setTextViewTextSize(TV_CONTENT, TypedValue.COMPLEX_UNIT_SP,
                        -0.14f * length + 24.14f);
            } else {
                remoteViews.setTextViewTextSize(TV_CONTENT, TypedValue.COMPLEX_UNIT_SP, 16);
            }
        } else {
            if (clazz.getSuperclass().equals(BaseThingWidget.class)) {
                setChecklistForSingleThing(context, remoteViews, thing, appWidgetId, clazz);
            } else {
                setChecklistForThingsListItem(context, remoteViews, content);
            }
        }

        remoteViews.setViewVisibility(V_PADDING_BOTTOM, View.VISIBLE);
        setSeparatorVisibilities(remoteViews, View.VISIBLE);
    }

    private static void setChecklistForSingleThing(
            Context context, RemoteViews remoteViews, Thing thing, int appWidgetId, Class clazz) {
        remoteViews.setViewVisibility(LV_CHECKLIST,        View.VISIBLE);
        remoteViews.setViewVisibility(LL_CHECK_LIST_ITEMS, View.GONE);
        remoteViews.setViewVisibility(TV_CONTENT,          View.GONE);

        remoteViews.setViewPadding(LV_CHECKLIST, dp12, dp12, dp12, 0);

        Intent intent = new Intent(context, ChecklistWidgetService.class);
        intent.putExtra(Def.Communication.KEY_WIDGET_ID, appWidgetId);
        intent.putExtra(Def.Communication.KEY_ID, thing.getId());
        // Very important! without this line, two different checklist items may have same content
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        remoteViews.setRemoteAdapter(LV_CHECKLIST, intent);

        /**
         * see {@link ChecklistWidgetService.ChecklistViewFactory#setupEvents(RemoteViews, int)}
         */
        intent = new Intent(context, clazz);
        intent.setAction(Def.Communication.BROADCAST_ACTION_UPDATE_CHECKLIST);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.lv_thing_check_list, pendingIntent);
    }

    private static void setChecklistForThingsListItem(
            Context context, RemoteViews remoteViews, String checklistStr) {
        remoteViews.setViewVisibility(LL_CHECK_LIST_ITEMS, View.VISIBLE);
        remoteViews.setViewVisibility(LV_CHECKLIST,        View.GONE);
        remoteViews.setViewVisibility(TV_CONTENT,          View.GONE);

        remoteViews.removeAllViews(LL_CHECK_LIST_ITEMS);

        remoteViews.setViewPadding(LL_CHECK_LIST_ITEMS, dp12, dp12, dp12, 0);

        List<String> items = CheckListHelper.toCheckListItems(checklistStr, false);
        items.remove("2");
        items.remove("3");
        items.remove("4");
        final int size = items.size();

        if (size > 8) {
            items = items.subList(0, 8);
        }

        for (String item : items) {
            RemoteViews rvItem = createRemoteViewsForChecklistItem(context, item, size);
            remoteViews.addView(LL_CHECK_LIST_ITEMS, rvItem);
        }

        if (size > 8) {
            RemoteViews rvItem = new RemoteViews(context.getPackageName(), R.layout.check_list_tv);
            rvItem.setViewVisibility(IV_STATE_CHECK_LIST, View.GONE);
            rvItem.setTextViewText(TV_CONTENT_CHECK_LIST, "...");
            rvItem.setTextViewTextSize(TV_CONTENT_CHECK_LIST, TypedValue.COMPLEX_UNIT_SP, 18);
            rvItem.setViewPadding(TV_CONTENT_CHECK_LIST, 0, (int) (-4 * screenDensity), 0, 0);
            remoteViews.addView(LL_CHECK_LIST_ITEMS, rvItem);
        }
    }

    private static void setAudioAttachment(Context context, RemoteViews remoteViews, Thing thing) {
        String attachment = thing.getAttachment();
        String str = AttachmentHelper.getAudioAttachmentCountStr(attachment, context);
        if (str == null || thing.isPrivate()) {
            remoteViews.setViewVisibility(LL_AUDIO_ATTACHMENT, View.GONE);
            return;
        }

        remoteViews.setViewVisibility(LL_AUDIO_ATTACHMENT, View.VISIBLE);
        remoteViews.setViewPadding(LL_AUDIO_ATTACHMENT, dp12, (int) (screenDensity * 9), dp12, 0);
        remoteViews.setTextViewText(TV_AUDIO_COUNT, str);

        remoteViews.setViewVisibility(V_PADDING_BOTTOM, View.VISIBLE);
        setSeparatorVisibilities(remoteViews, View.VISIBLE);
    }

    private static void setState(Context context, RemoteViews remoteViews, Thing thing) {
        @Thing.State int state = thing.getState();
        if (thing.isPrivate() || state == Thing.UNDERWAY) {
            remoteViews.setViewVisibility(RL_THING_STATE, View.GONE);
            return;
        }

        remoteViews.setViewVisibility(RL_THING_STATE, View.VISIBLE);
        remoteViews.setTextViewText(TV_THING_STATE, Thing.getStateStr(state, context));
        if (state == Thing.FINISHED) {
            remoteViews.setImageViewResource(IV_THING_STATE, R.drawable.ic_finished_widget);
            remoteViews.setViewPadding(IV_THING_STATE, 0, (int) (screenDensity * 2.5),
                    (int) (screenDensity * 12), 0);
        } else if (state == Thing.DELETED) {
            remoteViews.setImageViewResource(IV_THING_STATE, R.drawable.ic_deleted_widget);
            remoteViews.setViewPadding(IV_THING_STATE, 0, (int) (screenDensity * 1.5),
                    (int) (screenDensity * 12), 0);
        }
        remoteViews.setViewVisibility(V_PADDING_BOTTOM, View.VISIBLE);
    }

    private static void setAction(Context context, RemoteViews remoteViews, Thing thing, Class clazz) {
        @Thing.Type int type = thing.getType();
        if (thing.isPrivate() || thing.getState() != Thing.UNDERWAY
                || (type != Thing.REMINDER && type != Thing.GOAL && type != Thing.HABIT)
                || !clazz.getSuperclass().equals(BaseThingWidget.class)) {
            remoteViews.setViewVisibility(LL_THING_ACTION, View.GONE);
            return;
        }

        remoteViews.setViewVisibility(LL_THING_ACTION, View.VISIBLE);
        if (type == Thing.HABIT) {
            setActionForHabit(context, remoteViews, thing);
        } else {
            setActionForReminder(context, remoteViews, thing);
        }
        remoteViews.setViewVisibility(V_PADDING_BOTTOM, View.VISIBLE);
    }

    private static void setActionForReminder(Context context, RemoteViews remoteViews, Thing thing) {
        remoteViews.setTextViewText(TV_THING_ACTION, context.getString(R.string.act_finish));

        long id = thing.getId();
        int position = ThingManager.getInstance(context).getPosition(id);
        Intent intent = new Intent(context, ReminderNotificationActionReceiver.class);
        intent.setAction(Def.Communication.WIDGET_ACTION_FINISH);
        intent.putExtra(Def.Communication.KEY_ID, id);
        intent.putExtra(Def.Communication.KEY_POSITION, position);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                (int) id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(TV_THING_ACTION, pendingIntent);
    }

    private static void setActionForHabit(Context context, RemoteViews remoteViews, Thing thing) {
        remoteViews.setTextViewText(TV_THING_ACTION, context.getString(R.string.act_finish_this_time_habit));

        long id = thing.getId();
        int position = ThingManager.getInstance(context).getPosition(id);
        Intent intent = new Intent(context, HabitWidgetActionReceiver.class);
        intent.setAction(Def.Communication.WIDGET_ACTION_FINISH);
        intent.putExtra(Def.Communication.KEY_ID, id);
        intent.putExtra(Def.Communication.KEY_POSITION, position);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                (int) id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(TV_THING_ACTION, pendingIntent);
    }

    private static void setReminder(Context context, RemoteViews remoteViews, Thing thing) {
        int thingType = thing.getType();
        int thingState = thing.getState();
        Reminder reminder = ReminderDAO.getInstance(context).getReminderById(thing.getId());
        if (reminder == null || thing.isPrivate()) {
            remoteViews.setViewVisibility(RL_REMINDER, View.GONE);
            return;
        }

        int state = reminder.getState();
        long notifyTime = reminder.getNotifyTime();

        remoteViews.setViewVisibility(RL_REMINDER, View.VISIBLE);
        remoteViews.setViewPadding(RL_REMINDER, dp12, dp12, dp12, 0);

        if (thingType == Thing.REMINDER) {
            remoteViews.setViewPadding(IV_REMINDER, 0, (int) (screenDensity * 2), 0, 0);
            remoteViews.setImageViewResource(IV_REMINDER, R.drawable.card_reminder);
            remoteViews.setTextViewTextSize(TV_REMINDER_TIME, TypedValue.COMPLEX_UNIT_SP, 12);

            String timeStr = DateTimeUtil.getDateTimeStrAt(notifyTime, context, false);
            if (timeStr.startsWith("on ")) {
                timeStr = timeStr.substring(3, timeStr.length());
            }
            String textToSet = timeStr;
            if (thingState != Thing.UNDERWAY || state != Reminder.UNDERWAY) {
                textToSet += ", " + Reminder.getStateDescription(thingState, state, context);
            }
            remoteViews.setTextViewText(TV_REMINDER_TIME, textToSet);
        } else {
            remoteViews.setViewPadding(IV_REMINDER, 0, (int) (screenDensity * 1.6), 0, 0);
            remoteViews.setImageViewResource(IV_REMINDER, R.drawable.card_goal);
            remoteViews.setTextViewTextSize(TV_REMINDER_TIME, TypedValue.COMPLEX_UNIT_SP, 16);

            if (thingState == Reminder.UNDERWAY && state == Reminder.UNDERWAY) {
                remoteViews.setTextViewText(TV_REMINDER_TIME,
                        DateTimeUtil.getDateTimeStrGoal(notifyTime, context));
            } else {
                remoteViews.setTextViewText(TV_REMINDER_TIME,
                        Reminder.getStateDescription(thingState, state, context));
            }
        }

        remoteViews.setViewVisibility(V_PADDING_BOTTOM, View.VISIBLE);
    }

    private static void setHabit(Context context, RemoteViews remoteViews, Thing thing) {
        Habit habit = HabitDAO.getInstance(context).getHabitById(thing.getId());
        if (habit == null || thing.isPrivate())  {
            remoteViews.setViewVisibility(RL_HABIT, View.GONE);
            return;
        }

        remoteViews.setViewVisibility(RL_HABIT, View.VISIBLE);
        remoteViews.setViewPadding(RL_HABIT, dp12, dp12, dp12, 0);

        remoteViews.setTextViewText(TV_HABIT_SUMMARY, habit.getSummary(context));

        if (thing.getState() == Thing.UNDERWAY) {
            remoteViews.setViewVisibility(TV_HABIT_NEXT_REMINDER,   View.VISIBLE);
            remoteViews.setViewVisibility(V_HABIT_SEPARATOR_2,      View.VISIBLE);
            remoteViews.setViewVisibility(TV_HABIT_LAST_FIVE,       View.VISIBLE);
            remoteViews.setViewVisibility(LL_HABIT_RECORD,          View.VISIBLE);
            remoteViews.setViewVisibility(TV_HABIT_FINISHED_THIS_T, View.VISIBLE);

            String next = context.getString(R.string.habit_next_reminder);
            remoteViews.setTextViewText(TV_HABIT_NEXT_REMINDER,
                    next + " " + habit.getNextReminderDescription(context));

            String record = habit.getRecord();
            StringBuilder lastFive;
            int len = record.length();
            if (len >= 5) {
                lastFive = new StringBuilder(record.substring(len - 5, len));
            } else {
                lastFive = new StringBuilder(record);
                for (int i = 0; i < 5 - len; i++) {
                    lastFive.append("?");
                }
            }
            setHabitLastFive(remoteViews, lastFive.toString());

            remoteViews.setTextViewText(TV_HABIT_FINISHED_THIS_T,
                    habit.getFinishedTimesThisTStr(context));
        } else {
            remoteViews.setViewVisibility(TV_HABIT_NEXT_REMINDER,   View.GONE);
            remoteViews.setViewVisibility(V_HABIT_SEPARATOR_2,      View.GONE);
            remoteViews.setViewVisibility(TV_HABIT_LAST_FIVE,       View.GONE);
            remoteViews.setViewVisibility(LL_HABIT_RECORD,          View.GONE);
            remoteViews.setViewVisibility(TV_HABIT_FINISHED_THIS_T, View.GONE);
        }

        remoteViews.setViewVisibility(V_PADDING_BOTTOM, View.VISIBLE);
    }

    private static void setHabitLastFive(RemoteViews remoteViews, String lastFive) {
        int[] ids = {
                R.id.iv_thing_habit_record_1,
                R.id.iv_thing_habit_record_2,
                R.id.iv_thing_habit_record_3,
                R.id.iv_thing_habit_record_4,
                R.id.iv_thing_habit_record_5
        };
        char[] states = lastFive.toCharArray();
        for (int i = 0; i < states.length; i++) {
            if (states[i] == '0') {
                remoteViews.setImageViewResource(ids[i], R.drawable.card_habit_unfinished);
            } else if (states[i] == '1') {
                remoteViews.setImageViewResource(ids[i], R.drawable.card_habit_finished);
            } else {
                remoteViews.setImageViewResource(ids[i], R.drawable.card_habit_unknown);
            }
        }
    }

}