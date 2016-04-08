package me.wcy.weather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import butterknife.Bind;
import cn.bmob.v3.listener.UpdateListener;
import me.wcy.weather.R;
import me.wcy.weather.model.ImageWeather;
import me.wcy.weather.utils.Extras;
import me.wcy.weather.utils.RequestCode;
import me.wcy.weather.utils.ScreenUtils;
import me.wcy.weather.utils.SystemUtils;

public class ViewImageActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "ViewImageActivity";
    @Bind(R.id.iv_weather_image)
    ImageView ivWeatherImage;
    @Bind(R.id.tv_location)
    TextView tvLocation;
    @Bind(R.id.tv_user_name)
    TextView tvUserName;
    @Bind(R.id.tv_say)
    TextView tvSay;
    @Bind(R.id.tv_time)
    TextView tvTime;
    @Bind(R.id.tv_tag)
    TextView tvTag;
    @Bind(R.id.tv_praise)
    TextView tvPraise;
    private ImageWeather mImageWeather;
    private ProgressDialog mProgressDialog;

    public static void start(Activity context, ImageWeather imageWeather) {
        Intent intent = new Intent(context, ViewImageActivity.class);
        intent.putExtra(Extras.IMAGE_WEATHER, imageWeather);
        context.startActivityForResult(intent, RequestCode.REQUEST_VIEW_IMAGE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        mImageWeather = (ImageWeather) getIntent().getSerializableExtra(Extras.IMAGE_WEATHER);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        ImageLoader.getInstance().loadImage(mImageWeather.getImageUrl(), SystemUtils.getDefaultDisplayOption(), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                int imageWidth = ScreenUtils.getScreenWidth() - ScreenUtils.dp2px(12) * 2;
                int imageHeight = (int) ((float) loadedImage.getHeight() / (float) loadedImage.getWidth() * (float) imageWidth);
                ivWeatherImage.setMinimumHeight(imageHeight);
                ivWeatherImage.setImageBitmap(loadedImage);
            }
        });
        tvLocation.setText(mImageWeather.getLocation().getAddress());
        tvUserName.setText(mImageWeather.getUserName());
        tvSay.setText(mImageWeather.getSay());
        tvSay.setVisibility(TextUtils.isEmpty(mImageWeather.getSay()) ? View.GONE : View.VISIBLE);
        tvTag.setText("标签  " + mImageWeather.getTag());
        initTimeAndPraise();
    }

    @Override
    protected void setListener() {
        tvPraise.setOnClickListener(this);
    }

    private void initTimeAndPraise() {
        SimpleDateFormat fullSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm");
        String time = "00:00";
        try {
            time = timeSdf.format(fullSdf.parseObject(mImageWeather.getCreatedAt()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        tvTime.setText(time + "拍摄  " + mImageWeather.getPraise() + "个赞");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_praise:
                praise();
                break;
        }
    }

    private void praise() {
        mProgressDialog.show();
        mImageWeather.increment("praise");
        mImageWeather.update(this, new UpdateListener() {
            @Override
            public void onSuccess() {
                mProgressDialog.cancel();
                mImageWeather.setPraise(mImageWeather.getPraise() + 1);
                initTimeAndPraise();

                Intent data = new Intent();
                data.putExtra(Extras.IMAGE_WEATHER, mImageWeather);
                setResult(RESULT_OK, data);
            }

            @Override
            public void onFailure(int i, String s) {
                Log.e(TAG, "praise fail. code:" + i + ",msg:" + s);
                mProgressDialog.cancel();
            }
        });
    }
}
