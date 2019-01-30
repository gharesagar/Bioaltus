package customtools;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class RegularButton extends android.support.v7.widget.AppCompatButton {
    public RegularButton(Context context) {
        super(context);
        init();
    }

    public RegularButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RegularButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init(){
        Typeface face=Typeface.createFromAsset(getContext().getAssets(), "opensansbold.ttf");
        this.setTypeface(face);
    }
}
