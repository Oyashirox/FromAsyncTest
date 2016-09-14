package fr.oyashirox.fromasynctest;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import rx.AsyncEmitter;
import rx.Observable;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Created by Florian on 14/09/2016.
 */
public class Repository {
    private static final long BUFFER_SIZE = 2048;
    private final OkHttpClient mClient;
    private final Context mContext;

    public Repository(OkHttpClient mClient, Context mContext) {
        this.mClient = mClient;
        this.mContext = mContext;
    }

    public Observable<Progress> downloadTiles(final String tilesUrl) {
        return Observable.fromEmitter(new Action1<AsyncEmitter<Progress>>() {
            @Override
            public void call(AsyncEmitter<Progress> progressAsyncEmitter) {
                Request request = new Request.Builder()
                        .url(tilesUrl)
                        .build();

                try {
                    // see http://stackoverflow.com/a/34345052/1343969
                    final Call runningRequest =  mClient.newCall(request);
                    final Response response = runningRequest.execute();
                    long contentLength = response.body().contentLength();
                    File destinationFile = getDestinationFile();
                    Timber.d("Saving tiles zip to %s", destinationFile);
                    final BufferedSink sink = Okio.buffer(Okio.sink(destinationFile));

                    progressAsyncEmitter.setCancellation(new AsyncEmitter.Cancellable() {
                        @Override
                        public void cancel() throws Exception {
                            try {
                                runningRequest.cancel();
                                //response.body().close();
                                sink.close();
                                Timber.d("Download cancelled");
                            } catch (Exception e) {
                                Timber.wtf(e, "Should never happen");
                            }
                        }
                    });

                    long totalRead = 0;
                    long lastRead;
                    while ((lastRead = response.body().source().read(sink.buffer(), BUFFER_SIZE)) != -1) {
                        sink.emitCompleteSegments();
                        totalRead += lastRead;
                        progressAsyncEmitter.onNext(new Progress(totalRead, contentLength, false));
                    }
                    sink.writeAll(response.body().source());
                    sink.close();

                    progressAsyncEmitter.onNext(new Progress(totalRead, contentLength, true));
                    progressAsyncEmitter.onCompleted();
                } catch (IOException e) {
                    progressAsyncEmitter.onError(e);
                }
            }
        }, AsyncEmitter.BackpressureMode.DROP);
    }

    private File getDestinationFile() {
        return new File(mContext.getExternalFilesDir(null), "debian.iso");
    }
}
