package org.hse.android;

import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

public class TeacherActivity extends BaseActivity {
    @Override
    protected int getLayoutResId() {
        return R.layout.activity_teacher;
    }

    @Override
    protected int getSpinnerId() {
        return R.id.spinner_teacher;
    }

    @Override
    protected List<SpinnerItem> getSpinnerItems() {
        List<SpinnerItem> spinnerItems = new ArrayList<>();
        spinnerItems.add(new SpinnerItem(1, "Преподаватель 1"));
        spinnerItems.add(new SpinnerItem(2, "Преподаватель 2"));
        spinnerItems.add(new SpinnerItem(3, "Преподаватель 2"));
        return spinnerItems;
    }
}
