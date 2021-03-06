package com.mobica.paymentsmethod.paypal;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.PayPalRequest;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.mobica.paymentsmethod.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import java.net.HttpURLConnection;

import cz.msebera.android.httpclient.Header;

/**
 * Handling general PayPal operations
 */
public class PayPalHandler {
	private static final String TAG = "PayPal";
	private static final String TEST_SERVER_URL = "https://braintreetestapplication-php.herokuapp.com/";
	private static final String TEST_CHECKOUT_URL = TEST_SERVER_URL + "checkout.php";
	private static final String TEST_TOKEN_URL = TEST_SERVER_URL + "client_token";
	private static final String TEST_TOKEN = "eyJ2ZXJzaW9uIjoyLCJhdXRob3JpemF0aW9uRmluZ2VycHJpbnQiOiJlMWIyZGQ0NDA5ZDUxM2RhMGExNGFlMGE4ZTE3ZmE4MzYyOWRjOTZmNzIyNTc2ZWQzNjlmYmRkNTkwODgwZjJifGNyZWF0ZWRfYXQ9MjAxOC0wNS0xNVQwNjozMDoyMy4zMzM5MDU2NDMrMDAwMFx1MDAyNm1lcmNoYW50X2lkPTM0OHBrOWNnZjNiZ3l3MmJcdTAwMjZwdWJsaWNfa2V5PTJuMjQ3ZHY4OWJxOXZtcHIiLCJjb25maWdVcmwiOiJodHRwczovL2FwaS5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tOjQ0My9tZXJjaGFudHMvMzQ4cGs5Y2dmM2JneXcyYi9jbGllbnRfYXBpL3YxL2NvbmZpZ3VyYXRpb24iLCJjaGFsbGVuZ2VzIjpbXSwiZW52aXJvbm1lbnQiOiJzYW5kYm94IiwiY2xpZW50QXBpVXJsIjoiaHR0cHM6Ly9hcGkuc2FuZGJveC5icmFpbnRyZWVnYXRld2F5LmNvbTo0NDMvbWVyY2hhbnRzLzM0OHBrOWNnZjNiZ3l3MmIvY2xpZW50X2FwaSIsImFzc2V0c1VybCI6Imh0dHBzOi8vYXNzZXRzLmJyYWludHJlZWdhdGV3YXkuY29tIiwiYXV0aFVybCI6Imh0dHBzOi8vYXV0aC52ZW5tby5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tIiwiYW5hbHl0aWNzIjp7InVybCI6Imh0dHBzOi8vY2xpZW50LWFuYWx5dGljcy5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tLzM0OHBrOWNnZjNiZ3l3MmIifSwidGhyZWVEU2VjdXJlRW5hYmxlZCI6dHJ1ZSwicGF5cGFsRW5hYmxlZCI6dHJ1ZSwicGF5cGFsIjp7ImRpc3BsYXlOYW1lIjoiQWNtZSBXaWRnZXRzLCBMdGQuIChTYW5kYm94KSIsImNsaWVudElkIjpudWxsLCJwcml2YWN5VXJsIjoiaHR0cDovL2V4YW1wbGUuY29tL3BwIiwidXNlckFncmVlbWVudFVybCI6Imh0dHA6Ly9leGFtcGxlLmNvbS90b3MiLCJiYXNlVXJsIjoiaHR0cHM6Ly9hc3NldHMuYnJhaW50cmVlZ2F0ZXdheS5jb20iLCJhc3NldHNVcmwiOiJodHRwczovL2NoZWNrb3V0LnBheXBhbC5jb20iLCJkaXJlY3RCYXNlVXJsIjpudWxsLCJhbGxvd0h0dHAiOnRydWUsImVudmlyb25tZW50Tm9OZXR3b3JrIjp0cnVlLCJlbnZpcm9ubWVudCI6Im9mZmxpbmUiLCJ1bnZldHRlZE1lcmNoYW50IjpmYWxzZSwiYnJhaW50cmVlQ2xpZW50SWQiOiJtYXN0ZXJjbGllbnQzIiwiYmlsbGluZ0FncmVlbWVudHNFbmFibGVkIjp0cnVlLCJtZXJjaGFudEFjY291bnRJZCI6ImFjbWV3aWRnZXRzbHRkc2FuZGJveCIsImN1cnJlbmN5SXNvQ29kZSI6IlVTRCJ9LCJtZXJjaGFudElkIjoiMzQ4cGs5Y2dmM2JneXcyYiIsInZlbm1vIjoib2ZmIn0=";

	private String clientToken;

	/**
	 * Update client token
	 */
	public void updateClientToken() {
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(TEST_TOKEN_URL, new TextHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, String token) {
				Log.d(TAG, "Response after get token from server: " + token);
				clientToken = token;
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				Log.d(TAG, "Failure after get token from server: " + responseString);

			}
		});
	}

	/**
	 * Start PayPalActvity with drop UI menu
	 * @param fragment
	 * @param requestCode
	 */
	public void startPayPalActivity(Fragment fragment, int requestCode) {
		//TODO: replace test token with one returned from server
		clientToken = TEST_TOKEN;
		DropInRequest dropInRequest = new DropInRequest().clientToken(clientToken);
		fragment.startActivityForResult(dropInRequest.getIntent(fragment.getContext()), requestCode);
	}

	/**
	 * Start PayPal activity
	 * @param context
	 */
	public void startBillingAgreement(final Activity context) {
		BraintreeFragment braintreeFragment = getBraintreeFragment(context);
		if (braintreeFragment == null){
			Log.d(TAG, "Problem with initialize fragment");
			return;
		}
		PayPalRequest request = new PayPalRequest()
				.localeCode("US")
				.billingAgreementDescription("Your agreement description");
		PayPal.requestBillingAgreement(braintreeFragment, request);
	}

	/**
	 * Handle data from PayPal activity or webview
	 * @param context
	 * @param data
	 */
	public void handlePaypalData(Context context, Intent data) {
		if (data == null) {
			Log.d(TAG, "No data returned");
			return;
		}
		DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
		if (result == null) {
			Log.d(TAG, "No drop in data returned");
			return;
		}
		PaymentMethodNonce paymentMethodNonce = result.getPaymentMethodNonce();
		if (paymentMethodNonce == null) {
			Log.d(TAG, "No payment method returned");
			return;
		}
		String nonce = result.getPaymentMethodNonce().getNonce();
		Log.d(TAG, "Result from Paypal activity: " + nonce);
		postNonceToServer(context, nonce);
	}

	private BraintreeFragment getBraintreeFragment(final Activity context) {
		BraintreeFragment braintreeFragment = null;
		try {
			braintreeFragment = BraintreeFragment.newInstance(context, TEST_TOKEN);
			braintreeFragment.addListener(new PaymentMethodNonceCreatedListener() {
				@Override
				public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
					String nonce = paymentMethodNonce.getNonce();
					Log.d(TAG, "Nonce from server: " + nonce);
					postNonceToServer(context, nonce);
				}
			});
			braintreeFragment.addListener(new BraintreeErrorListener() {
				@Override
				public void onError(Exception error) {
					Log.d(TAG, "Error from server: " + error.getMessage());
				}
			});
			braintreeFragment.addListener(new BraintreeCancelListener() {
				@Override
				public void onCancel(int requestCode) {
					Log.d(TAG, "Braintree operation canceled: " + requestCode);
				}
			});
		} catch (InvalidArgumentException e) {
			Log.e(TAG, e.getLocalizedMessage());
		}

		return braintreeFragment;
	}

	private void postNonceToServer(final Context context, String nonce) {
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		//TODO: replace test nonce with one returned from server
		params.put("payment_method_nonce", "fake-valid-nonce");
		params.put("amount", "55");
		client.post(TEST_CHECKOUT_URL, params,
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
						Log.d(TAG, "Response after post nonce to server.statusCode: " + statusCode);
						handleResponseCode(context, statusCode);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
						Log.d(TAG, "Failure response after post nonce to server. statusCode: " + statusCode);
						Log.d(TAG, "Failure response after post nonce to server. Error: " + error.getMessage());
						handleResponseCode(context, statusCode);
					}

					private void handleResponseCode(Context context, int statusCode) {
						//TODO: remove condition for HttpURLConnection.HTTP_SEE_OTHER when server
						// will return status value (not prepare a redirect after successful transaction)
						if (statusCode == HttpURLConnection.HTTP_OK ||
								statusCode == HttpURLConnection.HTTP_ACCEPTED ||
								statusCode == HttpURLConnection.HTTP_MOVED_TEMP) {
							Toast.makeText(context, R.string.transaction_completed, Toast.LENGTH_LONG).show();
						}
					}
				}
		);
	}
}
