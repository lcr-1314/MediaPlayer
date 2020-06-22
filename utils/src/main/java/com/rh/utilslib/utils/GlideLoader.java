package com.rh.utilslib.utils;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.widget.ImageView;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.rh.utilslib.R;

import java.io.File;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;


/**
 * @author Bill xiang
 * @description Glide 图片加载
 * @date 2017/12/29
 * @modify
 */

public final class GlideLoader {

    private static Glide glide;
    private static String cacheDir;

    private final static RequestManager.DefaultOptions DEFAULT_OPTIONS = new InternalOptions();

    public static void init(@NonNull Application context) {
        glide = Glide.get(context);
        // glide.setMemoryCategory(MemoryCategory.LOW);
        cacheDir = Glide.getPhotoCacheDir(context).getAbsolutePath();
        Glide.with(context).setDefaultOptions(DEFAULT_OPTIONS);
    }

    public static Uri wrapperUrl(String url) {
        return Uri.parse(url);
    }

    public static Uri wrapperFile(String path) {
        return Uri.fromFile(new File(path));
    }

    public static Uri wrapperAsset(String assetName) {
        return Uri.parse("file:///android_asset/" + assetName);
    }

    public static Uri wrapperRaw(@NonNull Context context, @RawRes int id) {
        return Uri.parse("android.resource://" + context.getPackageName() + "/raw/" + id);
    }

    public static Uri wrapperDrawable(@NonNull Context context, @DrawableRes int id) {
        return Uri.parse("android.resource://" + context.getPackageName() + "/drawable/" + id);
    }

    /**
     * 异步加载一张图片
     *
     * @param view 控件
     * @param uri  网络地址、文件地址、asset地址
     */
    public static TargetWrapper load(@NonNull ImageView view, String uri) {
        TargetWrapper wrapper;
        if (ImageView.ScaleType.CENTER_CROP == view.getScaleType()) {
            Glide.with(view.getContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .centerCrop()
                    .into((wrapper = new TargetWrapper(view)).target);
        } else {
            Glide.with(view.getContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .fitCenter()
                    .into((wrapper = new TargetWrapper(view)).target);
        }
        return wrapper;
    }

    /**
     * 异步加载一张图片
     *
     * @param view 控件
     * @param uri  网络地址、文件地址、asset地址
     */
    public static TargetWrapper load(@NonNull ImageView view, String uri, LoadListener loadListener) {
        TargetWrapper wrapper;
        wrapper = new TargetWrapper(view);
        wrapper.setLoadListener(loadListener);
        if (ImageView.ScaleType.CENTER_CROP == view.getScaleType()) {
            Glide.with(view.getContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .centerCrop()
                    .into(wrapper.target);
        } else {
            Glide.with(view.getContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .fitCenter()
                    .into(wrapper.target);
        }
        return wrapper;
    }

    /**
     * 异步加载一张图片
     *
     * @param view 控件
     * @param uri  网络地址、文件地址、asset地址
     */
    public static TargetWrapper load(@NonNull Context context,
                                     @NonNull ImageView view, String uri) {
        TargetWrapper wrapper;
        if (ImageView.ScaleType.CENTER_CROP == view.getScaleType()) {
            Glide.with(context.getApplicationContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .centerCrop()
                    .into((wrapper = new TargetWrapper(view)).target);
        } else {
            Glide.with(context.getApplicationContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .fitCenter()
                    .into((wrapper = new TargetWrapper(view)).target);
        }
        return wrapper;
    }

    public static TargetWrapper load(@NonNull ImageView view, String uri,
                                     @DrawableRes int loading) {
        TargetWrapper wrapper;
        if (ImageView.ScaleType.CENTER_CROP == view.getScaleType()) {
            Glide.with(view.getContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .fitCenter()
                    .placeholder(loading)
                    .error(loading)
                    .into((wrapper = new TargetWrapper(view)).target);
        } else {
            Glide.with(view.getContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .centerCrop()
                    .placeholder(loading)
                    .error(loading)
                    .into((wrapper = new TargetWrapper(view)).target);
        }
        return wrapper;
    }

    public static TargetWrapper loadCircle(@NonNull ImageView view, String uri,
                                           @DrawableRes int loading) {
        TargetWrapper wrapper;
        if (ImageView.ScaleType.CENTER_CROP == view.getScaleType()) {
            Glide.with(view.getContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .centerCrop()
                    .bitmapTransform(new CropCircleTransformation(view.getContext()))
                    .placeholder(loading)
                    .error(loading)
                    .into((wrapper = new TargetWrapper(view)).target);
        } else {
            Glide.with(view.getContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .fitCenter()
                    .bitmapTransform(new CropCircleTransformation(view.getContext()))
                    .placeholder(loading)
                    .error(loading)
                    .into((wrapper = new TargetWrapper(view)).target);
        }
        return wrapper;
    }

    public static TargetWrapper loadCircle(@NonNull ImageView view, Drawable drawable) {
        TargetWrapper wrapper;
        if (ImageView.ScaleType.CENTER_CROP == view.getScaleType()) {
            Glide.with(view.getContext())
                    .load("")
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .centerCrop()
                    .bitmapTransform(new CropCircleTransformation(view.getContext()))
                    .placeholder(drawable)
                    .error(drawable)
                    .into((wrapper = new TargetWrapper(view)).target);
        } else {
            Glide.with(view.getContext())
                    .load("")
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .fitCenter()
                    .bitmapTransform(new CropCircleTransformation(view.getContext()))
                    .placeholder(drawable)
                    .error(drawable)
                    .into((wrapper = new TargetWrapper(view)).target);
        }
        return wrapper;
    }

    /**
     * 异步加载一张图片
     *
     * @param view 控件
     * @param uri  网络地址、文件地址、asset地址
     */
    public static TargetWrapper load(@NonNull ImageView view, String uri,
                                     @DrawableRes int loading, @DrawableRes int error) {
        TargetWrapper wrapper;
        if (ImageView.ScaleType.CENTER_CROP == view.getScaleType()) {
            Glide.with(view.getContext().getApplicationContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .centerCrop()
                    .placeholder(loading)
                    .error(error)
                    .into((wrapper = new TargetWrapper(view)).target);
        } else {
            Glide.with(view.getContext().getApplicationContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .fitCenter()
                    .placeholder(loading)
                    .error(error)
                    .into((wrapper = new TargetWrapper(view)).target);
        }
        return wrapper;
    }

    /**
     * 异步加载一张图片
     *
     * @param view 控件
     * @param uri  网络地址、文件地址、asset地址
     */
    public static TargetWrapper loadCircle(@NonNull ImageView view, String uri,
                                           @DrawableRes int loading, @DrawableRes int error) {
        TargetWrapper wrapper;
        if (ImageView.ScaleType.CENTER_CROP == view.getScaleType()) {
            Glide.with(view.getContext().getApplicationContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .centerCrop()
                    .bitmapTransform(new CropCircleTransformation(view.getContext()))
                    .placeholder(loading)
                    .error(error)
                    .into((wrapper = new TargetWrapper(view)).target);
        } else {
            Glide.with(view.getContext().getApplicationContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .fitCenter()
                    .bitmapTransform(new CropCircleTransformation(view.getContext()))
                    .placeholder(loading)
                    .error(error)
                    .into((wrapper = new TargetWrapper(view)).target);
        }
        return wrapper;
    }

    /**
     * 异步加载一张图片
     *
     * @param view 控件
     * @param uri  网络地址、文件地址、asset地址
     */
    public static TargetWrapper loadRound(@NonNull ImageView view, String uri,
                                          @DrawableRes int loading, @DrawableRes int error) {
        TargetWrapper wrapper;
        if (ImageView.ScaleType.CENTER_CROP == view.getScaleType()) {
            Glide.with(view.getContext().getApplicationContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .centerCrop()
                    .bitmapTransform(new GlideRoundTransform(view.getContext()))
                    .placeholder(loading)
                    .error(error)
                    .into((wrapper = new TargetWrapper(view)).target);
        } else {
            Glide.with(view.getContext().getApplicationContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .fitCenter()
                    .bitmapTransform(new GlideRoundTransform(view.getContext()))
                    .placeholder(loading)
                    .error(error)
                    .into((wrapper = new TargetWrapper(view)).target);
        }
        return wrapper;
    }

    /**
     * 异步加载一张图片
     *
     * @param view 控件
     * @param uri  网络地址、文件地址、asset地址
     */
    public static TargetWrapper loadBlur(@NonNull ImageView view, String uri,
                                         @DrawableRes int loading, @DrawableRes int error) {
        TargetWrapper wrapper;
        if (ImageView.ScaleType.CENTER_CROP == view.getScaleType()) {
            Glide.with(view.getContext().getApplicationContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .centerCrop()
                    .placeholder(loading)
                    .error(error)
                    .bitmapTransform(new BlurTransformation(
                            view.getContext().getApplicationContext(), 5, 3))
                    .into((wrapper = new TargetWrapper(view)).target);
        } else {
            Glide.with(view.getContext().getApplicationContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .crossFade()
                    .fitCenter()
                    .placeholder(loading)
                    .error(error)
                    .bitmapTransform(new BlurTransformation(
                            view.getContext().getApplicationContext(), 5, 3))
                    .into((wrapper = new TargetWrapper(view)).target);
        }
        return wrapper;
    }

    public static TargetWrapper load(@NonNull Context context, @NonNull ImageView view,
                                     String uri, @DrawableRes int loading) {
        TargetWrapper wrapper;
        Glide.with(context)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .crossFade()
                .placeholder(loading)
                .error(loading)
                .into((wrapper = new TargetWrapper(view)).target);

        return wrapper;
    }

    public static TargetWrapper loadUri(@NonNull Context context, @NonNull ImageView view,
                                        @NonNull Uri uri, @DrawableRes int loading) {
        TargetWrapper wrapper;
        Glide.with(context)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .crossFade()
                .placeholder(loading)
                .error(loading)
                .into((wrapper = new TargetWrapper(view)).target);
        return wrapper;
    }


    public static TargetWrapper loadUri(@NonNull ImageView view,
                                        @NonNull Uri uri) {
        TargetWrapper wrapper;
        Glide.with(view.getContext().getApplicationContext())
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .crossFade()
                .into((wrapper = new TargetWrapper(view)).target);
        return wrapper;
    }

    public static void loadUriGif(@NonNull Context context, @NonNull ImageView view, @NonNull Uri uri) {
        Glide.with(context)
                .load(uri)
                .asGif()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(view);
    }


    public static TargetWrapper loadFile(@NonNull Context context, @NonNull ImageView view,
                                         @NonNull String path, @DrawableRes int loading) {
        return loadUri(context, view, wrapperFile(path), loading);
    }

    public static TargetWrapper loadFile(@NonNull ImageView view, @NonNull String path) {
        return loadUri(view, wrapperFile(path));
    }

    public static TargetWrapper loadDrawable(@NonNull Context context, @NonNull ImageView view,
                                             @DrawableRes int drawableId, @DrawableRes int loading) {
        return loadUri(context, view, wrapperDrawable(context, drawableId), loading);
    }


    public static void loadDrawableGif(@NonNull Context context, @NonNull ImageView view,
                                       @DrawableRes int drawableId) {
        loadUriGif(context, view, wrapperDrawable(context, drawableId));
    }

    public static TargetWrapper loadFile(@NonNull ImageView view,
                                         @NonNull String uri, @DrawableRes int loading) {
        return loadFile(view.getContext(), view, uri, loading);
    }

    public static TargetWrapper loadDrawable(@NonNull ImageView view,
                                             @DrawableRes int drawableId, @DrawableRes int loading) {
        return loadDrawable(view.getContext(), view, drawableId, loading);
    }

    public static void loadDrawableGif(@NonNull ImageView view, @DrawableRes int drawableId) {
        loadDrawableGif(view.getContext(), view, drawableId);
    }

    public static TargetWrapper loadAsset(@NonNull ImageView view,
                                          @NonNull String name, @DrawableRes int loading) {
        return loadUri(view.getContext(), view, wrapperAsset(name), loading);
    }

    public static TargetWrapper loadRaw(@NonNull ImageView view,
                                        @RawRes int rawId, @DrawableRes int loading) {
        return loadUri(view.getContext(), view, wrapperRaw(view.getContext(), rawId), loading);
    }

    /**
     * 异步加载一张图片
     *
     * @param view 控件
     * @param uri  网络地址、文件地址、asset地址
     */
    public static TargetWrapper thumbnail(@NonNull ImageView view, String uri,
                                          @DrawableRes int loading) {
        TargetWrapper wrapper;
        Glide.with(view.getContext().getApplicationContext())
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .crossFade()
                .animate(R.anim.glide_math_out)
                .thumbnail(0.1f)
                .placeholder(loading)
                .into((wrapper = new TargetWrapper(view)).target);
        return wrapper;
    }

    /**
     * 异步加载一张图片
     *
     * @param view 控件
     * @param uri  网络地址、文件地址、asset地址
     */
    public static TargetWrapper thumbnail(@NonNull ImageView view, String uri,
                                          @DrawableRes int loading, @DrawableRes int erro) {
        TargetWrapper wrapper;
        Glide.with(view.getContext().getApplicationContext())
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .crossFade()
                .animate(R.anim.glide_math_out)
                .thumbnail(0.1f)
                .placeholder(loading)
                .error(erro)
                .into((wrapper = new TargetWrapper(view)).target);
        return wrapper;
    }

    /**
     * 异步加载一张图片
     *
     * @param view 控件
     * @param uri  网络地址、文件地址、asset地址
     */
    public static TargetWrapper thumbnail(@NonNull ImageView view, String uri) {
        TargetWrapper wrapper;
        Glide.with(view.getContext().getApplicationContext())
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .crossFade()
                .animate(R.anim.glide_math_out)
                .thumbnail(0.1f)
                .into((wrapper = new TargetWrapper(view)).target);

        return wrapper;
    }

    /**
     * 通过url加载一张图片然后返回bitmap回调
     *
     * @param callback 回调
     */
    public static void loadAsBitmap(@NonNull Context context, @NonNull String uri,
                                    @NonNull final ListenerUtils.SimpleCallback<Bitmap> callback) {
        Glide.with(context)
                .load(uri)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(true)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource,
                                                GlideAnimation<? super Bitmap> glideAnimation) {
                        callback.complete(resource);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        callback.complete(null);
                    }
                });
    }

    public static void pause(@NonNull Context context) {
        Glide.with(context).pauseRequests();
    }

    public static void resume(@NonNull Context context) {
        Glide.with(context).resumeRequests();
    }

    public static void clearDiskCache() {
        if (null == glide) {
            throw new RuntimeException("Please call init method in application creating first.");
        } else {
            glide.clearDiskCache();
        }
    }

    public static void clearMemoryCache() {
        if (null == glide) {
            throw new RuntimeException("Please call init method at application creating first.");
        } else {
            glide.clearMemory();
        }
    }

//    public static long getDiskCacheSize() {
//        return FileUtils.getFileSize(cacheDir);
//    }

    private static final class InternalOptions implements RequestManager.DefaultOptions {

        private Drawable loading;
        private Drawable error;
        private RequestListener requestListener;

        public InternalOptions() {
        }

        public void setLoading(Drawable loading) {
            this.loading = loading;
        }

        public InternalOptions(Drawable loading, Drawable error, RequestListener requestListener) {
            this.loading = loading;
            this.error = error;
            this.requestListener = requestListener;
        }

        public void setLoading(@NonNull Context context, @DrawableRes int loading) {
            this.loading = context.getResources().getDrawable(loading);
        }

        public void setError(Drawable error) {
            this.error = error;
        }

        public void setError(@NonNull Context context, @DrawableRes int error) {
            this.error = context.getResources().getDrawable(error);
        }

        public void setRequestListener(RequestListener requestListener) {
            this.requestListener = requestListener;
        }

        @Override
        public <T> void apply(GenericRequestBuilder<T, ?, ?, ?> requestBuilder) {
            requestBuilder
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(loading)
                    .error(this.error)
                    .listener(requestListener);
        }
    }

    // 圆角转换器
    private final static class GlideRoundTransform extends BitmapTransformation {

        private float radius = 0f;

        public GlideRoundTransform(Context context) {
            this(context, 4);
        }

        private GlideRoundTransform(Context context, int dp) {
            super(context);
            this.radius = Resources.getSystem().getDisplayMetrics().density * dp;
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            return roundCrop(pool, toTransform);
        }

        private Bitmap roundCrop(BitmapPool pool, Bitmap source) {
            if (source == null) return null;

            Bitmap result = pool.get(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
            if (result == null) {
                result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            RectF rectF = new RectF(0f, 0f, source.getWidth(), source.getHeight());
            canvas.drawRoundRect(rectF, radius, radius, paint);
            return result;
        }

        @Override
        public String getId() {
            return getClass().getName() + Math.round(radius);
        }
    }

    private final static class GlideCircleTransform extends BitmapTransformation {

        public GlideCircleTransform(Context context) {
            super(context);
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            return circleCrop(pool, toTransform);
        }

        private Bitmap circleCrop(BitmapPool pool, Bitmap source) {
            if (source == null) return null;

            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

            Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
            if (result == null) {
                result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);
            return result;
        }

        @Override
        public String getId() {
            return getClass().getName();
        }
    }

    public static abstract class LoadListener {

        public void onLoadStart() {
        }

        public abstract void onLoadComplete(Bitmap bitmap, boolean success);

    }

    public static class TargetWrapper {

        private LoadListener loadListener;

        private final GlideDrawableImageViewTarget target;

        private TargetWrapper(ImageView view) {
            this.target = new Target(view);
        }

        public void setLoadListener(LoadListener loadListener) {
            this.loadListener = loadListener;
        }

        private class Target extends GlideDrawableImageViewTarget {

            private Target(ImageView view) {
                super(view);
            }

            private Target(ImageView view, int maxLoopCount) {
                super(view, maxLoopCount);
            }

            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                super.onResourceReady(resource, animation);
                if (null != loadListener) {
                    if (resource instanceof GlideBitmapDrawable) {
                        loadListener.onLoadComplete(((GlideBitmapDrawable) resource).getBitmap(), true);
                    } else if (resource instanceof GifDrawable) {
                        loadListener.onLoadComplete(((GifDrawable) resource).getFirstFrame(), true);
                    }
                }
            }

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onStop() {
                super.onStop();
            }

            @Override
            public void onLoadStarted(Drawable placeholder) {
                super.onLoadStarted(placeholder);
                if (null != loadListener) {
                    loadListener.onLoadStart();
                }
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                if (null != loadListener) {
                    loadListener.onLoadComplete(null, false);
                }
            }

            @Override
            public void onLoadCleared(Drawable placeholder) {
                super.onLoadCleared(placeholder);
            }
        }
    }

}
