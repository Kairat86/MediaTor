package z.zer.tor.media.android.gui.activity.internal;

import android.app.Fragment;
import android.content.Intent;

import java.lang.ref.WeakReference;

import z.zer.tor.media.R;
import z.zer.tor.media.android.core.ConfigurationManager;
import z.zer.tor.media.android.gui.activity.MainActivity;
import z.zer.tor.media.android.gui.activity.SettingsActivity;
import z.zer.tor.media.android.gui.activity.WizardActivity;
import z.zer.tor.media.android.gui.fragments.MyFilesFragment;
import z.zer.tor.media.android.gui.fragments.TransfersFragment;
import z.zer.tor.media.android.gui.fragments.TransfersFragment.TransferStatus;
import z.zer.tor.media.util.Ref;


public final class MainController {

    private final WeakReference<MainActivity> activityRef;

    public MainController(MainActivity activity) {
        activityRef = Ref.weak(activity);
    }

    public MainActivity getActivity() {
        if (!Ref.alive(activityRef)) {
            return null;
        }
        return activityRef.get();
    }

    public void switchFragment(int itemId) {
        if (!Ref.alive(activityRef)) {
            return;
        }
        MainActivity activity = activityRef.get();
        Fragment fragment = activity.getFragmentByNavMenuId(itemId);
        if (fragment != null) {
            activity.switchContent(fragment);
        }
    }

    public void showPreferences() {
        if (!Ref.alive(activityRef)) {
            return;
        }
        MainActivity activity = activityRef.get();
        Intent i = new Intent(activity, SettingsActivity.class);
        activity.startActivity(i);
    }

    public void launchMyMusic() {
        if (!Ref.alive(activityRef)) {
            return;
        }
        MainActivity activity = activityRef.get();
        Intent i = new Intent(activity, com.andrew.apollo.ui.activities.HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(i);
    }

    public void showTransfers(TransferStatus status) {
        if (!Ref.alive(activityRef)) {
            return;
        }
        MainActivity activity = activityRef.get();
        if (!(activity.getCurrentFragment() instanceof TransfersFragment)) {
            activity.runOnUiThread(() -> {
                TransfersFragment fragment = (TransfersFragment) activity.getFragmentByNavMenuId(R.id.menu_main_transfers);
                fragment.selectStatusTab(status);
                switchFragment(R.id.menu_main_transfers);
            });
        }
    }

    public void showMyFiles() {
        if (!Ref.alive(activityRef)) {
            return;
        }
        MainActivity activity = activityRef.get();
        if (!(activity.getCurrentFragment() instanceof MyFilesFragment)) {
            activity.runOnUiThread(() -> {
                MyFilesFragment fragment = (MyFilesFragment) activity.getFragmentByNavMenuId(R.id.menu_main_library);
                switchFragment(R.id.menu_main_library);
            });
        }
    }

    public void startWizardActivity() {
        if (!Ref.alive(activityRef)) {
            return;
        }
        MainActivity activity = activityRef.get();
        ConfigurationManager.instance().resetToDefaults();
        Intent i = new Intent(activity, WizardActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(i);
    }

    public void showShutdownDialog() {
        if (!Ref.alive(activityRef)) {
            return;
        }
        MainActivity activity = activityRef.get();
        activity.showShutdownDialog();
    }

    public void syncNavigationMenu() {
        if (!Ref.alive(activityRef)) {
            return;
        }
        MainActivity activity = activityRef.get();
        activity.syncNavigationMenu();
    }

    public void setTitle(CharSequence title) {
        if (!Ref.alive(activityRef)) {
            return;
        }
        MainActivity activity = activityRef.get();
        activity.setTitle(title);
    }

    public Fragment getFragmentByNavMenuId(int itemId) {
        if (!Ref.alive(activityRef)) {
            return null;
        }
        MainActivity activity = activityRef.get();
        return activity.getFragmentByNavMenuId(itemId);
    }

    public void switchContent(Fragment fragment) {
        if (!Ref.alive(activityRef)) {
            return;
        }
        MainActivity activity = activityRef.get();
        activity.switchContent(fragment);
    }
}
