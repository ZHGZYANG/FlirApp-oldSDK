//package com.flir.flirapp;
//
//import android.annotation.SuppressLint;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.CountDownTimer;
//
//import org.beyka.tiffbitmapfactory.TiffBitmapFactory;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.LinkedBlockingDeque;
//import java.util.concurrent.LinkedBlockingQueue;
//
//public class VideoHandler {
//    private int finishStatus;
//    private final String filePathName;
//    private BitmapToVideoEncoder bitmapToVideoEncoder;
//    private LinkedBlockingQueue<Bitmap> bitmapQueue = new LinkedBlockingQueue<>(200);
//    private ArrayList<String> fileNames=new ArrayList<>(200);
//    //    private boolean countDownFinished;
//    private int videoCount = 0;
//    private int noEqualToTmp = 0;
//    private int encodeStop = 0;
//
//    public VideoHandler(String filePathName) {
//        this.finishStatus = 0;
//        this.filePathName = filePathName;
//    }
//
//    //    protected void init(int tmp){
////        if (noEqualToTmp!=tmp){
////            taskQueue();
////            noEqualToTmp=tmp;
////        }
////    }
//    protected void init() {
//        taskQueue();
//    }
//
//    private void taskQueue() {
//        int bitmapCount = 0;
//        if (!bitmapQueue.isEmpty()) {
////            timerStart();
////            new Thread(new Runnable() {
////                @Override
////                public void run() {
//            bitmapToVideoEncoder = new BitmapToVideoEncoder(new BitmapToVideoEncoder.IBitmapToVideoEncoderCallback() {
//                @Override
//                public void onEncodingComplete(File outputFile) {
//                }
//            });
//            Bitmap bitmap = bitmapQueue.poll();
//            encodeStop = 0;
////            if (bitmap == null) {
////                bitmapToVideoEncoder.stopEncoding();
////            } else {
////                bitmapToVideoEncoder.startEncoding(bitmap.getWidth(), bitmap.getHeight(), new File(filePathName + "." + videoCount));
//            bitmapToVideoEncoder.startEncoding(bitmap.getWidth(), bitmap.getHeight(), new File(filePathName));
//            videoCount++;
////                while (bitmapCount < 1800 && bitmap != null) {
//            while (true) {
////                    bitmapCount++;
//                if (encodeStop == 1) {
//                    return;
//                }
//                if (bitmap != null) {
//                    bitmapToVideoEncoder.queueFrame(bitmap);
//                    bitmap = bitmapQueue.poll();
//
//                }
//
//            }
////                bitmapToVideoEncoder.stopEncoding();
////                bitmapCount = 0;
////            }
////                }
////            }).start();
//        }
//    }
//
//    private int taskQueue2(String path) throws IOException {
//        bitmapToVideoEncoder = new BitmapToVideoEncoder(new BitmapToVideoEncoder.IBitmapToVideoEncoderCallback() {
//            @Override
//            public void onEncodingComplete(File outputFile) {
//            }
//        });
//        bitmapToVideoEncoder.startEncoding(480, 640, new File(filePathName));
//        for (String value : fileNames){
//            File image=new File(value);
//            //for jpeg and png
//            FileInputStream fis = new FileInputStream(image);
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options);
//
//            //for .tif
////            TiffBitmapFactory.Options options = new TiffBitmapFactory.Options();
////            options.inJustDecodeBounds = true;
////            Bitmap bitmap = TiffBitmapFactory.decodeFile(image, options);
//
//
//            bitmapToVideoEncoder.queueFrame(bitmap);
//        }
//        bitmapToVideoEncoder.stopEncoding();
//        return finished2(path);
//    }
//
//    protected void pass(Bitmap bitmap) throws InterruptedException {
//        bitmapQueue.put(bitmap);
//    }
//
//    protected int pass(String path) throws IOException { // All images were captured
//        if (!getFileNames(path)) return -1;
//        return taskQueue2(path);
//    }
//
//    private boolean getFileNames(String path){
//        File file=new File(path);
//        File[] files=file.listFiles();
//        if (files == null) return false;
//        for (File value : files) {
//            fileNames.add(value.getAbsolutePath());
//        }
//        return true;
//    }
//
//    protected int finished() {
//        while (true) {
//            if (bitmapQueue.isEmpty()) {
//                encodeStop = 1;
//                bitmapToVideoEncoder.stopEncoding();
//                this.finishStatus = 1;
//                return merge();
//            }
//        }
//    }
//
//    private int merge() {
////        for (int i=0;i<videoCount;i++){
////
////        }
//        return 0;
//    }
//
//
//    private int finished2(String path){
//        fileNames.clear();
//        this.finishStatus = 1;
////        deleteAllFiles(new File(path));
//        return 0;
//    }
//
//    private void deleteAllFiles(File root) {
//        File[] files = root.listFiles();
//        if (files != null)
//            for (File f : files) {
//                if (f.isDirectory()) {
//                    deleteAllFiles(f);
//                    try {
//                        f.delete();
//                    } catch (Exception e) {
//                    }
//                } else {
//                    if (f.exists()) {
//                        deleteAllFiles(f);
//                        try {
//                            f.delete();
//                        } catch (Exception e) {
//                        }
//                    }
//                }
//            }
//    }
//
//
////    private final CountDownTimer itimer = new CountDownTimer(5 * 1000, 1000) {
////        @Override
////        public void onTick(long millisUntilFinished) {
////        }
////
////        @Override
////        public void onFinish() {
////            countDownFinished = true;
////        }
////    };
////
////    private void timerCancel() {
////        itimer.cancel();
////    }
////
////    private void timerStart() {
////        countDownFinished=false;
////        itimer.start();
////    }
//
//}
