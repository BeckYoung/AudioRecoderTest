package com.example.audiorecodertest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int AUDIO_RUNNING = 1;
    public static final int AUDIO_STOP = 0;
    public static final int AUDIO_PCM_RUNNING = 2;
    public static final int AUDIO_PCM_STOP = -2;

    private Button btn_audio;
    private TextView tv_audio;
    private AudioRecordTask audioRecordTask;
    private Button btn_pcm_play;
    private TextView tv_pcm_playing;
    private AudioTrackTast audioTrackTast;

    private ExecutorService executorPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission
                .WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager
                    .PERMISSION_GRANTED) {
                requestPermissions(permissions, 1000);
            }
        }
        audioRecordTask = new AudioRecordTask();
        audioTrackTast = new AudioTrackTast();

        btn_audio = findViewById(R.id.btn_audio);
        tv_audio = findViewById(R.id.tv_audio);
        btn_pcm_play = findViewById(R.id.btn_pcm_play);
        tv_pcm_playing = findViewById(R.id.tv_pcm_playing);
        btn_audio.setOnClickListener(this);
        btn_pcm_play.setOnClickListener(this);

        executorPool = Executors.newSingleThreadExecutor();

        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainMessage(MainActMessage message) {
        int type=message.getActionType();
        if(!TextUtils.isEmpty(message.getPcmFilePath())){
            audioTrackTast.setPcmFilePath(message.getPcmFilePath());
        }

        if (type == AUDIO_RUNNING) {
            tv_audio.setVisibility(View.VISIBLE);
            btn_audio.setText(R.string.audio_status_stop);
        } else if(type==AUDIO_STOP){
            tv_audio.setVisibility(View.GONE);
            btn_audio.setText(R.string.audio_status_start);
        } else if(type== AUDIO_PCM_RUNNING){
            tv_pcm_playing.setVisibility(View.VISIBLE);
            btn_pcm_play.setText(R.string.audio_pcm_status_stop);
        } else if(type== AUDIO_PCM_STOP){
            tv_pcm_playing.setVisibility(View.GONE);
            btn_pcm_play.setText(R.string.audio_pcm_status_play);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        int len = permissions.length;
        if (requestCode == 1000) {
            boolean isAll = true; //全部权限是否申请成功
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAll = false;
                }
            }

            if (!isAll) {
                Toast.makeText(this, "权限未全部同意！", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_audio:
                if (audioRecordTask.isAudioRunning()) {
                    // 正在录音,停止录音
                    audioRecordTask.setAudioRunning(false);
                } else {
                    executorPool.execute(audioRecordTask);
                }
                break;
            case R.id.btn_pcm_play:
                if (audioRecordTask.isAudioRunning()) {
                    Toast.makeText(this,"正在录音，无法播放！",Toast.LENGTH_LONG).show();
                } else {
                    if(audioTrackTast.getPlayStatus()== AudioTrack.PLAYSTATE_PLAYING){
                        audioTrackTast.setPlayStatus(AudioTrack.PLAYSTATE_STOPPED);
                    }else {
                        executorPool.execute(audioTrackTast);
                    }
                }
                break;
            default:
                break;
        }
    }

    public static class MainActMessage {
        private int actionType;
        private String pcmFilePath;

        public String getPcmFilePath() {
            return pcmFilePath;
        }

        public void setPcmFilePath(String pcmFilePath) {
            this.pcmFilePath = pcmFilePath;
        }

        public int getActionType() {
            return actionType;
        }

        public void setActionType(int actionType) {
            this.actionType = actionType;
        }
    }
}
