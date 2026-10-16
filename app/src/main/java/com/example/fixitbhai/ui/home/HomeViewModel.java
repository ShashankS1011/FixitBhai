package com.example.fixitbhai.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.example.fixitbhai.Contact;
import com.example.fixitbhai.HomeRepository;
import com.example.fixitbhai.util.Event;
import com.example.fixitbhai.util.Resource;

import java.util.List;

/**
 * ViewModel managing HomeFragment state and surviving process death / configuration changes.
 * Exposes immutable LiveData to UI without keeping Activity/Fragment references.
 */
public class HomeViewModel extends AndroidViewModel {

    private final HomeRepository repository;
    private final SavedStateHandle savedStateHandle;

    private LiveData<Resource<List<Contact>>> contactsLiveData;
    private final MutableLiveData<Event<String>> toastEvent = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application, @NonNull SavedStateHandle savedStateHandle) {
        super(application);
        this.repository = new HomeRepository(application);
        this.savedStateHandle = savedStateHandle;
        loadContacts();
    }

    public void loadContacts() {
        contactsLiveData = repository.getContacts();
    }

    @NonNull
    public LiveData<Resource<List<Contact>>> getContactsLiveData() {
        return contactsLiveData;
    }

    @NonNull
    public LiveData<Event<String>> getToastEvent() {
        return toastEvent;
    }

    public void triggerToastEvent(String message) {
        toastEvent.setValue(new Event<>(message));
    }
}