package z.zer.tor.media.android.gui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import z.zer.tor.media.R;
import z.zer.tor.media.android.core.Constants;
import z.zer.tor.media.android.gui.util.UIUtils;
import z.zer.tor.media.android.gui.views.AbstractActivity;

public final class VPNStatusDetailActivity extends AbstractActivity {

    public VPNStatusDetailActivity() {
        super(R.layout.view_vpn_status_detail);
    }

    @Override
    protected void initComponents(Bundle savedInstanceState) {
        final ImageView headerIcon = findView(R.id.view_vpn_status_header_icon);
        final TextView headerStatus = findView(R.id.view_vpn_status_header);
        final TextView vpnText = findView(R.id.view_vpn_status_vpn_text);


        final TextView vpnBullet = findView(R.id.view_vpn_status_bullet_textview);
        vpnBullet.setText(fromHtml(R.string.you_dont_need_a_vpn_to_use_frostwire_bullet_html));


        final TextView learnVPNText = findView(R.id.view_vpn_status_learn_more_textview);

        /*
        issue-709
            final Button piaVPNButton = findView(R.id.view_vpn_status_pia);
         */
        final Button expressVPNButton = findView(R.id.view_vpn_status_expressvpn);
        final Button nordVPNButton = findView(R.id.view_vpn_status_nordvpn);

        boolean isProtectedConnection = isProtectedConnection();
        // By default the layout has icon and title set to unprotected.
        if (isProtectedConnection) {
            // Current Status Icon
            headerIcon.setImageResource(R.drawable.vpn_icon_on_info);
            // Current Status Title, color is set in layout
            headerStatus.setText(R.string.protected_connection);
            // Current Status Text
            vpnText.setText(fromHtml(R.string.protected_connections_visibility_bullet_html));
            // learnVPNText
            learnVPNText.setText(R.string.vpn_find_out_more);
        } else {
            // Current Status Icon
            headerIcon.setImageResource(R.drawable.vpn_icon_off_info);
            // Current Status Title
            headerStatus.setText(R.string.unprotected_connection);
            headerStatus.setTextColor(Color.RED);
            // Current Status Text
            vpnText.setText(fromHtml(R.string.unprotected_connections_visibility_bullet_html));
            // learnVPNText
            learnVPNText.setText(R.string.vpn_find_out_more);
        }

        learnVPNText.setPaintFlags(learnVPNText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        /*
        issue-709
            piaVPNButton.setOnClickListener(v -> UIUtils.openURL(v.getContext(), Constants.PIA_VPN_URL));
        */

        expressVPNButton.setOnClickListener(v -> UIUtils.openURL(v.getContext(), Constants.EXPRESSVPN_URL));

        nordVPNButton.setOnClickListener(v -> UIUtils.openURL(v.getContext(), Constants.NORDVPN_URL));

        headerIcon.setOnClickListener(new LearnVPNLink());
        headerStatus.setOnClickListener(new LearnVPNLink());
        learnVPNText.setOnClickListener(new LearnVPNLink());
    }

    private final class LearnVPNLink implements View.OnClickListener {
        public void onClick(View v) {
            UIUtils.openURL(v.getContext(), Constants.FROSTWIRE_VPN_URL);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Intent newIntent = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // if we came from Transfers, make sure to go have the transfer fragment shown by MainActivity.
        Intent i = getIntent();
        if (i != null && i.getExtras() != null &&
                i.getExtras().getString("from", "").equals("transfers")) {
            newIntent.setAction(Constants.ACTION_SHOW_TRANSFERS);
        }

        startActivity(newIntent);
        try {
            super.onBackPressed();
        } catch (Throwable ignored) {
        }
    }

    private boolean isProtectedConnection() {
        Intent intent = getIntent();
        String action = intent != null ? intent.getAction() : null;
        return action != null && action.equals(Constants.ACTION_SHOW_VPN_STATUS_PROTECTED);
    }

    private Spanned fromHtml(int resId) {
        return fromHtml(getString(resId));
    }

    // once we get to API 24, we can replace the method for the new one
    @SuppressWarnings("deprecated")
    private static Spanned fromHtml(String html) {
        //noinspection deprecation
        return Html.fromHtml(html);
    }
}
