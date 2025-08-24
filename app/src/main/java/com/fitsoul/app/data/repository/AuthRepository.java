package com.fitsoul.app.data.repository;

import com.fitsoul.app.domain.model.User;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthRepository {
    
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    
    @Inject
    public AuthRepository(FirebaseAuth firebaseAuth, FirebaseFirestore firestore) {
        this.firebaseAuth = firebaseAuth;
        this.firestore = firestore;
    }
    
    public boolean isSignedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
    
    public CompletableFuture<Result<User>> getCurrentUser() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    User user = new User.Builder()
                        .setUid(firebaseUser.getUid())
                        .setEmail(firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "")
                        .setDisplayName(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "")
                        .build();
                    return Result.success(user);
                } else {
                    return Result.failure(new Exception("No authenticated user"));
                }
            } catch (Exception e) {
                return Result.failure(e);
            }
        });
    }
    
    public CompletableFuture<Result<User>> signInWithEmail(String email, String password) {
        CompletableFuture<Result<User>> future = new CompletableFuture<>();
        
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    AuthResult authResult = task.getResult();
                    if (authResult != null && authResult.getUser() != null) {
                        FirebaseUser firebaseUser = authResult.getUser();
                        User user = new User.Builder()
                            .setUid(firebaseUser.getUid())
                            .setEmail(firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "")
                            .setDisplayName(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "")
                            .build();
                        future.complete(Result.success(user));
                    } else {
                        future.complete(Result.failure(new Exception("Authentication result is null")));
                    }
                } else {
                    Exception exception = task.getException();
                    future.complete(Result.failure(exception != null ? exception : new Exception("Sign in failed")));
                }
            });
            
        return future;
    }
    
    public CompletableFuture<Result<User>> signUpWithEmail(String email, String password) {
        CompletableFuture<Result<User>> future = new CompletableFuture<>();
        
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    AuthResult authResult = task.getResult();
                    if (authResult != null && authResult.getUser() != null) {
                        FirebaseUser firebaseUser = authResult.getUser();
                        User user = new User.Builder()
                            .setUid(firebaseUser.getUid())
                            .setEmail(firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "")
                            .setDisplayName(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "")
                            .build();
                        
                        // Save user data to Firestore
                        saveUserToFirestore(user).thenAccept(saveResult -> {
                            // Return success even if Firestore save fails (Firebase Auth succeeded)
                            future.complete(Result.success(user));
                        });
                    } else {
                        future.complete(Result.failure(new Exception("Authentication result is null")));
                    }
                } else {
                    Exception exception = task.getException();
                    future.complete(Result.failure(exception != null ? exception : new Exception("Sign up failed")));
                }
            });
            
        return future;
    }
    
    public CompletableFuture<Result<Void>> sendPasswordResetEmail(String email) {
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    future.complete(Result.success(null));
                } else {
                    Exception exception = task.getException();
                    future.complete(Result.failure(exception != null ? exception : new Exception("Password reset failed")));
                }
            });
            
        return future;
    }
    
    public CompletableFuture<Result<Void>> sendEmailVerification() {
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        future.complete(Result.success(null));
                    } else {
                        Exception exception = task.getException();
                        future.complete(Result.failure(exception != null ? exception : new Exception("Email verification failed")));
                    }
                });
        } else {
            future.complete(Result.failure(new Exception("No authenticated user")));
        }
        
        return future;
    }
    
    public boolean isEmailVerified() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null && user.isEmailVerified();
    }
    
    public CompletableFuture<Result<User>> signInWithGoogle(String idToken) {
        CompletableFuture<Result<User>> future = new CompletableFuture<>();
        
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    AuthResult authResult = task.getResult();
                    if (authResult != null && authResult.getUser() != null) {
                        FirebaseUser firebaseUser = authResult.getUser();
                        User user = new User.Builder()
                            .setUid(firebaseUser.getUid())
                            .setEmail(firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "")
                            .setDisplayName(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "")
                            .build();
                        
                        // Save/update user data to Firestore for Google Sign-In
                        saveUserToFirestore(user).thenAccept(saveResult -> {
                            future.complete(Result.success(user));
                        });
                    } else {
                        future.complete(Result.failure(new Exception("Google sign in result is null")));
                    }
                } else {
                    Exception exception = task.getException();
                    future.complete(Result.failure(exception != null ? exception : new Exception("Google sign in failed")));
                }
            });
            
        return future;
    }
    
    private CompletableFuture<Result<Void>> saveUserToFirestore(User user) {
        CompletableFuture<Result<Void>> future = new CompletableFuture<>();
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("email", user.getEmail());
        userData.put("displayName", user.getDisplayName());
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("lastLoginAt", System.currentTimeMillis());
        
        firestore.collection("users").document(user.getUid())
            .set(userData)
            .addOnSuccessListener(aVoid -> {
                future.complete(Result.success(null));
            })
            .addOnFailureListener(e -> {
                future.complete(Result.failure(e));
            });
            
        return future;
    }
    
    public void signOut() {
        firebaseAuth.signOut();
    }
    
    // Result wrapper class
    public static class Result<T> {
        private final T data;
        private final Exception exception;
        private final boolean isSuccess;
        
        private Result(T data, Exception exception, boolean isSuccess) {
            this.data = data;
            this.exception = exception;
            this.isSuccess = isSuccess;
        }
        
        public static <T> Result<T> success(T data) {
            return new Result<>(data, null, true);
        }
        
        public static <T> Result<T> failure(Exception exception) {
            return new Result<>(null, exception, false);
        }
        
        public boolean isSuccess() {
            return isSuccess;
        }
        
        public boolean isFailure() {
            return !isSuccess;
        }
        
        public T getOrNull() {
            return isSuccess ? data : null;
        }
        
        public Exception exceptionOrNull() {
            return !isSuccess ? exception : null;
        }
        
        public T getOrDefault(T defaultValue) {
            return isSuccess ? data : defaultValue;
        }
    }
}
