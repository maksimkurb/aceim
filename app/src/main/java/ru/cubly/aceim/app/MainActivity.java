package ru.cubly.aceim.app;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ru.cubly.aceim.app.ui.accounts.AccountsFragment;

public class MainActivity extends AppCompatActivity implements AccountsFragment.OnAccountsFragmentInteractionListener {

    Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentFragment = AccountsFragment.newInstance();
        FragmentTransaction trans = getSupportFragmentManager()
                .beginTransaction();
        trans.add(R.id.frame, currentFragment);
        trans.commit();
    }

    @Override
    public void onAccountCreate(View caller) {
        Snackbar.make(caller, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}
