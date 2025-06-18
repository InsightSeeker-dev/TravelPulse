package Projet.authactivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import Projet.authactivity.databinding.ActivityRegisterBinding; // Assurez-vous que le nom du layout est activity_register.xml
import Projet.authactivity.ui.login.LoginActivity;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        binding.textViewLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Naviguer vers LoginActivity
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Optionnel: fermer RegisterActivity
            }
        });
    }

    private void registerUser() {
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();
        String confirmPassword = binding.editTextConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.editTextEmail.setError("L'email est requis.");
            binding.editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.setError("Veuillez entrer un email valide.");
            binding.editTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.editTextPassword.setError("Le mot de passe est requis.");
            binding.editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            binding.editTextPassword.setError("Le mot de passe doit contenir au moins 6 caractères.");
            binding.editTextPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.editTextConfirmPassword.setError("La confirmation du mot de passe est requise.");
            binding.editTextConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.editTextConfirmPassword.setError("Les mots de passe ne correspondent pas.");
            binding.editTextConfirmPassword.requestFocus();
            // Effacer les champs de mot de passe pour plus de sécurité/convivialité
            binding.editTextPassword.setText("");
            binding.editTextConfirmPassword.setText("");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.buttonRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.buttonRegister.setEnabled(true);

                        if (task.isSuccessful()) {
                            // L'inscription est réussie
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(RegisterActivity.this, "Inscription réussie.", Toast.LENGTH_SHORT).show();
                            // Optionnel: envoyer un email de vérification
                            // user.sendEmailVerification();
                            
                            // Naviguer vers LoginActivity ou directement vers MainActivity si vous le souhaitez
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // Si l'inscription échoue, afficher un message à l'utilisateur.
                            Toast.makeText(RegisterActivity.this, "Échec de l'inscription: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
