/*
 * This source code is part of the eXtended project for Android.
 *
 * You are free to use, modify, and distribute this code.
 * However, please act responsibly: give proper credit to the original author.
 *
 * Copyright © 2025 @whitesiemens
 *
*/

package com.tgx.extended;

import android.content.Context;
import android.view.View;

import org.thunderdog.challegram.R;
import org.thunderdog.challegram.component.base.SettingView;
import org.thunderdog.challegram.core.Lang;
import org.thunderdog.challegram.navigation.SettingsWrapBuilder;
import org.thunderdog.challegram.telegram.Tdlib;
import org.thunderdog.challegram.telegram.TdlibUi;
import org.thunderdog.challegram.ui.ListItem;
import org.thunderdog.challegram.ui.RecyclerViewController;
import org.thunderdog.challegram.ui.SettingsAdapter;
import org.thunderdog.challegram.unsorted.Settings;
import org.thunderdog.challegram.v.CustomRecyclerView;

import java.util.ArrayList;
import java.util.Map;

import com.tgx.extended.ExtendedConfig;
import com.tgx.extended.utils.SystemUtils;
import com.tgx.extended.utils.URLUtils;
import com.tgx.extended.ExtendedConfig.Setting;

import static com.tgx.extended.ExtendedConfig.Setting.*;

public class ExtendedSettingsController extends RecyclerViewController<ExtendedSettingsController.Args> implements View.OnClickListener {

  public ExtendedSettingsController(Context ctx, Tdlib tdlib) {
    super(ctx, tdlib);
  }

  @Override
  public int getId() {
    return R.id.controller_extendedSettings;
  }

  private SettingsAdapter adapter;
  private int mode;

  public static final int MODE_GENERAL = 1, MODE_APPEARANCE = 2, MODE_CHATS = 3, MODE_MISC = 4;

  public static class Args {
    public final int mode;
    public Args(int m) { mode = m; }
  }

  @Override
  public void setArguments(Args args) {
    super.setArguments(args);
    mode = args.mode;
  }

  @Override
  public CharSequence getName() {
  	if (mode == MODE_GENERAL) return Lang.getString(R.string.GeneralSettings);
  	else if (mode == MODE_APPEARANCE) return Lang.getString(R.string.AppearanceSettings);
  	else if (mode == MODE_CHATS) return Lang.getString(R.string.ChatsSettings);
  	else if (mode == MODE_MISC) return Lang.getString(R.string.MiscSettings);
  	else return Lang.getString(R.string.ExtendedSettings);
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.btn_generalSettings || id == R.id.btn_appearanceSettings || id == R.id.btn_chatsSettings || id == R.id.btn_miscSettings) {
      if (id == R.id.btn_generalSettings) mode = MODE_GENERAL;
      else if (id == R.id.btn_appearanceSettings) mode = MODE_APPEARANCE;
      else if (id == R.id.btn_chatsSettings) mode = MODE_CHATS;
      else if (id == R.id.btn_miscSettings) mode = MODE_MISC;
      ExtendedSettingsController c = new ExtendedSettingsController(context(), tdlib);
      c.setArguments(new Args(mode));
      navigateTo(c);
    } else if (id == R.id.btn_extendedChannel) tdlib.ui().openUrl(this, Lang.getString(R.string.ExtendedChannelLink), new TdlibUi.UrlOpenParameters().forceInstantView());
    else if (id == R.id.btn_extendedChat) tdlib.ui().openUrl(this, Lang.getString(R.string.ExtendedChatLink), new TdlibUi.UrlOpenParameters().forceInstantView());
    else if (id == R.id.btn_extendedSources) tdlib.ui().openUrl(this, Lang.getString(R.string.ExtendedSourcesLink), new TdlibUi.UrlOpenParameters());
    else if (id == R.id.btn_extendedTranslate) tdlib.ui().openUrl(this, Lang.getString(R.string.ExtendedTranslateLink), new TdlibUi.UrlOpenParameters());
    else {
      toggleSettingByViewId(id); 
      adapter.updateValuedSettingById(id);
    }
  }

  private static final Map<Integer, Setting> toggleSettingsMapping = Map.of(
    R.id.btn_showUserId, SHOW_IDS
  );

  private void toggleSettingByViewId(int id) {
    Setting s = toggleSettingsMapping.get(id);
    if (s != null) ExtendedConfig.instance().toggleSetting(s);
  }

  private void setData(SettingView view, int data) {
  	view.setData(data);
  }

  private void setToggle(SettingView view, ExtendedConfig.Setting s, boolean isUpdate) {
    view.getToggler().setRadioEnabled(s.value, isUpdate);
  }

  @Override
  protected void onCreateView(Context ctx, CustomRecyclerView recyclerView) {
    adapter = new SettingsAdapter(this) {
      @Override
      protected void setValuedSetting(ListItem item, SettingView view, boolean isUpdate) {
        int id = item.getId();
        if (id == R.id.btn_extendedChannel) setData(view, R.string.ExtendedChannelDesc);
        else if (id == R.id.btn_extendedChat) setData(view, R.string.ExtendedChatDesc);
        else if (id == R.id.btn_extendedSources) setData(view, R.string.ExtendedSourcesDesc);
        else if (id == R.id.btn_extendedTranslate) setData(view, R.string.ExtendedTranslateDesc);
      }
    };

    ArrayList<ListItem> items = new ArrayList<>();
    items.add(new ListItem(ListItem.TYPE_EMPTY_OFFSET_SMALL));

    if (mode == MODE_GENERAL) {
      items.add(new ListItem(ListItem.TYPE_HEADER, 0, 0, R.string.ProfilePreferences));
      items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));
      items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_showUserId, 0, R.string.ShowUserId));
      items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));
    } else if (mode == MODE_APPEARANCE) {
      // TODO: Appearance settings
    } else if (mode == MODE_CHATS) {
      // TODO: Chats settings
    } else if (mode == MODE_MISC) {
      // TODO: Misc settings
    } else {
      items.add(new ListItem(ListItem.TYPE_HEADER, 0, 0, R.string.Settings));
      items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));
      items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_generalSettings, R.drawable.baseline_widgets_24, R.string.GeneralSettings));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR));
      items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_appearanceSettings, R.drawable.baseline_palette_24, R.string.AppearanceSettings));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR));
      items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_chatsSettings, R.drawable.baseline_chat_bubble_24, R.string.ChatsSettings));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR));
      items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_miscSettings, R.drawable.baseline_star_24, R.string.MiscSettings));
      items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));

      items.add(new ListItem(ListItem.TYPE_HEADER, 0, 0, R.string.AboutExtended));
      items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));
      items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_extendedChannel, R.drawable.baseline_newspaper_24, R.string.ExtendedChannel));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR));
      items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_extendedChat, R.drawable.baseline_forum_24, R.string.ExtendedChat));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR));
      items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_extendedSources, R.drawable.baseline_github_24, R.string.ExtendedSources));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR));
      items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_extendedTranslate, R.drawable.baseline_translate_24, R.string.ExtendedTranslate));
      items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));
    }
    adapter.setItems(items, true);
    recyclerView.setAdapter(adapter);
  }
}