package tech.shutu.androidlibyuvimageutils.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tech.shutu.androidlibyuvimageutils.R;
import tech.shutu.androidlibyuvimageutils.utils.FileUtil;
import tech.shutu.jni.YuvUtils;

/**
 * Created by raomengyang on 15/01/2017.
 */

public class BitmapToYuvActivity extends BaseActivity {

    private static final String TAG = BitmapToYuvActivity.class.getSimpleName();

    @BindView(R.id.iv_preview)
    ImageView ivPreview;
    @BindView(R.id.btn_start)
    Button btnStart;
    @BindView(R.id.tv_cost_time)
    TextView tvCostTime;
    @BindView(R.id.tv_cost_time_result)
    TextView tvCostTimeResult;
    @BindView(R.id.ll_time)
    LinearLayout llTime;
    @BindView(R.id.tv_data_size)
    TextView tvDataSize;
    @BindView(R.id.tv_process_data_size)
    TextView tvProcessDataSize;
    @BindView(R.id.ll_data_size)
    LinearLayout llDataSize;
    @BindView(R.id.tv_image_src_width)
    TextView tvImageSrcWidth;
    @BindView(R.id.tv_process_image_src_width)
    TextView tvProcessImageSrcWidth;
    @BindView(R.id.tv_image_src_height)
    TextView tvImageSrcHeight;
    @BindView(R.id.tv_process_image_src_height)
    TextView tvProcessImageSrcHeight;
    @BindView(R.id.ll_image_src_width)
    LinearLayout llImageSrcWidth;
    @BindView(R.id.tv_image_dst_width)
    TextView tvImageDstWidth;
    @BindView(R.id.tv_process_image_dst_width)
    TextView tvProcessImageDstWidth;
    @BindView(R.id.tv_image_dst_height)
    TextView tvImageDstHeight;
    @BindView(R.id.tv_process_image_dst_height)
    TextView tvProcessImageDstHeight;
    @BindView(R.id.ll_image_dst_width)
    LinearLayout llImageDstWidth;
    @BindView(R.id.rb_java)
    RadioButton rbJava;
    @BindView(R.id.rb_libyuv)
    RadioButton rbLibyuv;
    @BindView(R.id.rg_choose_process_type)
    RadioGroup rgChooseProcessType;
    @BindView(R.id.rb_normal)
    RadioButton rbNormal;
    @BindView(R.id.rb_with_scale)
    RadioButton rbWithScale;
    @BindView(R.id.rg_choose_libyuv_process_type)
    RadioGroup rgChooseLibyuvProcessType;

    private static final int SRC_WIDTH = 1280;
    private static final int SRC_HEIGHT = 720;

    private static final int DST_WIDTH = 480;
    private static final int DST_HEIGHT = 270;

    private int srcYuvLength;
    private int srcArgbLength;
    private int dstYuvLength;

    private boolean isUseLibyuv;
    private boolean isLibyuvScaled;

    public static void startActivity(Context ctx) {
        Intent it = new Intent(ctx, BitmapToYuvActivity.class);
        ctx.startActivity(it);
    }

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_bitmap_to_yuv);
        ButterKnife.bind(this);
    }

    @Override
    public void initViews() {
        rgChooseProcessType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_java:
                        isUseLibyuv = false;
                        break;
                    case R.id.rb_libyuv:
                        isUseLibyuv = true;
                        break;
                }
                rgChooseLibyuvProcessType.setVisibility(isUseLibyuv ? View.VISIBLE : View.GONE);
            }
        });

        rgChooseLibyuvProcessType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_normal:
                        isLibyuvScaled = false;
                        break;
                    case R.id.rb_with_scale:
                        isLibyuvScaled = true;
                        break;
                }
            }
        });
    }

    @Override
    public void initData() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.onepiece);
        ivPreview.setImageBitmap(bitmap);
    }


    @OnClick(R.id.btn_start)
    public void onClick() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.onepiece);
        long beginTimeMS = System.currentTimeMillis();
        long costTimeMS;
        srcYuvLength = bitmap.getWidth() * bitmap.getHeight() * 3 / 2;
        srcArgbLength = bitmap.getWidth() * bitmap.getHeight() * 4; // In format argb8888, per pixel cost 4 bytes, if it's rgb565, you should calculate the length of srcArgb by yourself.
        byte[] dstYuv;
        if (isUseLibyuv) {
            if (isLibyuvScaled) {
                dstYuvLength = DST_WIDTH * DST_HEIGHT * 3 / 2;
                YuvUtils.allocateMemo(srcYuvLength, srcArgbLength, dstYuvLength);
                dstYuv = new byte[dstYuvLength];
                YuvUtils.rgbToYuvWidthScaleBylibyuv(bitmap, dstYuv, SRC_WIDTH, SRC_HEIGHT, DST_WIDTH, DST_HEIGHT);
            } else {
                dstYuvLength = bitmap.getWidth() * bitmap.getHeight() * 3 / 2;
                YuvUtils.allocateMemo(srcYuvLength, srcArgbLength, dstYuvLength);
                dstYuv = new byte[dstYuvLength];
                YuvUtils.rgbToYuvBylibyuv(bitmap, dstYuv);
            }
            costTimeMS = System.currentTimeMillis() - beginTimeMS;
            YuvUtils.releaseMemo();
        } else {
            dstYuv = YuvUtils.getNV21(SRC_WIDTH, SRC_HEIGHT, bitmap);
            costTimeMS = System.currentTimeMillis() - beginTimeMS;
        }
        FileUtil.saveYuvToSdCardStorage(dstYuv); // save yuv bytes to sdcard
        tvCostTimeResult.setText(String.valueOf(costTimeMS) + " ms");
        tvProcessDataSize.setText(String.valueOf(dstYuv.length));
        tvProcessImageSrcWidth.setText(String.valueOf(bitmap.getWidth()));
        tvProcessImageSrcHeight.setText(String.valueOf(bitmap.getHeight()));
        tvProcessImageDstWidth.setText(String.valueOf(DST_WIDTH));
        tvProcessImageDstHeight.setText(String.valueOf(DST_HEIGHT));

        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

}
