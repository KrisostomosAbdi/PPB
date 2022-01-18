package android.example.googlelogin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private SignInButton signInButton;
    private GoogleSignInClient googleSignInClient;
    private String TAG="mainTag";
    private FirebaseAuth mAuth;
    private Button btnSignOut;
    private int RESULT_CODE_SINGIN=999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialization
        signInButton = findViewById(R.id.SignIn_Button);
        mAuth = FirebaseAuth.getInstance();
        btnSignOut = findViewById(R.id.Sign_out);
        btnSignOut.setVisibility(View.INVISIBLE);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1066103899362-49i8t44m9d70cs21jbf7qdgus339rl4l.apps.googleusercontent.com")
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(this,gso);

        //Attach a onClickListener
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInM();
            }
        });

        //Attach a onClickListener
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignInClient.signOut();
                btnSignOut.setVisibility(View.INVISIBLE);
                signInButton.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this,"you are logged out",Toast.LENGTH_LONG).show();
            }
        });
    }

    //when the signIn Button is clicked then start the signIn Intent
    private void signInM() {
        Intent singInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(singInIntent,RESULT_CODE_SINGIN);
    }

    // onActivityResult (Here we handle the result of the Activity )
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_CODE_SINGIN) {        //just to verify the code
            //create a Task object and use GoogleSignInAccount from Intent and write a separate method to handle singIn Result.

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }


    private void handleSignInResult(Task<GoogleSignInAccount> task) {

        //we use try catch block because of Exception.
        try {
            signInButton.setVisibility(View.INVISIBLE);
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Toast.makeText(MainActivity.this,"Signed In successfully",Toast.LENGTH_LONG).show();
            //SignIn successful now show authentication
            FirebaseGoogleAuth(account);

        } catch (ApiException e) {
            e.printStackTrace();
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(MainActivity.this,"SignIn Failed!!!",Toast.LENGTH_LONG).show();
            //FirebaseGoogleAuth(null);
        }
    }

    private void FirebaseGoogleAuth(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        //here we are checking the Authentication Credential and checking the task is successful or not and display the message
        //based on that.
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(MainActivity.this,"successful",Toast.LENGTH_LONG).show();
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    UpdateUI(firebaseUser);
                }
                else {
                    Toast.makeText(MainActivity.this,"Failed!",Toast.LENGTH_LONG).show();
                    UpdateUI(null);
                }
            }
        });
    }

    //Inside UpdateUI we can get the user information and display it when required
    private void UpdateUI(FirebaseUser fUser) {
        btnSignOut.setVisibility(View.VISIBLE);

        //getLastSignedInAccount returned the account
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account !=null){
            String personName = account.getDisplayName();
            String personGivenName = account.getGivenName();
            String personEmail = account.getEmail();
            String personId = account.getId();

            Toast.makeText(MainActivity.this,personName + "  " + personEmail,Toast.LENGTH_LONG).show();
        }
    }
}