package com.example.mqtt_demo;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mqtt_demo.db.MySQLiteOpenHelper;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int RESULT_CODE_REGISTER = 0;
    private Button btnRegister;
    private EditText etAccount,etPass,etPassConfirm,etUnit,etIdentity;
    private CheckBox cbAgree;
    private SQLiteDatabase db;
    private MySQLiteOpenHelper mySQLiteOpenHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
//        getSupportActionBar().setTitle("注册");

        etAccount=findViewById(R.id.et_account);
        etPass=findViewById(R.id.et_password);
        etUnit=findViewById(R.id.et_unit);
        etIdentity=findViewById(R.id.et_identity);
        etPassConfirm=findViewById(R.id.et_password_Confirm);
        cbAgree=findViewById(R.id.cb_agree);
        btnRegister=findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String name = etAccount.getText().toString();
        String unit = etUnit.getText().toString();
        String identity = etIdentity.getText().toString();
        String pass = etPass.getText().toString();
        String passConfirm = etPassConfirm.getText().toString();
        String account = etAccount.getText().toString().trim();
        // 输入验证
        if (account.length() != 11) {
            Toast.makeText(RegisterActivity.this, "手机号必须为11位数！", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(RegisterActivity.this, "手机号不能为空！", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(unit)) {
            Toast.makeText(RegisterActivity.this, "单位不能为空！", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(identity)) {
            Toast.makeText(RegisterActivity.this, "身份不能为空！", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(RegisterActivity.this, "密码不能为空！", Toast.LENGTH_LONG).show();
            return;
        }
        if (!TextUtils.equals(pass, passConfirm)) {
            Toast.makeText(RegisterActivity.this, "密码不一致！", Toast.LENGTH_LONG).show();
            return;
        }
        if (!cbAgree.isChecked()) {
            Toast.makeText(RegisterActivity.this, "请同意用户协议！", Toast.LENGTH_LONG).show();
            return;
        }

        // 创建或打开数据库
        mySQLiteOpenHelper = new MySQLiteOpenHelper(this);
        db = mySQLiteOpenHelper.getWritableDatabase();

        // 插入用户数据到数据库中
        ContentValues values = new ContentValues();
        values.put("account", name);
        values.put("password", pass);
        values.put("unit", unit);
        values.put("identity", identity);

        long newRowId = db.insert("account_password", null, values);

        if (newRowId == -1) {
            Toast.makeText(RegisterActivity.this, "注册失败，请重试！", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_LONG).show();

            // 注册成功后回到登录页面，数据回传
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("account", name);
            bundle.putString("unit", unit);
            bundle.putString("identity", identity);
            bundle.putString("password", pass);
            intent.putExtras(bundle);
            setResult(RESULT_CODE_REGISTER, intent);
            this.finish(); // 结束当前活动
        }

        // 关闭数据库
        db.close();
    }
}