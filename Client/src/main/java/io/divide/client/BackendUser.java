package io.divide.client;

import android.accounts.Account;
import android.app.Application;
import com.google.inject.Inject;
import com.jug6ernaut.android.logging.Logger;
import io.divide.client.auth.AuthManager;
import io.divide.client.auth.SignInResponse;
import io.divide.client.auth.SignUpResponse;
import io.divide.client.auth.credentials.LoginCredentials;
import io.divide.client.auth.credentials.SignUpCredentials;
import io.divide.client.auth.credentials.ValidCredentials;
import io.divide.client.data.Callback;
import io.divide.client.data.ServerResponse;
import io.divide.client.security.UserUtils;
import io.divide.shared.web.transitory.Credentials;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created with IntelliJ IDEA.
 * User: williamwebb
 * Date: 8/24/13
 * Time: 9:26 PM
 */
public final class BackendUser extends Credentials {

    private static Logger logger = Logger.getLogger(BackendUser.class);
    @Inject private static AuthManager authManager;
    @Inject private static Application app;

    private static final String ANONYMOUS_KEY = "anonymous_key";


    public BackendUser(){
//        authManager = Backend.get().getAuthManager();
    }

    public BackendUser(String username, String email, String password){
        this();
        this.setUsername(username);
        this.setEmailAddress(email);
        this.setPassword(password);
    }

    public static BackendUser from(ValidCredentials credentials){
        BackendUser beu = new BackendUser();
        beu.initFrom(credentials);
        return beu;
    }

    public static BackendUser getUser(){
        return authManager.getUser();
    }

    public static ServerResponse<BackendUser> getAnonymousUser(){
        String id = UserUtils.getDeviceIdUnique(app);
        ServerResponse<BackendUser> response;

        response = signIn(id, id);
        if(!response.getStatus().isSuccess()){
            response = signUp(id, id, id);
            if(response.getStatus().isSuccess()){
                BackendUser user = response.get();
                user.put(ANONYMOUS_KEY,true);
                user.saveASync();
            }
        }

        return response;
    }

    public static Account getStoredAccount(){
        return authManager.getStoredAccount();
    }

    protected final Credentials getLoggedInUser(){
        return getUser();
    }

    @Override
    protected final boolean isSystemUser(){
        return false;
    }

    private void initFrom(Credentials credentials){
        logger.debug("initFrom: " + credentials);
        try {
            this.meta_data = credentials.getMetaData();
            this.user_data = credentials.getUserData();
        } catch (Exception e) {
            logger.error("Failed to init BackendUser from Credentials",e);
        }
    }

    public static BackendUser fromToken(String token){
        return from(authManager.getRemoteUserFromToken(token));
    }

    public static SignInResponse signIn(LoginCredentials loginCredentials){
        return authManager.login(loginCredentials);
    }

    public static SignInResponse signIn(String email, String password){
        return signIn(new LoginCredentials(email,password));
    }

    public static SignUpResponse signUp(SignUpCredentials signUpCredentials){
        return authManager.signUp(signUpCredentials);
    }

    public static SignUpResponse signUp(String username, String email, String password){
        return signUp(new SignUpCredentials(username,email,password));
    }

    public static Observable<BackendUser> logInInBackground(String email, String password){
        return authManager.loginASync(new LoginCredentials(email,password));
//        authManager.loginASync(new LoginCredentials(email,password),callback);
    }

    public static void logout(){
        authManager.logout();
    }

    public void	requestPasswordReset(String email){
        throw new NotImplementedException();
    }
    public void	requestPasswordResetInBackground(String email, Callback<Boolean> callback){
        throw new NotImplementedException();
    }

    public Observable<BackendUser> signUpA(){
        SignUpCredentials creds = new SignUpCredentials(getUsername(),getEmailAddress(),getPassword());
        return authManager.signUpASync(creds);
//        authManager.signUpASync(creds,new Callback<BackendUser>() {
//            @Override
//            public void onResult(BackendUser backendUser) {
//                initFrom(backendUser);
//                callback.onResult(BackendUser.this);
//            }
//
//            @Override
//            public void onError(Exception t) {
//                callback.onError(t);
//            }
//        });
    }

    public boolean signUp(){
        SignUpCredentials creds = new SignUpCredentials(getUsername(),getEmailAddress(),getPassword());
        SignUpResponse response = authManager.signUp(creds);
        if(response.getStatus().isSuccess()){
            this.initFrom(response.get());
            return true;
        } else return false;
    }

    private void save(BackendUser object){
        authManager.sendUserData(object);
    }

    private void saveASync(BackendUser user, retrofit.Callback<String> callback){
        authManager.sendUserData(user,callback);
    }

    public void save(){
        save(this);
    }

    public void saveASync(){
        saveASync(this, new retrofit.Callback<String>() {
            @Override
            public void success(String s, Response response) {
                logger.debug("User update Success");
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                logger.error("User update Failed.", retrofitError);
            }
        });
    }

    public void saveASync(retrofit.Callback<String> callback){
        saveASync(this, callback);
    }

//    public ObjectManager getObjectManager(){
//        return objectManager;
//    }

    @Override
    public String toString() {
        return "BackendUser{" +
                "emailAddress='" + getEmailAddress() + '\'' +
                ", password='" + getPassword() + '\'' +
                ", userKey='" + getOwnerId() + '\'' +
                ", authToken='" + getAuthToken() + '\'' +
                ", createDate=" + getCreateDate() +
//                ", authTokenExpireDate=" + getAuthTokenExpireDate() +
                ", validation='" + getValidation() + '\'' +
                '}';
    }

}
