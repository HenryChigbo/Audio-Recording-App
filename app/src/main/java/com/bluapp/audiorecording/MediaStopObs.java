package com.bluapp.audiorecording;

public class MediaStopObs {

    public interface MediaStopInterface {
        public void releasemediaplayer();
    }


    private static MediaStopObs mInstance;
    private MediaStopInterface mListener;
    private boolean mState;

    private MediaStopObs() {}

    public static MediaStopObs getInstance() {
        if(mInstance == null) {
            mInstance = new MediaStopObs();
        }
        return mInstance;
    }

    public void setListener(MediaStopInterface listener) {
        mListener = listener;
    }

    public void changeState(boolean state) {
        if(mListener != null) {
            mState = state;
            notifyStateChange();
        }
    }

    public boolean getState() {
        return mState;
    }

    private void notifyStateChange() {
        mListener.releasemediaplayer();
    }
}
