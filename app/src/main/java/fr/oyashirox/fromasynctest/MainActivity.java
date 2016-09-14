package fr.oyashirox.fromasynctest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import okhttp3.OkHttpClient;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private Repository repository;
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        repository = new Repository(new OkHttpClient(), this);
        final Button button = (Button) findViewById(R.id.button);
        final TextView textView = (TextView) findViewById(R.id.textview);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(subscription != null){
                    subscription.unsubscribe();
                    subscription = null;
                    button.setText("Download");
                    textView.setText("");
                    return;
                }

                button.setText("Cancel");

                subscription = repository.downloadTiles("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Progress>() {
                            @Override
                            public void onStart() {
                                super.onStart();
                                //request(1);
                            }

                            @Override
                            public void onCompleted() {
                                Timber.d("Download completed");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Timber.e(e, "Download error");
                            }

                            @Override
                            public void onNext(Progress progress) {
                                Timber.d("Download progress : " + progress.toString());
                                textView.setText(progress.toString());
                                //request(1);
                            }
                        });
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (subscription != null)
            subscription.unsubscribe();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
