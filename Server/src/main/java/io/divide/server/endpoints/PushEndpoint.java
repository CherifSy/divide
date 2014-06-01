package io.divide.server.endpoints;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;
import io.divide.dao.ServerDAO;
import io.divide.server.auth.SecManager;
import io.divide.server.dao.DAOManager;
import io.divide.server.dao.Session;
import io.divide.shared.transitory.Credentials;
import io.divide.shared.transitory.EncryptedEntity;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

import static io.divide.server.utils.DaoUtils.getUserByEmail;
import static io.divide.server.utils.ResponseUtils.fromDAOExpection;
import static io.divide.shared.server.DAO.DAOException;

/**
 * Created with IntelliJ IDEA.
 * User: williamwebb
 * Date: 9/10/13
 * Time: 7:04 PM
 */
@Path("/push")
public class PushEndpoint {

    Logger logger = Logger.getLogger(PushEndpoint.class.getName());

    @Context DAOManager dao;
    @Context SecManager keyManager;

    /*
    currently failing as the decryption key is probably different
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(@Context Session session,EncryptedEntity.Reader entity){
        try{
            Credentials credentials = session.getUser();
            entity.setKey(keyManager.getPrivateKey());

            credentials.setPushMessagingKey(entity.get("token"));
            dao.save(credentials);
        } catch (DAOException e) {
            logger.severe(ExceptionUtils.getStackTrace(e));
            return fromDAOExpection(e);
        } catch (Exception e) {
            logger.severe(ExceptionUtils.getStackTrace(e));
            return Response.serverError().entity("Shit").build();
        }

        return Response.ok().build();
    }

    @DELETE
    public Response unregister(@Context Session session){
        try{
            Credentials credentials = session.getUser();
            credentials.setPushMessagingKey("");
            dao.save(credentials);
        } catch (ServerDAO.DAOException e) {
            logger.severe(ExceptionUtils.getStackTrace(e));
            return fromDAOExpection(e);
        }
        return Response.ok().build();
    }

    @GET
    @Path("/test/{email}/{data}")
    @Produces(MediaType.TEXT_HTML)
    public Response pushToDevice(@PathParam("email")String userId, @PathParam("data")String data){

        try {
            String result = sendMessageToDevice(userId,data);
            return Response.ok().entity(result).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    private String sendMessageToDevice(String email, String input) throws ServerDAO.DAOException, IOException {

        Credentials user = getUserByEmail(dao,email);

        Sender sender = new Sender(keyManager.getPushKey());
        Message message = new Message.Builder().addData("body", input).build();

        MulticastResult result = sender.send(message, Arrays.asList(user.getPushMessagingKey()), 5);

        System.out.println("Result = " + result);
        return result.toString();
    }

}
