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
    private int DrawableType1[] = {
            R.drawable.black_cap,
            R.drawable.white_cap,
            R.drawable.blue_cap,
            R.drawable.pink_cap
    };
    private int DrawableType2[] = {
            R.drawable.black_pe,
            R.drawable.brown_pe,
            R.drawable.gray_pe
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
                    imageView.setImageResource(DrawableType1[position]);
                    break;
                }
                case 1:{
                    ImageView imageView = view.findViewById(R.id.CapImage);
                    imageView.setImageResource(DrawableType2[position]);
                    break;
                }
            }
        }

        // 뷰페이저에 추가.
        container.addView(view) ;

        return view ;
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
                ret = DrawableType1.length;
                break;
            case 1:
                ret = DrawableType2.length;
                break;
        }

        return ret;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == (View)object);
    }
}
