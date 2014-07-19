package io.divide.shared.transitory;


import io.divide.shared.util.Base64;
import io.divide.shared.util.Crypto;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: williamwebb
 * Date: 7/27/13
 * Time: 6:19 PM
 *
 * Base user object.
 */

public class Credentials extends TransientObject implements Serializable {

    private static final MetaKey PASSWORD_KEY = new MetaKey("pw");
    public static final MetaKey EMAIL_KEY = new MetaKey("email");
    public static final MetaKey AUTH_TOKEN_KEY = new MetaKey("auth_token");
    public static final MetaKey RECOVERY_TOKEN_KEY = new MetaKey("recovery_auth_token");
    private static final MetaKey AUTH_TOKEN_EXPIRE_KEY = new MetaKey("auth_token_expire");
    public static final MetaKey USERNAME_KEY = new MetaKey("username");
    protected static final MetaKey VALIDATION_KEY = new MetaKey("validation");
    protected static final MetaKey PUSH_MESSAGING_KEY = new MetaKey("push_messaging_key");
    public static final MetaKey USER_GROUP_KEY = new MetaKey("user_group_key");


    protected Credentials(){
        super(Credentials.class);
    }

    private Credentials(Credentials credentials){
        super(Credentials.class);
        this.meta_data = new HashMap<String, String>(credentials.meta_data);
        this.user_data = new HashMap<String, Object>(credentials.user_data);
    }

    public Credentials(String username, String email, String password){
        super(Credentials.class);
        setUsername(username);
        setEmailAddress(email);
        setPassword(password);
    }

    protected boolean isSystemUser(){
        return false;
    }

    public String getUserGroup(){
        return meta_get(String.class,USER_GROUP_KEY);
    }

    public void setUserGroup(String group){
        meta_put(USER_GROUP_KEY,group);
    }

//    @Override
//    protected String getLoggedInUser(){
//        return getOwnerId();
//    }

    public String getPassword() {
        return meta_get(String.class, PASSWORD_KEY);
    }

    public void setPassword(String password) {
        meta_put(PASSWORD_KEY, password);
    }

    public void decryptPassword(PrivateKey privateKey){
        byte[] decoded = Base64.decode(getPassword().getBytes());
        byte[] encrypted = Crypto.decrypt(decoded,privateKey);
        setPassword(new String(encrypted));
    }

    public void encryptPassword(PublicKey publicKey){
        byte[] encrypted  = Crypto.encrypt(getPassword().getBytes(),publicKey);
        String encoded = new String( Base64.encode(encrypted) );
        setPassword(encoded);
    }

    public String getEmailAddress() {
        return meta_get(String.class, EMAIL_KEY);
    }

    public void setEmailAddress(String emailAddress) {
        meta_put(EMAIL_KEY, emailAddress);
    }

    public String getAuthToken() {
        return meta_get(String.class, AUTH_TOKEN_KEY);
    }

    public void setAuthToken(String authToken) {
        meta_put(AUTH_TOKEN_KEY, authToken);
    }

    public String getValidation() {
        return meta_get(String.class, VALIDATION_KEY);
    }

    public void setValidation(String validation) {
        meta_put(VALIDATION_KEY, validation);
    }

    public void setUsername(String username){
        meta_put(USERNAME_KEY, username);
    }

    public String getUsername(){
        return meta_get(String.class, USERNAME_KEY);
    }

    public void setPushMessagingKey(String key){
        meta_put(PUSH_MESSAGING_KEY, key);
    }

    public String getPushMessagingKey(){
        return meta_get(String.class, PUSH_MESSAGING_KEY);
    }

    public String getRecoveryToken() {
        return meta_get(String.class, RECOVERY_TOKEN_KEY);
    }

    public void setRecoveryToken(String authToken) {
        meta_put(RECOVERY_TOKEN_KEY, authToken);
    }

    /**
     * New Credentials object which contains no sensitive information, removing
     * Password
     * auth token
     * auth token expiration date
     * validation token
     * push messaging token
     * recovery token
     * @return save version of this Credentials object.
     */
    public Credentials getSafe(){
        Credentials safeCreds = new Credentials(this);
        safeCreds.meta_remove(PASSWORD_KEY);
        safeCreds.meta_remove(AUTH_TOKEN_KEY);
        safeCreds.meta_remove(AUTH_TOKEN_EXPIRE_KEY);
        safeCreds.meta_remove(VALIDATION_KEY);
        safeCreds.meta_remove(PUSH_MESSAGING_KEY);
        safeCreds.meta_remove(RECOVERY_TOKEN_KEY);
        return safeCreds;
    }


//    @Override
//    public String toString() {
//        return "Credentials{" +
//                "emailAddress='" + getEmailAddress() + '\'' +
//                ", username='" + getUsername() + '\'' +
//                ", password='" + getPassword() + '\'' +
//                ", userKey='" + getUserId() + '\'' +
//                ", createDate=" + getCreateDate() +
//                ", authToken='" + getAuthToken() + '\'' +
//                ", authTokenExpireDate=" + getAuthTokenExpireDate() +
//                ", validation='" + getValidation() + '\'' +
//                '}';
//    }
}
