package tech.shutu.androidlibyuvimageutils.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;

/**
 * Created by raomengyang on 23/12/2016.
 */

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView();
        ButterKnife.bind(this);
        initViews();
        initData();
    }

    public abstract void setContentView();

    public abstract void initViews();

    public abstract void initData();


    @Override
    protected void onResume() {
        super.onResume();
    }
}
