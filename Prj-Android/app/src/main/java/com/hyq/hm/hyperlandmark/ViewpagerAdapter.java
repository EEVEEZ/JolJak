package com.hyq.hm.hyperlandmark;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.google.ar.sceneform.samples.hellosceneform.R;

public class ViewpagerAdapter extends PagerAdapter {
    // LayoutInflater 서비스 사용을 위한 Context 참조 저장.
    private Context mContext = null ;
    private int mType;
    private int DrawableType0[] = {
            R.drawable.blank
    };
    private int DrawableType1[] = {
            R.drawable.black_cap,
            R.drawable.white_cap,
            R.drawable.blue_cap,
            R.drawable.pink_cap
    };
    private int DrawableType2[] = {
            R.drawable.jungblue,
            R.drawable.jungbrown,
            R.drawable.jungco,
            R.drawable.jungpurple
    };
    private int DrawableType3[] = {
            R.drawable.bere_gray,
            R.drawable.beremo_pi,
            R.drawable.beremo_wh
    };

    public ViewpagerAdapter() {

    }

    // Context를 전달받아 mContext에 저장하는 생성자 추가.
    public ViewpagerAdapter(Context context, int type) {
        mContext = context ;
        mType = type;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = null ;

        if (mContext != null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.select_cap, container, false);
            switch (mType) {
                case 0: {
                    ImageView imageView = view.findViewById(R.id.CapImage);
                    imageView.setImageResource(DrawableType0[position]);
                    break;
                }
                case 1: {
                    ImageView imageView = view.findViewById(R.id.CapImage);
                    imageView.setImageResource(DrawableType1[position]);
                    break;
                }
                case 2:{
                    ImageView imageView = view.findViewById(R.id.CapImage);
                    imageView.setImageResource(DrawableType2[position]);
                    break;
                }
                case 3:{
                    ImageView imageView = view.findViewById(R.id.CapImage);
                    imageView.setImageResource(DrawableType3[position]);
                    break;
                }
            }
        }

        // 뷰페이저에 추가.
        container.addView(view) ;

        return view ;
    }

    public int getIndex(){
        return mType;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // 뷰페이저에서 삭제.
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        int ret = -1;
        switch (mType){
            case 0:
                ret = DrawableType0.length;
                break;
            case 1:
                ret = DrawableType1.length;
                break;
            case 2:
                ret = DrawableType2.length;
                break;
            case 3:
                ret = DrawableType3.length;
                break;
        }

        return ret;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == (View)object);
    }
}
