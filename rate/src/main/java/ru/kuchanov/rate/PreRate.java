package ru.kuchanov.rate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.ref.WeakReference;

import io.techery.properratingbar.ProperRatingBar;

public final class PreRate {

    private static String appName;

    private WeakReference<Context> cntxRef;

    private static PreRate instance;

    private MaterialDialog lastDialog;

    private String emailAddress;

    private String emailSubject;

    private String firstDialogText;

    private int titleColor;

    @SuppressLint("unused")
    private int lineColor;

    private PreRate() {
        super();
    }

    public static PreRate init(final Activity act, final String feedbackEmailTo, final String feedbackSubj) {
        if (instance == null) {
            instance = new PreRate();
            instance.titleColor = ContextCompat.getColor(act, R.color.pre_rate_main_color);
            instance.lineColor = instance.titleColor;
            TimeSettings.setFirstStartTime(act);
        }
        instance.cntxRef = new WeakReference<>(act);

        instance.emailAddress = feedbackEmailTo;
        instance.emailSubject = feedbackSubj;
        instance.firstDialogText = act.getResources().getString(R.string.main_dialog_text);
        return instance;
    }

    @SuppressLint("unused")
    public PreRate configureColors(final int titleColor, final int lineColor) {
        this.titleColor = titleColor;
        this.lineColor = lineColor;
        return this;
    }

    @SuppressLint("unused")
    public PreRate configureText(final String firstDialogText) {
        this.firstDialogText = firstDialogText;
        return this;
    }

    public void showIfNeed() {
        if (TimeSettings.needShowPreRateDialog(cntxRef.get()) &&
            (lastDialog == null || !lastDialog.isShowing()) &&
            isConnected(cntxRef.get())) {
            showRateDialog();
        }
    }

    /***
     * call in onDestroy
     */
    public static void clearDialogIfOpen() {
        if (instance != null && instance.lastDialog != null &&
            instance.lastDialog.isShowing()) {
            instance.lastDialog.dismiss();
        }
    }

    public void showRateDialog() {
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(cntxRef.get())
                .cancelable(false)
                .content(firstDialogText)
                .title(cntxRef.get().getString(R.string.rate_app_title, getApplicationName(cntxRef.get())))
                .positiveText(R.string.yes)
                .onPositive((dialog, which) -> showPreStarsDialog())
                .neutralText(R.string.not_now)
                .onNeutral((dialog, which) -> lastDialog.dismiss())
                .negativeText(R.string.not_notify)
                .onNegative((dialog, which) -> {
                    TimeSettings.setShowMode(cntxRef.get(), TimeSettings.NOT_SHOW);
                    lastDialog.dismiss();
                });

        lastDialog = builder.build();
        lastDialog.show();
        TimeSettings.setShowMode(cntxRef.get(), TimeSettings.SHOW_LATER);
        TimeSettings.saveLastShowTime(cntxRef.get());
    }

    private void showPreStarsDialog() {
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(cntxRef.get())
                .cancelable(false)
                .title(getApplicationName(cntxRef.get()));

        final LayoutInflater inflater = LayoutInflater.from(cntxRef.get().getApplicationContext());
        @SuppressLint("InflateParams") final View customView = inflater.inflate(R.layout.pre_rate_stars_dialog, null, false);

        final ProperRatingBar ratingBar = customView.findViewById(R.id.ratingBar);

        ratingBar.setListener(properRatingBar -> lastDialog.getActionButton(DialogAction.POSITIVE).setEnabled(true));

        builder
                .customView(customView, false)
                .positiveText(R.string.yes)
                .onPositive((dialog, which) -> {
                    if (ratingBar.getRating() == 5) {
                        TimeSettings.setShowMode(cntxRef.get(), TimeSettings.NOT_SHOW);
                        String appPackageName = cntxRef.get().getPackageName();
                        try {
                            cntxRef.get().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(cntxRef.get().getString(R.string.market_os_url, appPackageName))));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            cntxRef.get().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(cntxRef.get().getString(R.string.market_web_url, appPackageName))));
                        }
                    } else {
                        showFeedbackDialog();
                    }
                })
                .negativeText(android.R.string.cancel)
                .onNegative((dialog, which) -> lastDialog.dismiss());
        lastDialog = builder.build();

        lastDialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);

        lastDialog.show();
    }

    private void showFeedbackDialog() {
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(cntxRef.get())
                .cancelable(false)
                .title(R.string.help_us);

        final LayoutInflater inflater = LayoutInflater.from(cntxRef.get().getApplicationContext());
        @SuppressLint("InflateParams") final View customView = inflater.inflate(R.layout.pre_rate_feedback_dialog, null, false);

        final EditText etEmailText = customView.findViewById(R.id.etMessage);

        builder.customView(customView, false)
                .positiveText(R.string.yes)
                .onPositive((dialog, which) -> {
                    String text = etEmailText.getText().toString();
                    if (!TextUtils.isEmpty(text)) {
                        Intent intentEmail = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + emailAddress));
                        intentEmail.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                        intentEmail.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
                        intentEmail.putExtra(Intent.EXTRA_TEXT, text);
                        cntxRef.get().startActivity(Intent.createChooser(intentEmail, cntxRef.get().getString(R.string.choose_email_provider)));
                    }
                })
                .negativeText(android.R.string.cancel)
                .onNegative((dialog, which) -> lastDialog.dismiss());

        lastDialog = builder.build();
        lastDialog.show();
        TimeSettings.setShowMode(cntxRef.get(), TimeSettings.NOT_SHOW);
    }

    private static String getApplicationName(final Context context) {
        if (appName == null) {
            final int stringId = context.getApplicationInfo().labelRes;
            appName = context.getString(stringId);
        }
        return appName;
    }

    private static boolean isConnected(final Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}