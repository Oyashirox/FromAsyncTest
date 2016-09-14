package fr.oyashirox.fromasynctest;

/**
 * Created by Florian on 14/09/2016.
 */
public class Progress {
    public final long totalRead;
    public final long contentLength;
    public final boolean finished;

    public Progress(long totalRead, long contentLength, boolean finished) {

        this.totalRead = totalRead;
        this.contentLength = contentLength;
        this.finished = finished;
    }

    @Override
    public String toString() {
        return totalRead + "/" + contentLength + "(" + totalRead * 100 / contentLength + " %)";
    }
}
