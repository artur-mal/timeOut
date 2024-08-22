package org.example.transactiontimeouttest;


import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Path("/banking")
public class BankingResource {

    @EJB
    private BankingService bankingService;

    @Resource(name = "jdbc/banking")
    private DataSource dataSource;


    @GET
    @Path("/deposit")
    public Response deposit(@QueryParam("accountId") Long accountId, @QueryParam("amount") double amount) {
        bankingService.deposit(accountId, amount);
        return Response.ok("Deposit successful").build();
    }

    @GET
    @Path("/withdraw")
    public Response withdraw(@QueryParam("accountId") Long accountId, @QueryParam("amount") double amount) {
        bankingService.withdraw(accountId, amount);
        return Response.ok("Withdrawal successful").build();
    }
    @GET
    @Path("/accounts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAccounts() {
        List<Account> accounts = bankingService.getAllAccounts();

        return Response.ok(accounts).build();
    }

    @GET
    @Path("/timeout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTimeOut() throws SQLException, InterruptedException {
        dataSource.getConnection();
        List<Connection> connections = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            connections.add(dataSource.getConnection());
        }

        System.out.println("Connections acquired and now idle. Sleeping for 10 minutes...");
        Thread.sleep(60000);  // Sleep for 1 minutes to simulate idle connections

        for (Connection connection : connections) {
            connection.close();
        }
        return Response.ok("end").build();
    }
}
