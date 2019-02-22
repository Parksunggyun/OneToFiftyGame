package superbrain.rowan.com.onetofiftygame;

import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.Toast;

import java.util.Vector;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import superbrain.rowan.com.onetofiftygame.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    Vector<Integer> _1to25, _26to50;
    ActivityMainBinding binding;
    ItemAdapter adapter;
    Observable<Long> duration;
    Disposable disposable;
    int now;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        handler = new Handler();
        init();
        binding.retryBtn.setOnClickListener(view -> {
            stop();
            init();

        });
    }


    private void timer() {
        duration = Observable.interval(10, TimeUnit.MILLISECONDS)
                .map(milli -> milli += 1L);
        disposable = duration.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    long sec = s / 100;
                    long milli = s % 100;
                    runOnUiThread(() -> binding.timeTxtView.setText(sec + " : " + milli));
                });

    }

    private void stop() {
        CompositeDisposable disposable = new CompositeDisposable();
        disposable.add(this.disposable);
        disposable.dispose();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
    }

    private void init() {
        timer();
        binding.gridView.removeOnItemTouchListener(select);
        now = 1;
        _1to25 = new Vector<>();
        _26to50 = new Vector<>();
        binding.gridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int length = binding.gridView.getWidth() / 5 - 10;
                adapter.setLength(length, length);
                adapter.notifyDataSetChanged();

                binding.gridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        for (int i = 1; i <= 25; i++) {
            _1to25.add(i);
            _26to50.add(i + 25);
        }
        binding.gridView.setLayoutManager(new GridLayoutManager(this, 5));
        adapter = new ItemAdapter(this);
        binding.gridView.setAdapter(adapter);
        for (int i = 1; i <= 25; i++) {
            int rand = (int) (Math.random() * _1to25.size());
            adapter.init1to25(_1to25.get(rand));
            _1to25.remove(rand);
            adapter.notifyDataSetChanged();
        }
        binding.gridView.addOnItemTouchListener(select);
    }

    private RecyclerView.OnItemTouchListener select = new RecyclerView.OnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView parent, @NonNull MotionEvent evt) {
            try {
                switch (evt.getAction()) {
                    case MotionEvent.ACTION_UP:
                        Button child = (Button) parent.findChildViewUnder(evt.getX(), evt.getY());
                        if (child != null) {
                            int selected = Integer.parseInt(child.getText().toString());
                            if (selected == now) {
                                int position = parent.getChildAdapterPosition(child);
                                Log.e("position", " => " + selected);
                                if (selected >= 26 && selected == now)
                                    adapter.setUpVisible(position);
                                now++;
                                if (_26to50 != null) {
                                    int rand = (int) (Math.random() * _26to50.size());
                                    adapter.update26to50(position, _26to50.get(rand));
                                    _26to50.remove(rand);
                                    if (_26to50.size() == 0) _26to50 = null;
                                }
                                adapter.notifyItemChanged(position);
                            } else {
                                Toast.makeText(MainActivity.this, "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                            }
                            if (now == 51) stop();
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean b) {

        }
    };

}
