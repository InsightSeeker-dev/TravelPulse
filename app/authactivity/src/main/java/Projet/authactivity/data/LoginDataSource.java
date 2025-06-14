package Projet.authactivity.data;

import Projet.authactivity.data.model.LoggedInUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import androidx.annotation.NonNull;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public interface LoginCallback {
        void onResult(Result<LoggedInUser> result);
    }

    public void login(String username, String password, final LoginCallback callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String displayName = user.getDisplayName();
                            if (displayName == null || displayName.isEmpty()) {
                                displayName = user.getEmail();
                            }
                            callback.onResult(new Result.Success<>(new LoggedInUser(user.getUid(), displayName)));
                        } else {
                            callback.onResult(new Result.Error(new Exception("Utilisateur Firebase introuvable.")));
                        }
                    } else {
                        Exception e = task.getException();
                        // Passe le message d'erreur détaillé dans la cause
                        callback.onResult(new Result.Error(new Exception("Erreur d'authentification Firebase", e)));
                    }
                });
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
    }
}