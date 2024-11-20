package dev.e23.dashstar.handler;

import dev.e23.dashstar.model.Article;
import dev.e23.dashstar.model.Comment;
import dev.e23.dashstar.model.User;
import dev.e23.dashstar.repository.ArticleRepository;
import dev.e23.dashstar.repository.CommentRepository;
import dev.e23.dashstar.repository.UserRepository;
import dev.e23.dashstar.security.Secured;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.HashMap;
import java.util.Map;

@Path("/comments")
public class CommentHandler {

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private ArticleRepository articleRepository;

    @POST
    @Path("/")
    @Secured({"user", "admin"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createComment(Comment comment , @Context SecurityContext securityContext ) {
        User user = userRepository.findByID(Integer.valueOf(securityContext.getUserPrincipal().getName()));
        comment.setUser(user);
        Article article = articleRepository.findByID(comment.getArticleId());
        comment.setArticle(article);
        comment.setCreatedAt(System.currentTimeMillis() / 1000);
        commentRepository.create(comment);
        Map<String, Object> res = new HashMap<>();
        res.put("code", Response.Status.OK);
        return Response.status(Response.Status.OK).entity(res).build();
    }
    @DELETE
    @Path("/{id}")
    @Secured({"user", "admin"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteComment(@PathParam("id") int commentId, @Context SecurityContext securityContext) {
        Comment comment = commentRepository.findByID(commentId);
        if (comment == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(createErrorResponse("Comment not found")).build();
        }


        User user = userRepository.findByID(Integer.valueOf(securityContext.getUserPrincipal().getName()));
        if (!user.isAdmin() && !user.getId().equals(comment.getUser().getId())) {
            return Response.status(Response.Status.FORBIDDEN).entity(createErrorResponse("You do not have permission to delete this comment")).build();
        }

        commentRepository.delete(comment);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> res = new HashMap<>();
        res.put("code", Response.Status.BAD_REQUEST.getStatusCode());
        res.put("error", message);
        return res;
    }

}
