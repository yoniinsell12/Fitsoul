package com.fitsoul.app.core.auth;

import android.app.Activity;
import android.content.Intent;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GoogleSignInHelper {
    
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> signInLauncher;
    private OnSignInResultListener listener;
    
    public interface OnSignInResultListener {
        void onSuccess(String idToken);
        void onFailure(String error);
    }
    
    @Inject
    public GoogleSignInHelper() {
    }
    
    public void initialize(ComponentActivity activity, String webClientId) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();
                
        googleSignInClient = GoogleSignIn.getClient(activity, gso);
        
        signInLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleSignInResult(task);
                } else {
                    if (listener != null) {
                        listener.onFailure("Google Sign-In cancelled");
                    }
                }
            }
        );
    }
    
    public void signIn(OnSignInResultListener listener) {
        this.listener = listener;
        Intent signInIntent = googleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }
    
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            if (idToken != null && listener != null) {
                listener.onSuccess(idToken);
            } else if (listener != null) {
                listener.onFailure("Failed to get ID token");
            }
        } catch (ApiException e) {
            if (listener != null) {
                listener.onFailure("Google Sign-In failed: " + e.getMessage());
            }
        }
    }
    
    public void signOut() {
        if (googleSignInClient != null) {
            googleSignInClient.signOut();
        }
    }
}
