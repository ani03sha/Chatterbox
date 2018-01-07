package org.anirudh.redquark.chatterbox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.anirudh.redquark.chatterbox.application.ChatterboxApplication;
import org.anirudh.redquark.chatterbox.receiver.ConnectivityReceiver;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private static final int RC_SIGN_IN = 9001;

    /* For Google*/
    private GoogleApiClient googleApiClient;

    /* For facebook */
    private CallbackManager callbackManager;

    /* Firebase Auth instance*/
    private FirebaseAuth auth;

    /* Layout elements */
    private EditText inputEmail;
    private EditText inputPassword;

    /* Progress Bar */
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* Creating button for Phone Authentication*/
        Button phoneAuth = findViewById(R.id.phone_auth);

        /* Initialize Firebase Auth */
        auth = FirebaseAuth.getInstance();

        /* Getting the type of user from the SharedPreferences */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        /* Google Login*/
        SignInButton googleSignIn = findViewById(R.id.googleLogin);

        /* Button for resetting the password */
        Button btnReset = findViewById(R.id.btn_reset_password);

        /* Button for signing up user*/
        Button btnSignup = findViewById(R.id.btn_signup);

        /* Google Login */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(LoginActivity.this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkConnection()) {
                    Intent i = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                    startActivityForResult(i, RC_SIGN_IN);
                }
            }
        });

        /* Facebook Login */
        callbackManager = CallbackManager.Factory.create();
        LoginButton facebookLogin = findViewById(R.id.facebook_sign_in_button);
        facebookLogin.setReadPermissions("email", "public_profile");
        facebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess() " + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel()");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook error: " + error);
            }
        });

        /*Click even for Phone Auth */
        phoneAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, PhoneAuthActivity.class));
            }
        });

        /* Click on Sign up button */
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkConnection()) {
                    startActivity(new Intent(LoginActivity.this, SignupActivity.class));
                }
            }
        });

        /* Forgot Password action */
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkConnection()) {
                    startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
                }
            }
        });

        inputEmail = findViewById(R.id.your_email_address);
        inputPassword = findViewById(R.id.create_new_password);
        progressBar = findViewById(R.id.progressBar);
        Button btnLogin = findViewById(R.id.btn_login);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkConnection()) {
                    startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkConnection()) {
                    String email = inputEmail.getText().toString();
                    final String password = inputPassword.getText().toString();

                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (TextUtils.isEmpty(password)) {
                        Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    progressBar.setVisibility(View.VISIBLE);

                    //authenticate user
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    progressBar.setVisibility(View.GONE);
                                    if (!task.isSuccessful()) {
                                        // there was an error
                                        if (password.length() < 6) {
                                            inputPassword.setError(getString(R.string.minimum_password));
                                        } else {
                                            Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Intent intent = new Intent(LoginActivity.this, SettingsActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
        }else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {

            String name = currentUser.getDisplayName();
            Toast.makeText(LoginActivity.this, name + " is logged in ", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
            intent.putExtra("userName", currentUser.getDisplayName());
            intent.putExtra("phoneNumber", currentUser.getPhoneNumber());
            startActivity(intent);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("LoginActivity", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        Toast.makeText(LoginActivity.this, "" + credential.getProvider(), Toast.LENGTH_LONG).show();

        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d("LoginActivity", "signInWithCredential:onComplete:" + task.isSuccessful());

                        Toast.makeText(LoginActivity.this, auth.getCurrentUser().getDisplayName() + " is logged in",
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(LoginActivity.this, ChatActivity.class));
                    }
                });
    }

    public String getData() {
        String name = null;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            name = user.getDisplayName();
        }
        return name;
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private boolean checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
        return isConnected;
    }

    private void showSnack(boolean isConnected) {
        String message;
        if (!isConnected) {
            message = "Sorry! Not connected to internet. Please check your internet connection";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        ChatterboxApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }
}