package club.letget.howolddoilook.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import club.letget.howolddoilook.entity.Face;

/**
 * @author Maiffy
 */
public class BitmapUtil {


    private Context context;
    public BitmapUtil(Context context) {
        this.context = context;
    }

    /**
     * 根据照片的Uri地址获得照片的Bitmap对象
     * @param imgUri 照片的Uri地址
     * @return 照片的Bitmap对象
     * @throws IOException
     */
    public Bitmap getBitmapByUri(Uri imgUri) throws IOException{
        return MediaStore.Images.Media.getBitmap(context.getContentResolver(), imgUri);
    }

    /**
     * 根据照片的Uri得到照片的byte[]值
     * @param uri 照片的Uri地址
     * @return 照片的byte[]值，出现异常则返回NULL
     */
    public byte[] getByteByImageUri(Uri uri){
        try{
            Bitmap bitmap = this.getBitmapByUri(uri);
            ByteArrayOutputStream out  = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

            bitmap.recycle();
            return out.toByteArray();
        }catch (Exception e){
            return null;
        }
    }

    /**
     * 根据图片的Uri和Face集合，绘制出面部范围和年龄性别信息
     * @param srcImgUti 照片的Uri
     * @param faces Face面部信息集合
     * @return 绘制好的Bitmap
     * @throws Exception
     */
    public Bitmap getBitmapByFaces(Uri srcImgUti, List<Face> faces) throws Exception{

        Bitmap srcBitmap = this.getBitmapByUri(srcImgUti);

        Bitmap resultBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), srcBitmap.getConfig());

        int imgWidth = srcBitmap.getWidth();
        int imgHeight = srcBitmap.getHeight();
        Log.d("face","图片的尺寸：高："+imgHeight+",宽："+imgWidth);
        Canvas canvas = new Canvas(resultBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.rgb(66, 174, 213));

        canvas.drawBitmap(srcBitmap, new Matrix(), paint);
        srcBitmap.recycle();

        paint.setAlpha(200);
        Face face;
        for (int i = 0; i < faces.size(); i++){

            face = faces.get(i);

            Log.d("face",face.toString());
            paint.setColor(Color.rgb(66, 174, 213));
            //脸部中心点坐标
            int centerX = (int)(imgWidth*face.getCenterX()/100);
            int centerY = (int)(imgHeight*face.getCenterY()/100);
            Log.d("face","脸部中心点坐标：X:"+centerX+",Y:"+centerY);
            //脸部大小
            int height = (int)(imgHeight*face.getHeight()/100);
            int width = (int)(imgWidth*face.getWidth()/100);
            Log.d("face","脸部大小：高："+height+",宽:"+width);
            //脸部轮廓
            Rect faceRect = new Rect(centerX-width/2,centerY-height/2,centerX+width/2,centerY+height/2);

            paint.setStyle(Paint.Style.STROKE);//设置画笔为不填满
            paint.setStrokeWidth(8.0f);//设置画笔的粗细
            canvas.drawRect(faceRect, paint);//绘制脸部轮廓的长方形

            //显示性别和年龄的长方形，显示在脸部轮廓下面
            Rect ageRect = new Rect();


            if(centerY <= imgHeight/2){//下边距离大
                if(imgHeight-(centerY+height/2+height/4+20) > 1){//下边能放下
                    ageRect.set(centerX-width/2,centerY+height/2+20,centerX+width/2,centerY+height/2+height/4+20);
                }else{//下面放不下，只能放在里面了
                    ageRect.set(centerX-width/2+15,centerY+height/4-10,centerX+width/2-15,centerY+height/2-10);
                }
            }else{//上边距离大
                if(  centerY-height/2-height/4-20 > 1 ){//上边能放下
                    ageRect.set(centerX-width/2,centerY-height/2-height/4-20,centerX+width/2,centerY-height/2-20);
                }else{//下边放不下，只能放在里面了
                    ageRect.set(centerX-width/2+15,centerY+height/4-10,centerX+width/2-15,centerY+height/2-10);
                }
            }

            paint.setStyle(Paint.Style.FILL_AND_STROKE);//设置画笔为填充
            canvas.drawRect(ageRect, paint);

            //在年龄矩形里绘制年龄性别信息

            paint.setStrokeWidth(1);
            paint.setFlags(Paint.LINEAR_TEXT_FLAG);
            paint.setTextSize(height / 5);
            Paint.FontMetricsInt fontMetricsInt = paint.getFontMetricsInt();
            int baseLine = ageRect.top + (ageRect.bottom - ageRect.top - fontMetricsInt.bottom + fontMetricsInt.top)/2 - fontMetricsInt.top;
            paint.setTextAlign(Paint.Align.CENTER);

            paint.setColor(Color.WHITE);
            String ageAndGender = face.getGender()+" "+face.getAge();
            canvas.drawText(ageAndGender, ageRect.centerX(), baseLine, paint);

        }

        return resultBitmap;
    }

    /**
     * 把给定的Bitmap保存到手机上的DCIM目录下
     * @param bitmap
     * @return
     */
    public void saveImgToSD(Bitmap bitmap){

        String fileName = "IMG_"+android.text.format.DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA))+".png";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),fileName);
        String state = Environment.getExternalStorageState();
//        if (Environment.MEDIA_MOUNTED.equals(state)) {
//            Toast.makeText(context,"sd卡可写",Toast.LENGTH_SHORT).show();
//        }else {
//            Toast.makeText(context,"sd卡不可写",Toast.LENGTH_SHORT).show();
//        }
        try{
            if (!file.mkdirs()) {
                Log.e("sss", "Directory not created");
            }


            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,fos);


            fos.flush();
            fos.close();
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(context,"保存失败。。",Toast.LENGTH_LONG).show();
        }
        //return Uri.fromFile(file);

    }
}

