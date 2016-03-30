package ru.cubly.aceim.app.core;

import android.os.IBinder;
import android.os.RemoteException;

import ru.cubly.aceim.app.service.ICoreService;

public class AceIM {

    private static AceIM aceimInstance;
    public static AceIM getInstance() {
        if (aceimInstance == null) {
            aceimInstance = new AceIM();
        }
        return aceimInstance;
    }
    private AceIM() {}


    private ICoreService mCoreService;
    public ICoreService getCoreService() throws NullPointerException {
        assert mCoreService != null;
        return mCoreService;
    }
    public boolean isCoreServiceStarted() {
        return mCoreService != null;
    }
    public void startCoreService(IBinder service) {
        assert mCoreService == null;
        mCoreService = ICoreService.Stub.asInterface(service);
    }
    public void stopCoreService() throws RemoteException {
        try {
            mCoreService.exit(false);
        } finally {
            mCoreService = null;
        }
    }

    public void handleException(Exception e) {

    }
}