package io.divide.client.auth;

import io.divide.client.BackendUser;
import rx.Observer;
import rx.Subscription;

/**
 * Created by williamwebb on 2/3/14.
 */
public abstract class LoginListener implements Observer<BackendUser> {

    private Subscription subscription;

    @Override
    public void onCompleted() { }

    @Override
    public void onError(Throwable throwable) { }

    public void unsubscribe(){
        if(subscription!=null){
            subscription.unsubscribe();
        }
    }

    protected void setSubscription(Subscription subscription){
        this.subscription = subscription;
    }

}
