package com.etgames.alarmme;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
public class FabViewModel extends ViewModel {
    public final SingleLiveEvent<Void> fabClicked = new SingleLiveEvent();
    private final MutableLiveData<Boolean> showFab = new MutableLiveData<>();
  public final SingleLiveEvent<Void> quiteFabClicked = new SingleLiveEvent();
    private final MutableLiveData<Boolean> showQuiteFab = new MutableLiveData<>();

    public LiveData<Boolean> getShowFab() {
        return showFab;
    }
    public LiveData<Boolean> getShowQuiteFab() {
        return showQuiteFab;
    }

    public void triggerFabClick() {

        fabClicked.call();
    }
    public void triggerQuiteFabClick() {
        quiteFabClicked.call();
    }

    public void setShowFab(boolean show) {
        showFab.setValue(show);
    }
    public void setShowQuiteFab(boolean show) {
        showQuiteFab.setValue(show);
    }
}