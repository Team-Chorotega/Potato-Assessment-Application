package com.chorotega_potato_assessment_app.helper;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InsetToolbar extends Toolbar {
    public InsetToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init() {
        ViewCompat.setOnApplyWindowInsetsListener(this, (v, insets) -> {
            Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarInsets.top,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });
    }
}