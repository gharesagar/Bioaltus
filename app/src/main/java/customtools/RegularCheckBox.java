package customtools;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.CheckBox;

public class RegularCheckBox extends CheckBox {
    public RegularCheckBox(Context context) {
        super(context);
        init();
    }
    public RegularCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RegularCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init(){
        Typeface face=Typeface.createFromAsset(getContext().getAssets(), "opensansregular.ttf");
        this.setTypeface(face);
    }
}
