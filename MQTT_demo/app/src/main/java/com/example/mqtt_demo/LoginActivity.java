package com.example.mqtt_demo;

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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mqtt_demo.db.MySQLiteOpenHelper;

public class LoginActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_REGISTER = 1;
    private static final String TAG="tag";
    private Button btnLogin;
    private EditText etAccount,etPassword;
    private CheckBox cbRemember;
    private String userName="a";
    private String pass="123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        getSupportActionBar().setTitle("登录");

        initView();
        initData();


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取用户输入的账号与密码
                String account = etAccount.getText().toString();
                String password = etPassword.getText().toString();

                // 判断账号密码是否符合要求
                if (TextUtils.isEmpty(account)) {
                    Toast.makeText(LoginActivity.this, "用户名不能为空！", Toast.LENGTH_LONG).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "密码不能为空！", Toast.LENGTH_LONG).show();
                    return;
                }

                // 验证用户名和密码
                if (checkUserCredentials(account, password)) {
                    Toast.makeText(LoginActivity.this, "恭喜你，登录成功！", Toast.LENGTH_LONG).show();
                    if (cbRemember.isChecked()) {
                        SharedPreferences spf = getSharedPreferences("spfRecorid", MODE_PRIVATE);
                        SharedPreferences.Editor edit = spf.edit();
                        edit.putString("account", account);
                        edit.putString("password", password);
                        edit.putBoolean("isRemember", true);
                        edit.apply();
                    } else {
                        SharedPreferences spf = getSharedPreferences("spfRecorid", MODE_PRIVATE);
                        SharedPreferences.Editor edit = spf.edit();
                        edit.putBoolean("isRemember", false);
                        edit.apply();
                    }

                    // 实现跳转
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    // 传递用户名
                    intent.putExtra("account", account);
                    startActivity(intent);
                    // 结束当前活动
                    LoginActivity.this.finish();
                } else {
                    Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //记住密码（取出数据）
    private void initData() {
        SharedPreferences spf=getSharedPreferences("spfRecorid",MODE_PRIVATE);
        boolean isRemember=spf.getBoolean("isRemember",false);
        String account=spf.getString("account","");
        String password=spf.getString("password","");
        //更新用户名密码（注册的用户名密码）
        userName=account;
        pass=password;

        if (isRemember){
            etAccount.setText(account);
            etPassword.setText(password);
            cbRemember.setChecked(true);
        }

    }

    //初始化
    private void initView(){
        btnLogin=findViewById(R.id.btn_login);
        etAccount=findViewById(R.id.et_account);
        etPassword=findViewById(R.id.et_password);
        cbRemember=findViewById(R.id.cb_remember);
    }

    //还没有账号（跳转到注册）
    public void toRegister(View view) {
        Intent intent=new Intent(this,RegisterActivity.class);

        //startActivity(intent);
        startActivityForResult(intent,REQUEST_CODE_REGISTER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CODE_REGISTER&&resultCode==RegisterActivity.RESULT_CODE_REGISTER&&data!=null){
            Bundle extras=data.getExtras();
            //获取用户密码
            String account=extras.getString("account","");
            String password=extras.getString("password","");
            etAccount.setText(account);
            etPassword.setText(password);

            //更新用户名密码（注册的用户名密码）
            userName=account;
            pass=password;
        }
    }


    //查询用户账号和密码
    private boolean checkUserCredentials(String account, String password) {
        MySQLiteOpenHelper mySQLiteOpenHelper = new MySQLiteOpenHelper(this);
        SQLiteDatabase db = mySQLiteOpenHelper.getReadableDatabase();

        // 使用参数化查询以防止 SQL 注入
        String[] columns = { "account", "password" };
        String selection = "account = ? AND password = ?";
        String[] selectionArgs = { account, password };

        Cursor cursor = db.query("account_password", columns, selection, selectionArgs, null, null, null);

        boolean isValidUser = cursor.getCount() > 0; // 如果查询结果大于0，用户名和密码正确

        cursor.close(); // 关闭游标
        db.close(); // 关闭数据库
        return isValidUser;
    }


}