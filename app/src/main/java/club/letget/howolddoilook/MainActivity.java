package club.letget.howolddoilook;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;
import com.facepp.result.FaceppResult;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import club.letget.howolddoilook.entity.Face;
import club.letget.howolddoilook.utils.BitmapUtil;
import club.letget.howolddoilook.widget.HeartProgressBar;
import club.letget.howolddoilook.widget.RippleTextView;


public class MainActivity extends Activity {

    private static final int CAMERA_IMAGE_ACTIVITY_REQUEST_CODE = 100;//打开相机的请求码
    private static final int CROP_IMAGE_ACTIVITY_REQUEST_CODE = 200;//打开截图的请求码
    private static final int GALLERY_IMAGE_ACTIVITY_REQUEST_CODE = 300;//打开图库的请求码

    private long exitTime=0;
    private BitmapUtil bitmapUtil;

    private RippleTextView choiceRTv, picRtv, cameraRtv, saveRtv, shareRtv, backRtv;
    private HeartProgressBar progress;
    private ImageView image, result_IV;
    private TextView waitTv;

    private File cameraFile;
    private MyTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    //初始化控件并设置点击事件
    private void initView() {

        task = new MyTask();

        bitmapUtil = new BitmapUtil(this);

        choiceRTv = (RippleTextView)findViewById(R.id.choice_rtv);
        picRtv = (RippleTextView)findViewById(R.id.pic_rtv);
        cameraRtv = (RippleTextView)findViewById(R.id.camera_rtv);

        progress = (HeartProgressBar)findViewById(R.id.progress);
        waitTv = (TextView)findViewById(R.id.wait_tv);

        image = (ImageView)findViewById(R.id.image);
        result_IV = (ImageView)findViewById(R.id.result_iv);

        saveRtv = (RippleTextView) findViewById(R.id.save);
        shareRtv = (RippleTextView) findViewById(R.id.share);
        backRtv = (RippleTextView) findViewById(R.id.back);


        MyButtonOnClickListener listener = new MyButtonOnClickListener();
        choiceRTv.setOnClickListener(listener);
        cameraRtv.setOnClickListener(listener);
        picRtv.setOnClickListener(listener);
        backRtv.setOnClickListener(listener);



    }

    class MyButtonOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
           switch (view.getId()){

               case R.id.choice_rtv :
                   choiceRTv.setVisibility(View.INVISIBLE);
                   picRtv.setVisibility(View.VISIBLE);
                   cameraRtv.setVisibility(View.VISIBLE);
                   cameraRtv.setEnabled(true);
                   picRtv.setEnabled(true);

                   break;
               case R.id.pic_rtv :



                   //直接打开系统图库，让用用户选择图片
                   Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                   startActivityForResult(galleryIntent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE);

                   break;
               case R.id.camera_rtv ://  打开相机拍照

                   Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                   //拍照文件保存的位置
                   String cameraFileName = "IMG_"+android.text.format.DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA))+".jpg";
                   cameraFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), cameraFileName);

                    //设置保存的位置
                   intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile));
                   //启动拍照Activity
                   MainActivity.this.startActivityForResult(intent, CAMERA_IMAGE_ACTIVITY_REQUEST_CODE);
                   break;
               case R.id.back : //返回按钮

                   result_IV.setVisibility(View.INVISIBLE);
                   image.setVisibility(View.VISIBLE);
                   waitTv.setVisibility(View.INVISIBLE);
                   choiceRTv.setVisibility(View.VISIBLE);
                   saveRtv.setVisibility(View.INVISIBLE);
                   shareRtv.setVisibility(View.INVISIBLE);
                   backRtv.setVisibility(View.INVISIBLE);
                   break;

           }
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAMERA_IMAGE_ACTIVITY_REQUEST_CODE){//拍完照片
            if (resultCode == RESULT_OK) {

                //打开截图Activity的intent
                Intent cropIntent = new Intent("com.android.camera.action.CROP");
                cropIntent.setDataAndType(Uri.fromFile(cameraFile), "image/*");//设置要裁剪的图片
                cropIntent.putExtra("crop", "true");//设置裁剪

                //设置长宽比
                cropIntent.putExtra("aspectX",1);
                cropIntent.putExtra("aspectY",1);


                //打开裁剪
                startActivityForResult(cropIntent, CROP_IMAGE_ACTIVITY_REQUEST_CODE);

            }

        }
        if(requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE){//裁剪完照片



            if (resultCode == RESULT_OK) {



                image.setVisibility(View.INVISIBLE);
                result_IV.setMaxHeight(result_IV.getWidth());
                result_IV.setMinimumHeight(result_IV.getWidth());

                result_IV.setImageURI(data.getData());


                task.execute(data.getData());
                cameraRtv.setEnabled(false);
                picRtv.setEnabled(false);

            }
        }
        if(requestCode == GALLERY_IMAGE_ACTIVITY_REQUEST_CODE){
            if (resultCode == RESULT_OK) {

                //打开截图Activity的intent
                Intent cropIntent = new Intent("com.android.camera.action.CROP");
                cropIntent.setDataAndType(data.getData(),"image/*");//设置要裁剪的图片
                cropIntent.putExtra("crop", "true");//设置裁剪
                //设置长宽比
                cropIntent.putExtra("aspectX",1);
                cropIntent.putExtra("aspectY",1);
                //打开截图
                startActivityForResult(cropIntent, CROP_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    class MyTask extends AsyncTask<Uri ,Void , FaceppResult>{
        private Uri srcImgUri;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.start();
            waitTv.setText(R.string.wait_tv_start);
            waitTv.setVisibility(View.VISIBLE);
        }

        @Override
        protected FaceppResult doInBackground(Uri... uris) {
            srcImgUri = uris[0];
            FaceppResult result = null;
            try{
                HttpRequests httpRequests = new HttpRequests("ec099ea60ae023793f5e4230eb749737", "DXqkH1_ngsy91kfFTICCFgmhFhW-lt4n");

                result = httpRequests.detectionDetect(new PostParameters().setImg(bitmapUtil.getByteByImageUri(uris[0])));

                Log.d("json",result.toString());

            }catch (Exception e){

            }
            return result;
        }
        @Override
        protected void onPostExecute(FaceppResult faceppResult) {
            super.onPostExecute(faceppResult);
            if(faceppResult!=null) {
                try{
                    if(faceppResult.get("face").getCount() > 0) {

                        List<Face> faces = new ArrayList();

                        for (int i = 0; i < faceppResult.get("face").getCount(); i++) {
                            Face face = new Face();

                            face.setAge(new JSONObject(faceppResult.get("face").get(i).get("attribute").get("age").toString()).getInt("value"));
                            face.setGender(new JSONObject(faceppResult.get("face").get(i).get("attribute").get("gender").toString()).getString("value"));
                            String center = faceppResult.get("face").get(i).get("position").get("center").toString();
                            face.setCenterX(new JSONObject(center).getDouble("x"));
                            face.setCenterY(new JSONObject(center).getDouble("y"));
                            face.setHeight(Double.parseDouble(faceppResult.get("face").get(i).get("position").get("height").toString()));
                            face.setWidth(Double.parseDouble(faceppResult.get("face").get(i).get("position").get("width").toString()));

                            faces.add(face);
                        }
                        final Bitmap resultBitmap = bitmapUtil.getBitmapByFaces(this.srcImgUri, faces);
                        result_IV.setImageBitmap(resultBitmap);
                        //删除掉截图
                        getContentResolver().delete(this.srcImgUri, null, null);
                        progress.dismiss();
                        waitTv.setText(R.string.wait_tv_result);

                        cameraRtv.setVisibility(View.INVISIBLE);
                        picRtv.setVisibility(View.INVISIBLE);

                        backRtv.setVisibility(View.VISIBLE);
                        shareRtv.setVisibility(View.VISIBLE);
                        saveRtv.setVisibility(View.VISIBLE);
                        //TODO 给save设置点击事件，保存Bitmap到SD卡
//                        saveRtv.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                //把bitmap保存到SD卡
////                                Uri uri =
//                                        bitmapUtil.saveImgToSD(resultBitmap);
//                                //发送广播更新图库
//                                Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
////                                intentBc.setData(uri);
//                                MainActivity.this.sendBroadcast(intentBc);
//                            }
//                        });

                    }else{
                        progress.dismiss();
                        waitTv.setText(getString(R.string.wait_tv_noface));
                    }

                }catch (Exception f){

                    progress.dismiss();
                    waitTv.setText(getString(R.string.wait_tv_erro2));
                }
            }

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN){
            if(System.currentTimeMillis() - exitTime > 2000){
                Toast.makeText(getApplicationContext(), R.string.repress_quit, Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }else{
                finish();
                if(task.isCancelled()){
                    task.cancel(true);
                }
                android.os.Process.killProcess(android.os.Process.myPid());//杀死自己的进程
            }
            return true;
        }
        else return super.onKeyDown(keyCode, event);
    }
}
