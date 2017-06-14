package com.yalantis.ucrop.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.yalantis.ucrop.R;
import com.yalantis.ucrop.callback.BitmapLoadCallback;
import com.yalantis.ucrop.callback.CropBoundsChangeListener;
import com.yalantis.ucrop.callback.OverlayViewChangeListener;
import com.yalantis.ucrop.model.ExifInfo;
import com.yalantis.ucrop.util.BitmapLoadUtils;

public class UCropView extends FrameLayout {

  private final GestureCropImageView mGestureCropImageView;
  private final OverlayView mViewOverlay;
  private final ImageView mCustomOverlay;
  private Uri mOverlayUri;

  public UCropView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public UCropView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    LayoutInflater.from(context).inflate(R.layout.ucrop_view, this, true);
    mGestureCropImageView = (GestureCropImageView) findViewById(R.id.image_view_crop);
    mViewOverlay = (OverlayView) findViewById(R.id.view_overlay);
    mCustomOverlay = (ImageView) findViewById(R.id.view_custom_overlay);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ucrop_UCropView);
    mViewOverlay.processStyledAttributes(a);
    mGestureCropImageView.processStyledAttributes(a);
    a.recycle();


    mGestureCropImageView.setCropBoundsChangeListener(new CropBoundsChangeListener() {
      @Override
      public void onCropAspectRatioChanged(float cropRatio) {
        mViewOverlay.setTargetAspectRatio(cropRatio);
      }
    });
    mViewOverlay.setOverlayViewChangeListener(new OverlayViewChangeListener() {
      @Override
      public void onCropRectUpdated(RectF cropRect) {
        mGestureCropImageView.setCropRect(cropRect);
        mCustomOverlay.setPadding(
            Math.round(cropRect.left),
            Math.round(cropRect.top),
            Math.round(getWidth() - cropRect.right),
            Math.round(getHeight() - cropRect.bottom));
      }
    });
  }

  @Override
  public boolean shouldDelayChildPressedState() {
    return false;
  }

  @NonNull
  public GestureCropImageView getCropImageView() {
    return mGestureCropImageView;
  }

  @NonNull
  public OverlayView getOverlayView() {
    return mViewOverlay;
  }

  public void setCustomOverlay(@Nullable Uri uri) {
    mOverlayUri = uri;
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldW, int oldH) {
    super.onSizeChanged(w, h, oldW, oldH);
    BitmapLoadUtils.decodeBitmapInBackground(getContext(), mOverlayUri, null, w, h, new BitmapLoadCallback() {
      @Override
      public void onBitmapLoaded(@NonNull Bitmap bitmap, @NonNull ExifInfo exifInfo, @NonNull String imageInputPath, @Nullable String imageOutputPath) {
        mCustomOverlay.setVisibility(VISIBLE);
        mCustomOverlay.setImageBitmap(bitmap);
      }

      @Override
      public void onFailure(@NonNull Exception bitmapWorkerException) {
        mCustomOverlay.setVisibility(GONE);
      }
    });
  }
}