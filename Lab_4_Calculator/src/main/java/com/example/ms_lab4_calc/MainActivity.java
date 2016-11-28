package com.example.ms_lab4_calc;

import android.gesture.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener{
    private GestureLibrary gLib;
    private GestureOverlayView gestures;
    private int operand1;
    private boolean isSet1 = true;
    private int operand2;
    private boolean isSet2 = false;
    private Operator operator;
    private TextView operand1View;
    private TextView operand2View;
    private TextView operatorView;
    private TextView errorView;
    enum Operator {
        PLUS("+"), MINUS("-"), MULTIPLY("*"), DIVIDE("/"), EQUAL("=");
        String operator;
        Operator(String operator) {
            this.operator = operator;
        }
        @Override
        public String toString(){
            return operator;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!gLib.load()) {
            finish();
        }
        operand1View = (TextView) findViewById(R.id.operand1);
        operand2View = (TextView) findViewById(R.id.operand2);
        operatorView = (TextView) findViewById(R.id.operator);
        errorView = (TextView)findViewById(R.id.errorView);
        gestures = (GestureOverlayView) findViewById(R.id.gestureView);
        gestures.addOnGesturePerformedListener(this);
        operand1 = 0;
        operator = null;

    }

    private int compute(int operand1, int operand2, Operator operator){
        int result = 0;
        if (operator.equals(Operator.PLUS)) {
            result =  operand1 + operand2;
        } else if (operator.equals(Operator.MINUS)){
            result =  operand1 - operand2;
        }
        else if (operator.equals(Operator.MULTIPLY)){
            result = operand1*operand2;
        }
        else if (operator.equals(Operator.DIVIDE)){
            if(operand2==0){
                errorView.setText(getString(R.string.divideByZero));
                return 0;
            }
            result =  operand1/operand2;
        }
        return result;
    }

    private void refresh(){
        operand2View.setText(isSet2?Integer.toString(operand2):"");
        operand1View.setText(isSet1?Integer.toString(operand1):"");
        operatorView.setText(operator==null?"":operator.toString());
    }

    private  Operator resolveOperator(String name){
        if(name.equals(Operator.MULTIPLY.toString())){
            return Operator.MULTIPLY;
        } else if(name.equals(Operator.DIVIDE.toString())){
            return Operator.DIVIDE;
        } else if(name.equals(Operator.MINUS.toString())) {
            return Operator.MINUS;
        } else if (name.equals(Operator.PLUS.toString())){
            return Operator.PLUS;
        }
        return  Operator.MULTIPLY;
    }
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        errorView.setText("");
        ArrayList<Prediction> predictions = gLib.recognize(gesture);
        if (predictions.size() > 0) {
            Collections.sort(predictions, new Comparator<Prediction>() {
                @Override
                public int compare(Prediction lhs, Prediction rhs) {
                    return Double.valueOf(rhs.score).compareTo(Double.valueOf(lhs.score));
                }
            });
            Prediction prediction = predictions.get(0);
            if (prediction.score > 1.0) {
                Toast toast = Toast.makeText(getApplicationContext(), prediction.name, Toast.LENGTH_SHORT);
                toast.show();
                if (Character.isDigit(prediction.name.charAt(0))) {
                    if(operator == null) {
                        operand1 = operand1>=0?operand1*10+Integer.parseInt(prediction.name):-(-operand1*10+Integer.parseInt(prediction.name));
                        isSet1 = true;
                    } else {
                        isSet2 = true;
                        operand2 = operand2>=0?operand2*10+Integer.parseInt(prediction.name):-(-operand2*10+Integer.parseInt(prediction.name));
                    }
                }
                else if (prediction.name.equals(Operator.EQUAL.toString())) {
                    if(isSet1 && operator != null && isSet2) {
                        operand1 = compute(operand1, operand2, operator);
                        isSet2 = false;
                        operand2 = 0;
                        operator = null;
                    } else {
                        operator = null;
                        isSet2 = false;
                        operand2 = 0;
                        isSet1 = true;
                    }
                }
                else if(prediction.name.equals(Operator.DIVIDE.toString()) || prediction.name.equals(Operator.MINUS.toString())
                        || prediction.name.equals(Operator.PLUS.toString()) || prediction.name.equals(Operator.DIVIDE.toString())){
                    if(isSet1 && operator == null){
                        operator = resolveOperator(prediction.name);
                    } else if(isSet1 && operator != null && !isSet2){
                        operator = resolveOperator(prediction.name);
                    } else if(isSet1 && operator!=null && isSet2){
                        operand1 = compute(operand1, operand2, operator);
                        operand2 = 0;
                        isSet2 = false;
                        operator = resolveOperator(prediction.name);
                    }
                }
                else if(prediction.name.equals(getString(R.string.stop))) {
                    operand1 = 0;
                    operand2 = 0;
                    operator = null;
                    isSet1 = true;
                    isSet2 = false;
                } else {
                    errorView.setText(getString(R.string.noGesture));
                }
            } else{
                errorView.setText(getString(R.string.noGesture));
            }
        } else{
            errorView.setText(getString(R.string.noGesture));
        }
        refresh();
    }
}
