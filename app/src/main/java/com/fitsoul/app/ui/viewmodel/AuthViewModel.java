package com.fitsoul.app.ui.viewmodel;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;
import com.fitsoul.app.data.repository.AuthRepository;
import com.fitsoul.app.domain.model.User;
import com.google.firebase.auth.FirebaseAuth;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

@HiltViewModel
public class AuthViewModel extends ViewModel {
    
    private final AuthRepository authRepository;
    private final FirebaseAuth firebaseAuth;
    private final Handler mainHandler;
    
    private final MutableLiveData<AuthUiState> _uiState = new MutableLiveData<>(new AuthUiState());
    public final LiveData<AuthUiState> uiState = _uiState;
    
    private final MutableLiveData<AuthState> _authState = new MutableLiveData<>(AuthState.loading());
    public final LiveData<AuthState> authState = _authState;
    
    private final MutableLiveData<Map<String, String>> _validationErrors = 
        new MutableLiveData<>(new HashMap<>());
    public final LiveData<Map<String, String>> validationErrors = _validationErrors;
    
    private FirebaseAuth.AuthStateListener authStateListener;
    
    @Inject
    public AuthViewModel(AuthRepository authRepository, FirebaseAuth firebaseAuth) {
        this.authRepository = authRepository;
        this.firebaseAuth = firebaseAuth;
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        setupAuthStateListener();
        checkAuthStatus();
    }
    
    private void setupAuthStateListener() {
        authStateListener = firebaseAuth -> {
            mainHandler.post(() -> {
                if (firebaseAuth.getCurrentUser() != null) {
                    authRepository.getCurrentUser().thenAccept(result -> {
                        mainHandler.post(() -> {
                            if (result.isSuccess()) {
                                User user = result.getOrNull();
                                if (user != null) {
                                    _authState.setValue(AuthState.authenticated(user));
                                } else {
                                    _authState.setValue(AuthState.unauthenticated());
                                }
                            } else {
                                _authState.setValue(AuthState.unauthenticated());
                                AuthUiState currentState = _uiState.getValue();
                                if (currentState != null) {
                                    Exception ex = result.exceptionOrNull();
                                    String errorMsg = ex != null ? ex.getMessage() : "Failed to load user profile";
                                    _uiState.setValue(currentState.withError(errorMsg));
                                }
                            }
                        });
                    });
                } else {
                    _authState.setValue(AuthState.unauthenticated());
                    clearValidationErrors();
                }
            });
        };
        firebaseAuth.addAuthStateListener(authStateListener);
    }
    
    private void checkAuthStatus() {
        _authState.setValue(AuthState.loading());
        if (authRepository.isSignedIn()) {
            authRepository.getCurrentUser().thenAccept(result -> {
                mainHandler.post(() -> {
                    if (result.isSuccess()) {
                        User user = result.getOrNull();
                        if (user != null) {
                            _authState.setValue(AuthState.authenticated(user));
                        } else {
                            _authState.setValue(AuthState.unauthenticated());
                        }
                    } else {
                        _authState.setValue(AuthState.unauthenticated());
                    }
                });
            });
        } else {
            _authState.setValue(AuthState.unauthenticated());
        }
    }
    
    public void signInWithEmail(String email, String password) {
        if (!validateEmailPassword(email, password)) {
            return;
        }
        
        clearValidationErrors();
        AuthUiState currentState = _uiState.getValue();
        if (currentState != null) {
            _uiState.setValue(currentState.withLoading(true).withError(null));
        }
        
        authRepository.signInWithEmail(email, password).thenAccept(result -> {
            mainHandler.post(() -> {
                AuthUiState state = _uiState.getValue();
                if (state != null) {
                    if (result.isSuccess()) {
                        _uiState.setValue(state.withLoading(false).withError(null));
                    } else {
                        Exception ex = result.exceptionOrNull();
                        String errorMsg = ex != null ? ex.getMessage() : "Sign in failed";
                        _uiState.setValue(state.withLoading(false).withError(errorMsg));
                    }
                }
            });
        });
    }
    
    public void signUpWithEmail(String email, String password) {
        if (!validateEmailPassword(email, password)) {
            return;
        }
        
        clearValidationErrors();
        AuthUiState currentState = _uiState.getValue();
        if (currentState != null) {
            _uiState.setValue(currentState.withLoading(true).withError(null));
        }
        
        authRepository.signUpWithEmail(email, password).thenAccept(result -> {
            mainHandler.post(() -> {
                AuthUiState state = _uiState.getValue();
                if (state != null) {
                    if (result.isSuccess()) {
                        _uiState.setValue(state.withLoading(false).withError(null).withSuccessMessage("Account created successfully!"));
                    } else {
                        Exception ex = result.exceptionOrNull();
                        String errorMsg = ex != null ? ex.getMessage() : "Sign up failed";
                        _uiState.setValue(state.withLoading(false).withError(errorMsg));
                    }
                }
            });
        });
    }
    
    public void sendPasswordResetEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            updateValidationError("email", "Email is required");
            return;
        }
        
        AuthUiState currentState = _uiState.getValue();
        if (currentState != null) {
            _uiState.setValue(currentState.withLoading(true).withError(null));
        }
        
        authRepository.sendPasswordResetEmail(email).thenAccept(result -> {
            mainHandler.post(() -> {
                AuthUiState state = _uiState.getValue();
                if (state != null) {
                    if (result.isSuccess()) {
                        _uiState.setValue(state.withLoading(false).withError(null).withSuccessMessage("Password reset email sent"));
                    } else {
                        Exception ex = result.exceptionOrNull();
                        String errorMsg = ex != null ? ex.getMessage() : "Failed to send password reset email";
                        _uiState.setValue(state.withLoading(false).withError(errorMsg));
                    }
                }
            });
        });
    }
    
    public void resetPassword(String email) {
        sendPasswordResetEmail(email);
    }
    
    public void signInWithGoogle(String idToken) {
        if (idToken == null || idToken.trim().isEmpty()) {
            AuthUiState currentState = _uiState.getValue();
            if (currentState != null) {
                _uiState.setValue(currentState.withError("Google Sign-In failed: Invalid token"));
            }
            return;
        }
        
        AuthUiState currentState = _uiState.getValue();
        if (currentState != null) {
            _uiState.setValue(currentState.withLoading(true).withError(null));
        }
        
        authRepository.signInWithGoogle(idToken).thenAccept(result -> {
            mainHandler.post(() -> {
                AuthUiState state = _uiState.getValue();
                if (state != null) {
                    if (result.isSuccess()) {
                        _uiState.setValue(state.withLoading(false).withError(null));
                    } else {
                        Exception ex = result.exceptionOrNull();
                        String errorMsg = ex != null ? ex.getMessage() : "Google sign in failed";
                        _uiState.setValue(state.withLoading(false).withError(errorMsg));
                    }
                }
            });
        });
    }
    
    public void signOut() {
        authRepository.signOut();
        _authState.setValue(AuthState.unauthenticated());
        clearValidationErrors();
        AuthUiState currentState = _uiState.getValue();
        if (currentState != null) {
            _uiState.setValue(currentState.withError(null).withSuccessMessage(null));
        }
    }
    
    public void clearErrors() {
        AuthUiState currentState = _uiState.getValue();
        if (currentState != null) {
            _uiState.setValue(currentState.withError(null).withSuccessMessage(null));
        }
        clearValidationErrors();
    }
    
    private boolean validateEmailPassword(String email, String password) {
        boolean isValid = true;
        Map<String, String> errors = new HashMap<>();
        
        if (email == null || email.trim().isEmpty()) {
            errors.put("email", "Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errors.put("email", "Please enter a valid email address");
            isValid = false;
        }
        
        if (password == null || password.trim().isEmpty()) {
            errors.put("password", "Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            errors.put("password", "Password must be at least 6 characters");
            isValid = false;
        }
        
        _validationErrors.setValue(errors);
        return isValid;
    }
    
    private void updateValidationError(String field, String error) {
        Map<String, String> currentErrors = _validationErrors.getValue();
        if (currentErrors == null) {
            currentErrors = new HashMap<>();
        } else {
            currentErrors = new HashMap<>(currentErrors);
        }
        currentErrors.put(field, error);
        _validationErrors.setValue(currentErrors);
    }
    
    private void clearValidationErrors() {
        _validationErrors.setValue(new HashMap<>());
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (authStateListener != null && firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
    
    // UI State class
    public static class AuthUiState {
        private final boolean isLoading;
        private final String errorMessage;
        private final String successMessage;
        
        public AuthUiState() {
            this(false, null, null);
        }
        
        public AuthUiState(boolean isLoading, String errorMessage, String successMessage) {
            this.isLoading = isLoading;
            this.errorMessage = errorMessage;
            this.successMessage = successMessage;
        }
        
        public boolean isLoading() {
            return isLoading;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public String getSuccessMessage() {
            return successMessage;
        }
        
        public AuthUiState withLoading(boolean loading) {
            return new AuthUiState(loading, this.errorMessage, this.successMessage);
        }
        
        public AuthUiState withError(String error) {
            return new AuthUiState(this.isLoading, error, null);
        }
        
        public AuthUiState withSuccessMessage(String message) {
            return new AuthUiState(this.isLoading, null, message);
        }
    }
    
    // Auth State class
    public static class AuthState {
        private final User user;
        private final boolean isAuthenticated;
        private final boolean isLoading;
        
        private AuthState(User user, boolean isAuthenticated, boolean isLoading) {
            this.user = user;
            this.isAuthenticated = isAuthenticated;
            this.isLoading = isLoading;
        }
        
        public static AuthState authenticated(User user) {
            return new AuthState(user, true, false);
        }
        
        public static AuthState unauthenticated() {
            return new AuthState(null, false, false);
        }
        
        public static AuthState loading() {
            return new AuthState(null, false, true);
        }
        
        public User getUser() {
            return user;
        }
        
        public boolean isAuthenticated() {
            return isAuthenticated;
        }
        
        public boolean isUnauthenticated() {
            return !isAuthenticated && !isLoading;
        }
        
        public boolean isLoading() {
            return isLoading;
        }
    }
}