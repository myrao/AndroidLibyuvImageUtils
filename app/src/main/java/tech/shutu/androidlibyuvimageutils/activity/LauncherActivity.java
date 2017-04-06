package tech.shutu.androidlibyuvimageutils.activity;

import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.OnClick;
import tech.shutu.androidlibyuvimageutils.R;

/**
 * Created by raomengyang on 31/03/2017.
 */

public class LauncherActivity extends BaseActivity {
    @BindView(R.id.btn_image)
    Button btnImage;
    @BindView(R.id.btn_camera)
    Button btnCamera;

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_launcher);
    }

    @Override
    public void initViews() {

    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.btn_image, R.id.btn_camera})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_image:
                BitmapToYuvActivity.startActivity(this);
                break;
            case R.id.btn_camera:
                CameraPreviewActivity.startActivity(this);
                break;
        }
    }
}
