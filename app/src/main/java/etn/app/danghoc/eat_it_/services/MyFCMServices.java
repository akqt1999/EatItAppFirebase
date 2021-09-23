package etn.app.danghoc.eat_it_.services;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

import etn.app.danghoc.eat_it_.Common.Common;

public class MyFCMServices extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String, String> dataRecv = remoteMessage.getData();
        if (dataRecv != null) {
            Log.d("alss","da nhan dc thong bao o ngoai");
            Toast.makeText(this, "nhan duoc thong bao o ngoai", Toast.LENGTH_SHORT).show();
            if (dataRecv.get(Common.IS_SEND_IMAGE) != null && dataRecv.get(Common.IS_SEND_IMAGE).equals("true")) {

                Log.d("alss","da nhan dc thong bao");
                Toast.makeText(this, "nhan duoc thong bao", Toast.LENGTH_SHORT).show();
                Glide.with(this)
                        .asBitmap()
                        .load(dataRecv.get(Common.IMAGE_URL))
                        .into(new CustomTarget<Bitmap>() {

                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                Common.showNotifiCationBigStyle(MyFCMServices.this,new Random().nextInt(),
                                        dataRecv.get(Common.NOTI_TITILE),
                                        dataRecv.get(Common.NOTI_CONTENT),
                                        resource,
                                        null);

                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });

            } else {
                Common.showNotifiCation(this, new Random().nextInt(),
                        dataRecv.get(Common.NOTI_TITILE),
                        dataRecv.get(Common.NOTI_CONTENT),
                        null);
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Common.updateToken(this, s);

    }
}
