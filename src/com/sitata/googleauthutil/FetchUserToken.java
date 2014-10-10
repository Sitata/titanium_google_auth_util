package com.sitata.googleauthutil;

import java.io.IOException;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;

public class FetchUserToken extends AsyncTask{
    Activity mActivity;
    String mScope;
    String mEmail;
	UserTokenHandler mParent;
	private static final String TAG = "TiGoogleAuthUtilModule";

	public interface UserTokenHandler {
		public void handleToken(String email, String token);

		public void handleTokenError(String errorCode);

		public void handleRecoverableException(Intent recoveryIntent);

		public void handleGooglePlayException(
				GooglePlayServicesAvailabilityException playEx);
	}

	FetchUserToken(Activity activity, String name, String scope,
			UserTokenHandler parent) {
        this.mActivity = activity;
        this.mScope = scope;
        this.mEmail = name;
		this.mParent = parent;
    }

    /**
     * Executes the asynchronous job. This runs when you call execute()
     * on the AsyncTask instance.
     */
    @Override
	protected Object doInBackground(Object... arg0) {
        try {
			Logger.getLogger(TAG).info("Fetching token from google.");
            String token = fetchToken();
            if (token != null) {
				Logger.getLogger(TAG).info(
						"Found token for email: " + mEmail + " - " + token);
				mParent.handleToken(mEmail, token);
            }
        } catch (IOException e) {
            // The fetchToken() method handles Google-specific exceptions,
            // so this indicates something went wrong at a higher level.
            // TIP: Check for network connectivity before starting the AsyncTask.
			Logger.getLogger(TAG).info("IOException: " + e.getMessage());
			mParent.handleTokenError(TitaniumGoogleAuthUtilModule.IO_EXCEPTION);
        }
		return null;
    }

    /**
     * Gets an authentication token from Google and handles any
     * GoogleAuthException that may occur.
     */
    protected String fetchToken() throws IOException {
        try {
            return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
		} catch (GooglePlayServicesAvailabilityException playEx) {
			Logger.getLogger(TAG).info(
					"Google Play Exception: " + playEx.getMessage());
			mParent.handleGooglePlayException(playEx);
        } catch (UserRecoverableAuthException userRecoverableException) {
			Logger.getLogger(TAG).info(
					"User Recoverable Exception: "
							+ userRecoverableException.getMessage());
			mParent.handleRecoverableException(userRecoverableException
					.getIntent());
        } catch (GoogleAuthException fatalException) {
			Logger.getLogger(TAG).info(
					"Fatal Exception: " + fatalException.getMessage());
			mParent.handleTokenError(TitaniumGoogleAuthUtilModule.FATAL_EXCEPTION);
        }
        return null;
    }



}