package org.hse.android;

import android.os.Bundle;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DemoActivity extends AppCompatActivity {

    private EditText inputNumber;
    private TextView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputNumber = findViewById(R.id.inputNumber);
        resultView = findViewById(R.id.resultView);
        Button sumButton = findViewById(R.id.sumButton);
        Button productButton = findViewById(R.id.productButton);

        sumButton.setOnClickListener(v -> calculateSum());
        productButton.setOnClickListener(v -> calculateProduct());
    }

    private void calculateSum() {
        long number = parseInput();
        if (number == -1) return;

        long sum = 0;
        for (long i = 1; i <= number; i++) {
            sum += i;
        }

        resultView.setText(getString(R.string.sum_result, sum));
    }

    private void calculateProduct() {
        long number = parseInput();
        if (number == -1) return;

        long product = 1;
        for (long i = 2; i <= number; i+=2) {
            product *= i;
        }

        resultView.setText(getString(R.string.product_result, product));
    }

    private long parseInput() {
        try {
            long number = Long.parseLong(inputNumber.getText().toString().trim());

            if (number < 2) {
                resultView.setText(R.string.invalid_input_min);
                Toast.makeText(this, R.string.invalid_input_min, Toast.LENGTH_SHORT).show();
                return -1;
            }
            if (number > 20) {
                resultView.setText(R.string.invalid_input_max);
                Toast.makeText(this, R.string.invalid_input_max, Toast.LENGTH_SHORT).show();
                return -1;
            }
            return number;
        } catch (NumberFormatException e) {
            resultView.setText(R.string.invalid_input);
            Toast.makeText(this, R.string.invalid_input, Toast.LENGTH_SHORT).show();
            return -1;
        }
    }
}
