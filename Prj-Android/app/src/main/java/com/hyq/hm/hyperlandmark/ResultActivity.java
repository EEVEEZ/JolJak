package com.hyq.hm.hyperlandmark;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.sceneform.samples.hellosceneform.R;

public class ResultActivity extends AppCompatActivity {

    private Button backButton;
    private Button webButton;
    private ImageView faceImage;
    private ImageView capImage1;
    private ImageView capImage2;
    private TextView faceText;
    private TextView recommandTitle;
    private TextView capText1;
    private TextView capText2;
    private TextView reasonText;

    private int result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detection_result);

        Intent intent = getIntent();
        result = intent.getIntExtra("result", -1);

        backButton = findViewById(R.id.Back);
        backButton.setOnClickListener(backButtonClick);
        webButton = findViewById(R.id.Search);
        webButton.setOnClickListener(webButtonClick);
        faceImage = findViewById(R.id.faceImage);
        capImage1 = findViewById(R.id.recommand_cap1);
        capImage2 = findViewById(R.id.recommand_cap2);
        recommandTitle = findViewById(R.id.recommand_title);
        capText1 = findViewById(R.id.recommand_explain1);
        capText2 = findViewById(R.id.recommand_explain2);
        reasonText = findViewById(R.id.reasonText);

        faceText = findViewById(R.id.Shape);
        recommandTitle.setText("추천하는 모자");
        recommandTitle.setTextSize(35.0f);

        System.out.println("result = ");
        System.out.println(result);

        switch (result){
            case 1:{
                faceText.setText("긴 얼굴형 입니다!");
                faceImage.setImageResource(R.drawable.result_long);
                capImage1.setImageResource(R.drawable.recommand_bucket);
                capImage2.setImageResource(R.drawable.recommand_low_cap);
                capText1.setText("버킷 햇");
                capText2.setText("챙이 낮은 캡");
                reasonText.setText("긴 얼굴형은 시선을 얼굴 가운데로 모아주는 게 중요하다. 모자 산이 높으면 위아래로 얼굴이 더 길어 보이는데 벙거지 스타일의 버킷 햇은 얼굴을 짧아 보이게 한다. 캡을 쓸 때도 마찬가지. 모자의 높이가 낮아야 얼굴이 짧아 보인다.");
                break;
            }
            case 2:{
                faceText.setText("각진 얼굴형 입니다!");
                faceImage.setImageResource(R.drawable.result_rectangle);
                capImage1.setImageResource(R.drawable.recommand_long_cap);
                capImage2.setImageResource(R.drawable.recommand_floppy_hat);
                capText1.setText("챙이 긴 캡");
                capText2.setText("와이드 플로피 햇");
                reasonText.setText("각진 얼굴형은 시선을 모자로 분산시키는 것이 포인트다. 뻣뻣하고 긴 챙은 각진 부분을 더 강조하기 마련, 챙을 살짝 구부려 굴곡을 연출해주는게 좋다. 한결 부드러운 느낌을 나타낼수 있으며, 긴 챙의 그림자가 얼굴의 각진 부분을 가려준다.");
                break;
            }
            case 3:{
                faceText.setText("둥근 얼굴형 입니다!");
                faceImage.setImageResource(R.drawable.result_round);
                capImage1.setImageResource(R.drawable.recommand_beret);
                capImage2.setImageResource(R.drawable.recommand_snapback);
                capText1.setText("각이 잡힌 베레모");
                capText2.setText("스냅백");
                reasonText.setText("둥근 얼굴형은 각 잡힌 베레모를 활용하면 시선을 분산시키는 데 탁월한데, 역시 모자를 살짝 띄워 쓰거나 베레모를 정수리 뒤 쪽으로 자리하게 하면 얼굴이 작아 보이는 효과를 얻을 수 있다. 얼굴을 많이 가리지 않으면서 둥근 느낌을 해소해줄 빳빳한 직선 챙의 스냅백은 베레모와 마찬가지로 시선이 앞쪽으로 쏠리면서 얼굴 전체의 균형을 잡아준다.");
                break;
            }
        }
        faceText.setTextSize(20.0f);
    }

    View.OnClickListener backButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
        }
    };

    View.OnClickListener webButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(),WebActivity.class);
            startActivity(intent);
        }
    };
}
