package org.example.transactiontimeouttest;


import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/banking")
public class BankingResource {

    @EJB
    private BankingService bankingService;

    @Resource(name = "jdbc/banking")
    private DataSource dataSource;


    @GET
    @Path("/deposit")
    public Response deposit(@QueryParam("accountId") Long accountId, @QueryParam("amount") double amount) {
        try {
            bankingService.deposit(accountId, amount);
            return Response.ok("Deposit successful").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Deposit failed: " + e.getMessage())
                    .build();
        }
    }

}
