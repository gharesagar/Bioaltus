package customtools;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class BoldTextview extends AppCompatTextView {
    public BoldTextview(Context context) {
        super(context);
        init();
    }

    public BoldTextview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoldTextview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    void init(){
        Typeface face=Typeface.createFromAsset(getContext().getAssets(), "opensansbold.ttf");
        this.setTypeface(face);
    }
}
