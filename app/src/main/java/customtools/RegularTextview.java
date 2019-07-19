package customtools;

import android.content.Context;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

public class RegularTextview extends AppCompatTextView {
    public RegularTextview(Context context) {
        super(context);
        init();
    }

    public RegularTextview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RegularTextview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    void init(){
        Typeface face=Typeface.createFromAsset(getContext().getAssets(), "opensansregular.ttf");
        this.setTypeface(face);
    }
}