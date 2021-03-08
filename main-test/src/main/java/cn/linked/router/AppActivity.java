package cn.linked.router;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import cn.linked.router.api.Router;
import cn.linked.router.common.Route;

@Route(path = "main")
public class AppActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        Router.initLoadLazy(getApplication());
    }
}
